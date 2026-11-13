package br.com.fiap.bairron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiBairrOnApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiBairrOnApplication.class, args);
    }

}
