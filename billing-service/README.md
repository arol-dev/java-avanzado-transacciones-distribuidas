# billing-service

Servicio de cobros. Participa principalmente en la coreografía como agregador y
finalizador de la SAGA.

## Rol en coreografía

- Escucha **FLIGHT_RESERVED** y **HOTEL_RESERVED** (tópicos configurables) y
  mantiene estado en memoria para cada sagaId.
- Cuando ambas reservas están confirmadas, publica:
    - BILLING_CHARGED (tópico billing.charged)
    - SAGA_COMPLETED (tópico saga.completed)

## Endpoints (orquestación)

- POST /api/billing/charge
    - Body: AppointmentRequest
    - Respuesta: BillingCharge (charged=true)
- POST /api/billing/refund?chargeId=...
    - Intenta reembolsar (mock)

## Kafka (coreografía)

- Consume:
    - topics.flight-reserved (por defecto: flight.reserved)
    - topics.hotel-reserved (por defecto: hotel.reserved)
- Produce:
    - topics.billing-charged (por defecto: billing.charged)
    - topics.saga-completed (por defecto: saga.completed)

## Configuración

- server.port=8084
- spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
- Consumer group: billing-service
- JsonDeserializer con trusted.packages="*" y JsonSerializer sin type headers

## Ejecución y prueba

- Con Docker Compose desde la raíz (ver README principal)
- Inicie una cita en modo coreografía y verifique en logs de billing-service
  cuando detecta ambas reservas y publica la finalización de la SAGA.

## Dependencias clave

- spring-boot-starter-web
- spring-kafka
- common