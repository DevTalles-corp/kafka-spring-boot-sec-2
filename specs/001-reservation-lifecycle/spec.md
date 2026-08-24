# Feature Specification: Ciclo de Vida de una Reserva

**Feature Branch**: `001-reservation-lifecycle`

**Created**: 2026-08-14

**Status**: Draft

**Input**: User description: "Feature: ciclo de vida de una reserva en bistro. El cliente solicita una mesa para una franja horaria; el sistema verifica disponibilidad con capacidad suficiente y decide en el mismo flujo: asigna y confirma (CONFIRMED) o rechaza (REJECTED). Consulta de estado por código de reserva."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - El cliente solicita una reserva (Priority: P1)

El cliente envía una solicitud de reserva indicando su nombre, su email, la fecha y hora de la reserva, y la cantidad de comensales. El sistema evalúa en el mismo flujo si existe una mesa libre con capacidad suficiente para esa franja horaria: si la hay, la asigna y confirma la reserva; si no la hay, la rechaza. En ambos casos el resultado es inmediato y terminal: la respuesta incluye un código de reserva y el estado final.

**Why this priority**: Es la operación central de la feature y la que aporta el valor de negocio: sin creación de reservas no existe el resto del flujo.

**Independent Test**: Puede probarse de forma completa enviando una solicitud de reserva y verificando que se recibe un código de reserva con un estado final (CONFIRMED o REJECTED) de manera inmediata.

**Acceptance Scenarios**:

1. **Given** que existe una mesa libre con capacidad igual o mayor a la cantidad de comensales en la franja solicitada, **When** el cliente envía una solicitud de reserva válida, **Then** la reserva queda CONFIRMED y se le asigna esa mesa.
2. **Given** que no existe ninguna mesa libre con capacidad suficiente en la franja solicitada, **When** el cliente envía una solicitud de reserva válida, **Then** la reserva queda REJECTED y la respuesta informa el rechazo como resultado exitoso de la operación, no como un error del sistema.
3. **Given** una solicitud de reserva válida, **When** el sistema la procesa, **Then** la reserva nace en PENDING y termina en CONFIRMED o REJECTED.
4. **Given** una solicitud con datos inválidos (email sin formato válido, cantidad de comensales no positiva, fecha u hora ausente o inválida), **When** el cliente la envía, **Then** el sistema la rechaza por validación y no crea la reserva.
5. **Given** una solicitud cuya cantidad de comensales supera las 12 personas, **When** el cliente la envía, **Then** el sistema la rechaza por validación de entrada y no crea la reserva ni evalúa disponibilidad de mesas.

---

### User Story 2 - El cliente consulta el estado de su reserva (Priority: P2)

El cliente consulta el estado actual de su reserva usando el código que recibió al crearla. El sistema devuelve el estado vigente de esa reserva.

**Why this priority**: Es el segundo caso de uso explícito de la feature y cierra el flujo del cliente; depende de que la creación de reservas ya exista.

**Independent Test**: Puede probarse creando una reserva y luego consultando su estado mediante el código, verificando que el estado devuelto coincide con el estado final de la reserva.

**Acceptance Scenarios**:

1. **Given** que existe una reserva creada, **When** el cliente consulta su estado con el código de reserva, **Then** el sistema devuelve el estado actual (CONFIRMED o REJECTED).
2. **Given** que el cliente consulta con un código de reserva inexistente, **When** realiza la consulta, **Then** el sistema informa que la reserva no existe.

---

### Edge Cases

