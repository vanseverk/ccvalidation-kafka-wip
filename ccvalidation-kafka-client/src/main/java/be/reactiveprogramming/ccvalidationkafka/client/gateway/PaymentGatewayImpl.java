package be.reactiveprogramming.ccvalidationkafka.client.gateway;

import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentEvent;
import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentResultEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;
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

@Component
public class PaymentGatewayImpl implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayImpl.class.getName());

    private KafkaReceiver kafkaReceiver;

    private KafkaSender kafkaProducer;

    private ObjectMapper objectMapper = new ObjectMapper();

    private TopicProcessor<PaymentResultEvent> sharedReceivedMessages;

    public PaymentGatewayImpl() {
        final Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        final SenderOptions<Integer, String> producerOptions = SenderOptions.create(producerProps);

        kafkaProducer = KafkaSender.create(producerOptions);

        final Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "payment-gateway-1");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-gateway");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        ReceiverOptions<Object, Object> consumerOptions = ReceiverOptions.create(consumerProps)
                .subscription(Collections.singleton("payment-gateway-1-feedback"))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        kafkaReceiver = KafkaReceiver.create(consumerOptions);

        Flux<ReceiverRecord> incomingReceivedMessages = kafkaReceiver.receive();

        Flux<PaymentResultEvent> msgs = incomingReceivedMessages
                .map(r -> {
                    r.receiverOffset().acknowledge();
                    PaymentResultEvent result = fromBinary((String) r.value(), PaymentResultEvent.class);
                    System.out.println("Result: " + result);
                    return result;
                });

        sharedReceivedMessages = TopicProcessor.create();

        msgs.subscribe(sharedReceivedMessages);

        Flux.from(sharedReceivedMessages).doOnEach(a -> log.info("Received reply from validator")).subscribe();
    }


    @Override
    public Mono<PaymentResultEvent> doPayment(final PaymentEvent payment) {
        log.info("Sending payment..");

        String payload = toBinary(payment);

        return Flux.from(sharedReceivedMessages)
                .filter(received -> {
                    System.out.println(payment.getId() + " " + received.getPaymentId());
                    return payment.getId().equals(received.getPaymentId());
                })
                .map(r -> {
                    System.out.println("Got our result! " + r.getPaymentId());
                    return r;
                }).doOnSubscribe(s -> {
                    SenderRecord<Integer, String, Integer> message = SenderRecord.create(new ProducerRecord<>("unconfirmed-transactions", 1, payload), 1);

                    kafkaProducer.send(Mono.just(message)).subscribe();
                })
                .take(1)
                .single()
                .map(s -> {
                    System.out.println("Our single result " + s);
                    return s;
                });
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
