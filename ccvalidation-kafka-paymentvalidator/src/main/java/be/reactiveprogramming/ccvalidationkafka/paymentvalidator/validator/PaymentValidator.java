package be.reactiveprogramming.ccvalidationkafka.paymentvalidator.validator;

import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentEvent;
import be.reactiveprogramming.ccvalidationkafka.common.event.PaymentResultEvent;

public interface PaymentValidator {

    PaymentResultEvent calculateResult(PaymentEvent paymentEvent);

}
