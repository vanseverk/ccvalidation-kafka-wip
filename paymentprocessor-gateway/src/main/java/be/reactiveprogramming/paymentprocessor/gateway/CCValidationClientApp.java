package be.reactiveprogramming.paymentprocessor.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
public class CCValidationClientApp {

  public static void main(String[] args) {
    SpringApplication.run(CCValidationClientApp.class, args);
  }

}
