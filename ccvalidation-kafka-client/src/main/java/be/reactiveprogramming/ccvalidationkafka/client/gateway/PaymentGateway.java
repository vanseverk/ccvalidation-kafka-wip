package be.reactiveprogramming.ccvalidationkafka.client.gateway;

import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentEvent;
import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentResultEvent;
import reactor.core.publisher.Mono;

public interface PaymentGateway {

    Mono<PaymentResultEvent> doPayment(PaymentEvent payment);

}
