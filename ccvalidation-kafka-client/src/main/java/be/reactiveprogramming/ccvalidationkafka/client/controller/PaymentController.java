package be.reactiveprogramming.ccvalidationkafka.client.controller;

import be.reactiveprogramming.ccvalidationkafka.client.gateway.PaymentGateway;
import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentEvent;
import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentResultEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class PaymentController {

    private final PaymentGateway paymentGateway;

    public PaymentController(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @PostMapping(value = "/payment")
    public Mono<PaymentResultEvent> eventStream(@RequestBody PaymentEvent payment) {
        return paymentGateway.doPayment(payment);
    }
}
