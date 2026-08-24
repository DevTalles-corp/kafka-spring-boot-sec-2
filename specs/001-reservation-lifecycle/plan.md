# Implementation Plan: Ciclo de Vida de una Reserva

**Branch**: `001-reservation-lifecycle` | **Date**: 2026-08-18 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-reservation-lifecycle/spec.md`

## Summary

El sistema debe aceptar solicitudes de reserva (nombre, email, fecha/hora, comensales), evaluar disponibilidad en el mismo flujo buscando una mesa libre con capacidad suficiente para la franja solicitada, y resolver de forma inmediata y terminal: CONFIRMED (asignando mesa) o REJECTED (sin mesa). Toda reserva nace PENDING y termina en uno de esos dos estados. El cliente puede consultar el estado de su reserva por su código. Se expone como API REST Spring Boot (monolito modular), con validación Bean Validation, mapeo MapStruct, manejo global de excepciones con `@RestControllerAdvice` + `ProblemDetail` (RFC 7807), y garantía de no doble asignación de mesa por franja vía bloqueo pesimista + constraint de unicidad en BD.

## Technical Context

**Language/Version**: Java 21 (LTS)

**Primary Dependencies**: Spring Boot 4.1.x, `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, MapStruct + Lombok (procesador Lombok declarado antes que MapStruct), springdoc-openapi

> **Stack verificado**: Al momento de implementar (2026-08-18), la última versión estable de Spring Boot publicada en Maven Central es **4.1.0**. Se implementa con `spring-boot-starter-parent` 4.1.0 y el starter `spring-boot-starter-webmvc`; ver research.md R1/R2.

**Storage**: H2 in-memory (dev/test, modo PGSQL) + PostgreSQL 16 + Flyway (prod)

**Testing**: JUnit 5, Spring Boot Test, AssertJ; test de concurrencia con solicitudes simultáneas (Testcontainers como alternativa documentada)

**Target Platform**: JVM server (REST API)

**Project Type**: Web service (monolito modular, único artefacto desplegable)

**Performance Goals**: Sin SLA duro; respuesta inmediata y síncrona por solicitud (flujo único). Escala de un único restaurante.

**Constraints**:
- Estructura de paquetes DENTRO de cada módulo: exactamente `controller`, `service`, `repository` y `model` (enmienda de la constitución, Principio I, versión 1.3.0).
- `controller` → adaptadores HTTP, DTOs request/response y mappers MapStruct; `service` → lógica de negocio y orquestación; `repository` → interfaces Spring Data; `model` → entidades JPA y enums.
- Prohibido `domain`, `application`, `api`, `infrastructure` y capas hexagonales.
- Módulos como sub-packages (`reservations`, `tables`), cada uno con su dominio, repositorio e interfaces públicas.
- DTOs para requests/responses; nunca exponer entidades JPA.
- Endpoints versionados bajo `/api/v1/`.
- Rechazo por falta de mesa = resultado exitoso (HTTP 200), no error del sistema.
- No doble asignación de mesa en la misma franja (FR-008) garantizado por bloqueo pesimista + constraint a nivel BD.
- Validación de entrada: `partySize` máximo 12 (FR-012); solicitudes con `partySize > 12` devuelven HTTP 400 sin crear reserva ni evaluar disponibilidad.

**Scale/Scope**: Un restaurante; volumen bajo; sin autenticación.

## Constitution Check

*GATE: Debe pasar antes de Phase 0. Se re-evalúa tras Phase 1.*

| Principio constitucional | Gate | Estado |
|---------------------------|------|--------|
| I. Calidad del Código | SOLID + Clean Code, capas por módulo | ✅ cumplido por diseño |
| I. Calidad del Código (enmendado, v1.3.0) | Paquetes dentro de cada módulo: exactamente `controller`, `service`, `repository` y `model`; NO usar `domain`, `application`, `api`, `infrastructure` ni capas hexagonales | ✅ cumplido por diseño (ver Project Structure) |
| I. Calidad del Código | Excepciones globales con `@RestControllerAdvice` + `ProblemDetail` (RFC 7807) | ✅ cumplido por diseño |
| I. Calidad del Código | Validación de entrada con Bean Validation en DTOs | ✅ cumplido por diseño |
| II. Arquitectura | Monolito modular, único artefacto, sub-packages (`reservations`, `tables`) | ✅ cumplido por diseño |
| II. Arquitectura | Módulos con dominio, repositorio e interfaces públicas propias | ✅ cumplido por diseño |
| I. Convenciones de Código | Identificadores en inglés; logs/mensajes/excepciones/textos al cliente en español | ✅ cumplido por diseño |
| I. Convenciones de Código | Endpoints bajo `/api/v1/` | ✅ cumplido por diseño |
| I. Convenciones de Código | DTOs para requests/responses, nunca entidades JPA; mapeo con MapStruct | ✅ cumplido por diseño |
| II. Convenciones de Repositorio | README.md con instrucciones de arranque | ✅ planificado (fuera de esta feature, en setup del proyecto) |
| II. Convenciones de Repositorio | Commits `clase-NN-slug` | ✅ se aplica en implementación |

