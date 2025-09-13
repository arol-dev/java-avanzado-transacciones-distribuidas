# common

Módulo con tipos compartidos entre servicios:

- DTOs (records Java): AppointmentRequest, FlightReservation, HotelReservation,
  BillingCharge
- Eventos SAGA: SagaEvent y SagaEventType

## Detalles técnicos

- Se usan records de Java 21 para inmutabilidad y serialización directa con
  Jackson.
- SagaEvent define un constructor compacto que autogenera eventId (UUID) y
  timestamp (Instant.now) cuando vienen nulos, facilitando la publicación desde
  servicios.
- La serialización JSON en Kafka desactiva los type headers (
  spring.json.add.type.headers=false); por eso, los consumidores confían en
  trusted.packages="*" y castea el payload.

## Dependencias clave

- spring-boot-starter (infra mínima)
- jackson-databind (JSON)
- jakarta.validation-api (anotaciones de validación en AppointmentRequest)

## Uso

Agregar como dependencia en otros módulos del proyecto (ya configurado en los
poms de cada servicio).