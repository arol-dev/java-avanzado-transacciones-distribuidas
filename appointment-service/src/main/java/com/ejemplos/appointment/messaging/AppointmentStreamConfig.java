package com.ejemplos.appointment.messaging;

import com.ejemplos.common.events.SagaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

@Configuration
public class AppointmentStreamConfig {

    private static final Logger log = LoggerFactory.getLogger(AppointmentStreamConfig.class);

    @Bean
    public BlockingQueue<SagaEvent> appointmentOutboxQueue() {
        // Simple outbox en memoria para demo
        return new ArrayBlockingQueue<>(1024);
    }

    // Supplier polled por Spring Cloud Stream (poller configurado en application.yml)
    @Bean
    public Supplier<SagaEvent> appointmentRequestedSupplier(BlockingQueue<SagaEvent> appointmentOutboxQueue) {
        return () -> {
            SagaEvent ev = appointmentOutboxQueue.poll();
            if (ev != null) {
                log.info("[AppointmentStream] Emisión de evento type={} sagaId={} eventId={} ts={} -> topic=appointments.requested",
                        ev.type(), ev.sagaId(), ev.eventId(), ev.timestamp());
            }
            return ev;
        };
    }
}