**Evaluación de gates**: Sin violaciones. No se requiere Complexity Tracking.

**Nota de gobernanza (enmienda)**: La constitución fue enmendada dos veces (paquetes `controller`/`service`/`repository` y luego incorporando `model`), quedando en **1.3.0**. El cumplimiento se evalúa contra el texto vigente: dentro de cada módulo solo existen `controller`, `service`, `repository` y `model` (ver Project Structure).

*Re-evaluación tras Phase 1 (post-diseño)*: los gates se mantienen cumplidos. La estructura de paquetes del plan (ver Project Structure) respeta el mandato: entidades y enums en `model`, DTOs y mappers en `controller`, interfaces Spring Data en `repository`, lógica en `service`. La decisión R3 (advice propio + ProblemDetail) y R4 (bloqueo pesimista + constraint único) respetan los principios I y II; no introducen complejidad injustificada.

## Project Structure

### Documentación (esta feature)

```text
specs/001-reservation-lifecycle/
├── plan.md              # Este archivo
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1
└── tasks.md             # Phase 2 (no se crea en /speckit.plan)
```

### Código fuente (raíz del repositorio)

```text
pom.xml
src/
├── main/
│   ├── java/com/bistro/
│   │   ├── BistroApplication.java
│   │   ├── reservations/
│   │   │   ├── controller/
│   │   │   │   ├── ReservationController.java
│   │   │   │   ├── ReservationRequest.java            # DTO de entrada (+ Bean Validation)
│   │   │   │   ├── ReservationResponse.java           # DTO de salida (creación)
│   │   │   │   ├── ReservationStatusResponse.java     # DTO de salida (consulta de estado)
│   │   │   │   └── ReservationMapper.java             # MapStruct entity ↔ DTO
│   │   │   ├── service/
│   │   │   │   ├── ReservationService.java            # orquesta disponibilidad + creación
│   │   │   │   └── ReservationNotFoundException.java  # 404 en consulta de estado
│   │   │   ├── repository/
│   │   │   │   └── ReservationRepository.java         # interfaz Spring Data
│   │   │   └── model/
│   │   │       ├── Reservation.java                   # entidad JPA
│   │   │       └── ReservationStatus.java             # enum PENDING/CONFIRMED/REJECTED
│   │   ├── tables/
│   │   │   ├── service/
│   │   │   │   └── TableService.java                  # interfaz pública del módulo para reservas
│   │   │   ├── repository/
│   │   │   │   └── TableRepository.java               # queries de capacidad + lock pesimista
│   │   │   └── model/
│   │   │       └── Table.java                         # entidad JPA
│   │   ├── shared/
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java        # @RestControllerAdvice → ProblemDetail
│   │   │   │   └── ProblemDetailConfig.java
│   │   │   └── config/
│   │   │       └── OpenApiConfig.java                 # título/descripción de la API
│   └── resources/
│       ├── application.yml
│       ├── db/migration/                              # Flyway
│       └── data.sql                                    # seed de mesas (dev)
└── test/
    └── java/com/bistro/
        ├── reservations/
        │   ├── service/ReservationServiceTest.java
        │   ├── service/ReservationConcurrencyTest.java
        │   └── controller/ReservationControllerTest.java
        └── BistroApplicationTest.java
```

**Structure Decision**: Monolito modular según la Constitución (enmendada, v1.3.0): un único módulo de build **Maven** (`pom.xml`, Java 21, Spring Boot 4.1.0) con sub-packages de dominio (`reservations`, `tables`) y un paquete transversal `shared` (cross-cutting, no es un módulo de dominio) para excepciones/config. Dentro de cada módulo SOLO existen `controller`, `service`, `repository` y `model`:
- Entidades JPA y enums → `model`.
- DTOs, mappers MapStruct y controladores → `controller`.
- Interfaces Spring Data → `repository`.
- Lógica de negocio → `service`.

El módulo `tables` en esta feature es solo de consulta (capacidad + lock pesimista) y expone su `TableService` como interfaz pública para el módulo `reservations`; su gestión (CRUD) se asigna a otra feature. La concurrencia de asignación usa bloqueo pesimista sobre la mesa + constraint único como backstop (research.md R4). Paquete raíz `com.bistro`.

## Complexity Tracking

> No aplica: sin violaciones constitucionales (ver Constitution Check).
