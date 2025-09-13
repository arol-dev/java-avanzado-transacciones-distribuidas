package com.ejemplos.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.ejemplos"})
public class SagaOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SagaOrchestratorApplication.class, args);
    }
}
