package be.reactiveprogramming.paymentprocessor.paymentvalidator.validator;

import be.reactiveprogramming.paymentprocessor.common.event.PaymentEvent;
import be.reactiveprogramming.paymentprocessor.common.event.PaymentResultEvent;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PaymentValidatorImpl implements PaymentValidator {

    private Random r = new Random();

    @Override
    public PaymentResultEvent calculateResult(PaymentEvent paymentEvent) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new PaymentResultEvent(paymentEvent.getId(), r.nextBoolean());
    }
}
