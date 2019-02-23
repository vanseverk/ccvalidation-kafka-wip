package be.reactiveprogramming.ccvalidationkafka.paymentvalidator.validator;

import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentEvent;
import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentResultEvent;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PaymentValidatorImpl implements PaymentValidator {

    private Random r = new Random();

    @Override
    public PaymentResultEvent calculateResult(PaymentEvent paymentEvent) {
        return new PaymentResultEvent(paymentEvent.getId(), r.nextBoolean());
    }
}
