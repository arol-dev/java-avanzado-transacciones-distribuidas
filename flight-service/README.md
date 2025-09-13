# flight-service

Servicio de reservas de vuelos. Participa en:

- Coreografía: consume el evento **APPOINTMENT_REQUESTED** y publica *
  *FLIGHT_RESERVED**.
- Orquestación: expone endpoints REST para reserva/cancelación usados por el
  orquestador.

## Endpoints (orquestación)

- POST /api/flight/reserve
    - Body: AppointmentRequest
    - Respuesta: FlightReservation confirmado
- POST /api/flight/cancel?reservationId=...
    - Cancela de forma best‑effort (mock)

## Kafka (coreografía)

- Consume: topics.appointment-requested (por defecto: appointments.requested)
- Produce: topics.flight-reserved (por defecto: flight.reserved)

## Configuración

- server.port=8082
- spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
- Consumer group: flight-service
- JsonDeserializer con trusted.packages="*" y JsonSerializer sin type headers

## Ejecución y prueba

- Con Docker Compose desde la raíz (ver README principal)
- Ver logs para eventos de reserva de vuelo tras iniciar una cita en modo
  coreografía.

## Dependencias clave

- spring-boot-starter-web
- spring-kafka
- common