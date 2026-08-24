# Data Model: Ciclo de Vida de una Reserva

## Entities

### Table

Represents a physical table in the restaurant.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGINT PK | auto-generated | Internal surrogate key |
| tableNumber | VARCHAR/STRING | NOT NULL | Human-readable table identifier |
| capacity | INT | NOT NULL, > 0 | Maximum diners the table can host |

### Reservation

Represents a reservation request and its final disposition.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGINT PK | auto-generated | Internal surrogate key |
| reservationCode | VARCHAR/STRING | NOT NULL, UNIQUE | Public identifier returned to the customer |
| customerName | VARCHAR/STRING | NOT NULL | Name of the customer |
| customerEmail | VARCHAR/STRING | NOT NULL, valid email format | Customer contact email |
| reservationTime | TIMESTAMP | NOT NULL | Date and time of the reservation (local restaurant time) |
| partySize | INT | NOT NULL, > 0, ≤ 12 | Number of diners |
| status | ENUM | NOT NULL | PENDING, CONFIRMED, REJECTED |
| assignedTableId | BIGINT FK → tables.id | NULLABLE | Assigned table when CONFIRMED |
| createdAt | TIMESTAMP | NOT NULL | When the request was received |

## Relationships

- `Reservation.assignedTableId` → `Table.id` (optional many-to-one)
- A table can have many reservations, but at most one CONFIRMED reservation per `reservationTime` slot.

## State Transitions

```
PENDING (conceptual only, never persisted)
   |
   +---> CONFIRMED (with assignedTableId)
   |
   +---> REJECTED (assignedTableId = null)
```

## Database Constraints

- `UNIQUE(reservation_code)` — guarantees public identifier uniqueness (FR-002/FR-011).
- `UNIQUE(assigned_table_id, reservation_time)` — prevents double booking of the same table in the same slot (FR-008).
- `partySize > 0` — validated at application level.
- `assigned_table_id IS NULL` allowed for REJECTED reservations.

## Indexes

- Unique index on `reservation_code`.
- Unique index on `(assigned_table_id, reservation_time)`.
- Index on `tables(capacity)` to speed up candidate search.
