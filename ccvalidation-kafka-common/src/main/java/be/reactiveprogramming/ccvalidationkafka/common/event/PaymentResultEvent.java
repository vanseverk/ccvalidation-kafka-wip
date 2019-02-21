package be.reactiveprogramming.ccvalidationkafka.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResultEvent {

  private String paymentId;
  private Boolean succesful;

  public PaymentResultEvent() {
  }

  public PaymentResultEvent(String paymentId, Boolean succesful) {
    this.paymentId = paymentId;
    this.succesful = succesful;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public Boolean getSuccesful() {
    return succesful;
  }
}