- ¿Qué ocurre cuando la cantidad de comensales supera la capacidad de todas las mesas del restaurante? → La reserva se rechaza por negocio (REJECTED); no es un error del sistema.
- ¿Qué ocurre cuando hay una mesa libre pero con capacidad menor a la cantidad de comensales? → La reserva se rechaza (REJECTED).
- ¿Cómo se evita que una mesa ya asignada a una reserva en una franja se asigne a otra reserva en la misma franja? → Esa mesa no debe considerarse disponible para la misma franja.
- ¿Qué ocurre si dos clientes solicitan la misma franja y la misma mesa al mismo tiempo? → Solo una de las solicitudes puede confirmarse con esa mesa; la otra debe resolverse con otra mesa o rechazarse.
- ¿Qué ocurre cuando el email no tiene un formato válido? → La solicitud se rechaza por validación y no se crea la reserva.
- ¿Qué ocurre cuando se consulta una reserva con un código inexistente? → El sistema informa que la reserva no existe.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE aceptar una solicitud de reserva que incluya el nombre del cliente, su email, la fecha y hora de la reserva, y la cantidad de comensales.
- **FR-002**: El sistema DEBE responder a cada solicitud de reserva con un código de reserva único y el estado resultante.
- **FR-003**: El sistema DEBE evaluar la disponibilidad en el momento de la creación de la reserva, buscando una mesa libre con capacidad igual o mayor a la cantidad de comensales para la franja solicitada.
- **FR-004**: Si existe una mesa disponible, el sistema DEBE asignarla a la reserva y dejar la reserva en estado CONFIRMED.
- **FR-005**: Si no existe ninguna mesa disponible con capacidad suficiente, el sistema DEBE dejar la reserva en estado REJECTED e informar el rechazo como resultado exitoso de la operación (NO como un error del sistema).
- **FR-006**: Toda reserva DEBE nacer en estado PENDING y DEBE terminar en CONFIRMED o REJECTED. No debe existir ningún otro estado.
- **FR-007**: Una reserva SOLO puede quedar en CONFIRMED si se le asignó una mesa.
- **FR-008**: Una mesa ya asignada a una reserva en una franja horaria NO DEBE poder asignarse a otra reserva en la misma franja.
- **FR-009**: El cliente DEBE poder consultar el estado actual de su reserva utilizando únicamente el código de reserva.
- **FR-010**: El sistema DEBE validar los datos de entrada antes de crear la reserva: email con formato válido, cantidad de comensales mayor a cero, y fecha y hora presentes y válidas.
- **FR-011**: El sistema DEBE garantizar que el código de reserva sea único entre todas las reservas.
- **FR-012**: El sistema DEBE rechazar por validación de entrada toda solicitud cuya cantidad de comensales supere las 12 personas, sin crear la reserva ni evaluar disponibilidad de mesas. Este límite es una validación de entrada (solicitud inválida), distinta del rechazo de negocio (REJECTED): el REJECTED aplica a solicitudes válidas para las que no hay mesa disponible.

### Key Entities *(include if feature involves data)*

- **Reservation**: Representa la solicitud de una mesa por parte de un cliente. Atributos: reservationCode (identificador público de la reserva), customerName, customerEmail, reservationTime (fecha y hora de la reserva), partySize (cantidad de comensales), status (PENDING, CONFIRMED, REJECTED), assignedTableId (mesa asignada, si la hay).
- **Table**: Representa una mesa del restaurante con una capacidad determinada. En esta feature se consulta su disponibilidad y capacidad, pero no se gestiona su creación ni edición.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las solicitudes de reserva válidas reciben una respuesta inmediata con un código de reserva y un estado final (CONFIRMED o REJECTED).
- **SC-002**: La decisión de disponibilidad es correcta y consistente con el estado de las mesas: nunca se confirma una reserva sin mesa asignada.
- **SC-003**: Una mesa asignada a una reserva en una franja horaria nunca se asigna a otra reserva en la misma franja.
- **SC-004**: El 100% de los rechazos por falta de mesa disponible se informan como resultados exitosos de la operación, no como errores del sistema.
- **SC-005**: El cliente puede conocer el estado de su reserva consultando con el código de reserva, y el estado devuelto coincide con el estado final de la reserva.

## Assumptions

- La franja horaria se identifica por la combinación de fecha y hora de la reserva. La duración de la franja no está definida en esta feature; se asume que una mesa queda ocupada para esa franja concreta (fecha + hora) y no se modela una duración.
- El restaurante ya cuenta con un conjunto de mesas cargado en el sistema (con su capacidad), provisto por otra feature de gestión de mesas. Esta feature solo consulta las mesas existentes.
- La hora de la reserva se interpreta en la zona horaria local del restaurante.
- El sistema corresponde a un único restaurante, no a una plataforma multi-local.
- No existe autenticación: cualquier cliente puede crear una reserva sin cuenta de usuario.
- En esta etapa no se definen métricas de negocio (satisfacción, tiempos, concurrencia).