# appointment-service

Servicio frontal para iniciar una SAGA de cita de viaje. Expone un endpoint HTTP
para comenzar el flujo en modo coreografía (Kafka) u orquestación (REST via
saga-orchestrator).

## Endpoints

```http request
POST /api/appointments?mode=choreography HTTP/1.1
Host: localhost:8081
Content-Type: application/json

{
      "customerId":"c1",
      "fromAirport":"MAD",
      "toAirport":"CDG",
      "flightDate":"2025-12-20",
      "hotelCity":"Paris",
      "nights":2,
      "amountCents":10000
    }
```

Respuesta: 202 con id de SAGA iniciada.

## Coreografía

- Publica evento **SagaEvent(APPOINTMENT_REQUESTED)** en el tópico
  `appointments.requested`.
- Tópico configurable vía application.yml: topics.appointment-requested (por
  defecto: appointments.requested).

## Orquestación

- Si mode=orchestration, invoca
  POST http://saga-orchestrator:8080/api/saga/start enviando el mismo cuerpo.
- Base URL configurable con orchestrator.url (por
  defecto: http://saga-orchestrator:8080).

## Configuración relevante

- server.port=8081
- spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
- JsonSerializer sin type headers para compatibilidad con los demás servicios.

## Ejecución

- Local :

```shell
mvn -DskipTests spring-boot:run
```

- Prueba rápida:

```shell
  curl -X POST "http://localhost:8081/api/appointments?mode=choreography" \
       -H "Content-Type: application/json" \
       -d '{"customerId":"c1","fromAirport":"MAD","toAirport":"CDG","flightDate":"2025-12-20","hotelCity":"Paris","nights":2,"amountCents":10000}'
```

## Dependencias clave

- spring-boot-starter-web
- spring-kafka
- common (DTOs y eventos)