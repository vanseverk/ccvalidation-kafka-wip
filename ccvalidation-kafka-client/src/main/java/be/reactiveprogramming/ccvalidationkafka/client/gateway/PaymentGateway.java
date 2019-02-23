package be.reactiveprogramming.ccvalidationkafka.client.gateway;

import be.reactiveprogramming.ccvalidationkafka.client.command.CreatePaymentCommand;
import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentResultEvent;
import reactor.core.publisher.Mono;

public interface PaymentGateway {

    Mono<PaymentResultEvent> doPayment(CreatePaymentCommand createPayment);

}
