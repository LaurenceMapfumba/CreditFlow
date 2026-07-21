package com.creditflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CreditflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditflowApplication.class, args);
    }
}
