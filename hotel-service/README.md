# hotel-service

Servicio de reservas de hotel. Participa en:

- Coreografía: consume el evento APPOINTMENT_REQUESTED y publica HOTEL_RESERVED.
- Orquestación: expone endpoints REST para reserva/cancelación usados por el
  orquestador.

## Endpoints (orquestación)

- POST /api/hotel/reserve
    - Body: AppointmentRequest
    - Respuesta: HotelReservation confirmado
- POST /api/hotel/cancel?reservationId=...
    - Cancela de forma best‑effort (mock)

## Kafka (coreografía)

- Consume: topics.appointment-requested (por defecto: appointments.requested)
- Produce: topics.hotel-reserved (por defecto: hotel.reserved)

## Configuración

- server.port=8083
- spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
- Consumer group: hotel-service
- JsonDeserializer con trusted.packages="*" y JsonSerializer sin type headers

## Ejecución y prueba

- Con Docker Compose desde la raíz (ver README principal)
- Ver logs para eventos de reserva de hotel tras iniciar una cita en modo
  coreografía.

## Dependencias clave

- spring-boot-starter-web
- spring-kafka
- common