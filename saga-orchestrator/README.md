# saga-orchestrator

Servicio orquestador que ejecuta la SAGA mediante llamadas REST secuenciales a
flight-service, hotel-service y billing-service. Implementa compensaciones
best‑effort en caso de fallo.

## Endpoint

- POST /api/saga/start
    - Body: AppointmentRequest
    - Flujo:
        1) POST /api/flight/reserve
        2) POST /api/hotel/reserve
        3) POST /api/billing/charge
    - Si ocurre un error, intenta:
        - POST /api/billing/refund?chargeId=...
        - POST /api/hotel/cancel?reservationId=...
        - POST /api/flight/cancel?reservationId=...

## Configuración

- server.port=8080
- Las URLs de los servicios destino están fijas al DNS de Docker Compose:
    - flight-service:8082
    - hotel-service:8083
    - billing-service:8084

## Ejecución

- Normalmente es invocado por appointment-service cuando mode=orchestration.
- También puede llamarse manualmente para pruebas:

```shell
  curl -X POST "http://localhost:8080/api/saga/start" \
       -H "Content-Type: application/json" \
       -d '{"customerId":"c1","fromAirport":"MAD","toAirport":"CDG","flightDate":"2025-12-20","hotelCity":"Paris","nights":2,"amountCents":10000}'
```

## Consideraciones

- Este orquestador es intencionalmente simple y sin persistencia; en entornos
  reales se recomienda almacenar el estado de la SAGA, incluir reintentos y
  timeouts, y contemplar idempotencia y trazabilidad.