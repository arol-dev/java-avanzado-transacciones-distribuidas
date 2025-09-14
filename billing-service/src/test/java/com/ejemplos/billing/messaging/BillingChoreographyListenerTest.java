package com.ejemplos.billing.messaging;

import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class BillingChoreographyListenerTest {

    private BlockingQueue<SagaEvent> billingChargedQueue;
    private BlockingQueue<SagaEvent> sagaCompletedQueue;

    private Consumer<SagaEvent> flightConsumer;
    private Consumer<SagaEvent> hotelConsumer;

    @BeforeEach
    void setUp() {
        billingChargedQueue = new ArrayBlockingQueue<>(16);
        sagaCompletedQueue = new ArrayBlockingQueue<>(16);
        BillingChoreographyListener listener = new BillingChoreographyListener(billingChargedQueue, sagaCompletedQueue);
        flightConsumer = listener.flightReservedConsumer();
        hotelConsumer = listener.hotelReservedConsumer();
    }

    @Test
    void onlyFlightReserved_doesNotEmit() {
        String sagaId = UUID.randomUUID().toString();
        flightConsumer.accept(buildFlightReserved(sagaId));

        assertThat(billingChargedQueue.poll()).as("Should not emit BILLING_CHARGED until both reservations are ready").isNull();
        assertThat(sagaCompletedQueue.poll()).as("Should not emit SAGA_COMPLETED until both reservations are ready").isNull();
    }

    @Test
    void onlyHotelReserved_doesNotEmit() {
        String sagaId = UUID.randomUUID().toString();
        hotelConsumer.accept(buildHotelReserved(sagaId));

        assertThat(billingChargedQueue.poll()).isNull();
        assertThat(sagaCompletedQueue.poll()).isNull();
    }

    @Test
    void flightThenHotel_emitsChargedAndCompletedOnce() {
        String sagaId = UUID.randomUUID().toString();
        flightConsumer.accept(buildFlightReserved(sagaId));
        hotelConsumer.accept(buildHotelReserved(sagaId));

        SagaEvent charged = billingChargedQueue.poll();
        assertThat(charged).as("Expected BILLING_CHARGED event").isNotNull();
        assertThat(charged.type()).isEqualTo(SagaEventType.BILLING_CHARGED);
        assertThat(charged.sagaId()).isEqualTo(sagaId);

        SagaEvent completed = sagaCompletedQueue.poll();
        assertThat(completed).as("Expected SAGA_COMPLETED event").isNotNull();
        assertThat(completed.type()).isEqualTo(SagaEventType.SAGA_COMPLETED);
        assertThat(completed.sagaId()).isEqualTo(sagaId);

        assertThat(billingChargedQueue.poll()).as("Should emit only once per sagaId").isNull();
        assertThat(sagaCompletedQueue.poll()).as("Should emit only once per sagaId").isNull();
    }

    @Test
    void hotelThenFlight_emitsChargedAndCompletedOnce() {
        String sagaId = UUID.randomUUID().toString();
        hotelConsumer.accept(buildHotelReserved(sagaId));
        flightConsumer.accept(buildFlightReserved(sagaId));

        SagaEvent charged = billingChargedQueue.poll();
        assertThat(charged).isNotNull();
        assertThat(charged.type()).isEqualTo(SagaEventType.BILLING_CHARGED);
        assertThat(charged.sagaId()).isEqualTo(sagaId);

        SagaEvent completed = sagaCompletedQueue.poll();
        assertThat(completed).isNotNull();
        assertThat(completed.type()).isEqualTo(SagaEventType.SAGA_COMPLETED);
        assertThat(completed.sagaId()).isEqualTo(sagaId);

        assertThat(billingChargedQueue.poll()).isNull();
        assertThat(sagaCompletedQueue.poll()).isNull();
    }

    @Test
    void completesOnlyTheSagaThatHasBothReservations() {
        String sagaA = UUID.randomUUID().toString();
        String sagaB = UUID.randomUUID().toString();

        // Complete only saga A
        flightConsumer.accept(buildFlightReserved(sagaA));
        hotelConsumer.accept(buildHotelReserved(sagaA));

        // For saga B, send only one of them
        flightConsumer.accept(buildFlightReserved(sagaB));

        // Expect events only for saga A
        SagaEvent chargedA = billingChargedQueue.poll();
        SagaEvent completedA = sagaCompletedQueue.poll();
        assertThat(chargedA).isNotNull();
        assertThat(completedA).isNotNull();
        assertThat(chargedA.sagaId()).isEqualTo(sagaA);
        assertThat(completedA.sagaId()).isEqualTo(sagaA);

        assertThat(billingChargedQueue.poll()).as("No more charged events expected").isNull();
        assertThat(sagaCompletedQueue.poll()).as("No more completed events expected").isNull();
    }

    private SagaEvent buildFlightReserved(String sagaId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", UUID.randomUUID().toString());
        return new SagaEvent(null, null, sagaId, SagaEventType.FLIGHT_RESERVED, payload);
    }

    private SagaEvent buildHotelReserved(String sagaId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", UUID.randomUUID().toString());
        return new SagaEvent(null, null, sagaId, SagaEventType.HOTEL_RESERVED, payload);
    }
}
