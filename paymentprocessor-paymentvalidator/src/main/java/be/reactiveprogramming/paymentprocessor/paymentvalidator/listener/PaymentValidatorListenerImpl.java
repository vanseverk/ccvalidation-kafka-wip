package be.reactiveprogramming.paymentprocessor.paymentvalidator.listener;

import be.reactiveprogramming.paymentprocessor.common.event.PaymentEvent;
import be.reactiveprogramming.paymentprocessor.common.event.PaymentResultEvent;
import be.reactiveprogramming.paymentprocessor.paymentvalidator.validator.PaymentValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class PaymentValidatorListenerImpl {

    private static final Logger log = LoggerFactory.getLogger(PaymentValidatorListenerImpl.class.getName());

    private KafkaReceiver kafkaReceiver;

    private KafkaSender kafkaProducer;

    private Random r = new Random();

    private ObjectMapper objectMapper = new ObjectMapper();

    private PaymentValidator paymentValidator;

    public PaymentValidatorListenerImpl(PaymentValidator paymentValidator) {
        this.paymentValidator = paymentValidator;

        final Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        final SenderOptions<Integer, String> producerOptions = SenderOptions.create(producerProps);

        kafkaProducer = KafkaSender.create(producerOptions);

        final Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "payment-validator-1");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-validator");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        ReceiverOptions<Object, Object> consumerOptions = ReceiverOptions.create(consumerProps)
                .subscription(Collections.singleton("unconfirmed-transactions"))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        kafkaReceiver = KafkaReceiver.create(consumerOptions);

        Scheduler scheduler = Schedulers.newElastic("sample", 60, true);

        Flux<ReceiverRecord> incomingReceivedMessages =
                ((Flux<ReceiverRecord>) kafkaReceiver.receive())
                        .groupBy(m -> {
                            System.out.println("TP: " + m.receiverOffset().topicPartition());
                            return m.receiverOffset().topicPartition();
                        })
                        .flatMap(partitionFlux ->
                                partitionFlux.publishOn(scheduler));

        incomingReceivedMessages
                .map(r -> {
                    r.receiverOffset().acknowledge();
                    return fromBinary((String) r.value(), PaymentEvent.class);
                })
                .flatMap(paymentEvent -> processEvent(paymentEvent))
                .subscribe();
    }

    private Publisher<?> processEvent(PaymentEvent paymentEvent) {
        PaymentResultEvent paymentResultEvent = paymentValidator.calculateResult(paymentEvent);
        return sendReply(paymentResultEvent, paymentEvent.getGateway());
    }

    private Publisher<?> sendReply(PaymentResultEvent paymentResultEvent, String gatewayName) {
        String payload = toBinary(paymentResultEvent);
        String feedbackTopic = "payment-gateway-" + gatewayName + "-feedback";

        SenderRecord<Integer, String, Integer> message = SenderRecord.create(new ProducerRecord<>(feedbackTopic, 1, payload), 1);
        return kafkaProducer.send(Mono.just(message));
    }

    private String toBinary(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private <T> T fromBinary(String object, Class<T> resultType) {
        try {
            return objectMapper.readValue(object, resultType);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
