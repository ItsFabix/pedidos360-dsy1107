package cl.duoc.dsy1107.mensajeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MensajeriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MensajeriaApplication.class, args);
    }
}