package be.reactiveprogramming.paymentprocessor.gateway.controller;

import be.reactiveprogramming.paymentprocessor.common.event.PaymentResultEvent;
import be.reactiveprogramming.paymentprocessor.gateway.command.CreatePaymentCommand;
import be.reactiveprogramming.paymentprocessor.gateway.gateway.PaymentGateway;
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
