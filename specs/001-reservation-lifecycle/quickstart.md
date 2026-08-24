# Quickstart: Ciclo de Vida de una Reserva

## Prerequisites

- JDK 21
- Maven 3.9+
- (Optional) Docker + Docker Compose for PostgreSQL 16 profile

## Run the Application (dev profile)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The dev profile starts the application on H2 in-memory database with pre-loaded sample tables (capacities 2, 4, 6, 8).

## Run Tests

```bash
mvn test
```

## Validation Scenarios

### S1 — Create a reservation that gets CONFIRMED

```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H 'Content-Type: application/json' \
  -d '{
    "customerName": "Ana García",
    "customerEmail": "ana@example.com",
    "reservationTime": "2026-08-20T19:30:00",
    "partySize": 4
  }'
```

Expected: HTTP 200 with `status: CONFIRMED` and `assignedTableId` set.

### S2 — Create a reservation that gets REJECTED (no table available)

Send multiple reservations for the same slot until all tables are exhausted, or request a `partySize` larger than the biggest table (e.g. 10).

```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H 'Content-Type: application/json' \
  -d '{
    "customerName": "Carlos López",
    "customerEmail": "carlos@example.com",
    "reservationTime": "2026-08-20T20:00:00",
    "partySize": 10
  }'
```

Expected: HTTP 200 with `status: REJECTED` and `assignedTableId: null`.

### S3 — Invalid request returns 400

```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H 'Content-Type: application/json' \
  -d '{
    "customerName": "",
    "customerEmail": "not-an-email",
    "reservationTime": null,
    "partySize": 0
  }'
```

Expected: HTTP 400 `application/problem+json` with validation errors array.

### S3b — partySize greater than 12 returns 400

FR-012: the system must reject any request whose partySize exceeds 12 as an input-validation error, without creating a reservation or evaluating table availability.

```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H 'Content-Type: application/json' \
  -d '{
    "customerName": "Marta Ruiz",
    "customerEmail": "marta@example.com",
    "reservationTime": "2026-08-20T20:00:00",
    "partySize": 13
  }'
```

Expected: HTTP 400 `application/problem+json` with a validation error indicating that `partySize` must be at most 12. No reservation is created.

### S4 — Get status of an existing reservation

Use the `reservationCode` returned by S1:

```bash
curl http://localhost:8080/api/v1/reservations/RES-20260818-A1B2C3
```

Expected: HTTP 200 with status matching the created reservation.

### S5 — Get status of a non-existing reservation returns 404

```bash
curl http://localhost:8080/api/v1/reservations/RES-NONEXISTENT
```

Expected: HTTP 404 `application/problem+json`.

### S6 — Concurrency: no double booking

Run the concurrency integration test:

```bash
mvn test -Dtest=ReservationConcurrencyTest
```

The test fires N simultaneous requests for the same table+slot. Expected: exactly one reservation ends with `status: CONFIRMED`; the rest are `REJECTED` or assigned to a different table.

## Production Profile

```bash
# Start PostgreSQL 16 (example docker-compose not included in this feature)
docker run -d --name bistro-postgres -e POSTGRES_DB=bistro -e POSTGRES_USER=bistro -e POSTGRES_PASSWORD=bistro -p 5432:5432 postgres:16

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
