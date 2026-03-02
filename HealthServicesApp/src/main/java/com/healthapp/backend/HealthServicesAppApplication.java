package com.healthapp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HealthServicesAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthServicesAppApplication.class, args);
    }

}
