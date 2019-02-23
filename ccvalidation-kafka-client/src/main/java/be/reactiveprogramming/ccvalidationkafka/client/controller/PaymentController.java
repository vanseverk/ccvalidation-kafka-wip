package be.reactiveprogramming.ccvalidationkafka.client.controller;

import be.reactiveprogramming.ccvalidationkafka.client.command.CreatePaymentCommand;
import be.reactiveprogramming.ccvalidationkafka.client.gateway.PaymentGateway;
import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentResultEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController {

    private final PaymentGateway paymentGateway;

    public PaymentController(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @PostMapping(value = "/payment")
    public Mono<PaymentResultEvent> eventStream(@RequestBody CreatePaymentCommand payment) {
        return paymentGateway.doPayment(payment);
    }
}
