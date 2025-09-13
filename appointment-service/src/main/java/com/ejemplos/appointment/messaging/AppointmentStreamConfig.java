package com.ejemplos.appointment.messaging;

import com.ejemplos.common.events.SagaEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

@Configuration
public class AppointmentStreamConfig {

    @Bean
    public BlockingQueue<SagaEvent> appointmentOutboxQueue() {
        // Simple outbox en memoria para demo
        return new ArrayBlockingQueue<>(1024);
    }

    // Supplier polled por Spring Cloud Stream (poller configurado en application.yml)
    @Bean
    public Supplier<SagaEvent> appointmentRequestedSupplier(BlockingQueue<SagaEvent> appointmentOutboxQueue) {
        return appointmentOutboxQueue::poll;
    }
}
