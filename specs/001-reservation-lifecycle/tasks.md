# Tasks: Ciclo de Vida de una Reserva

**Input**: Design documents from `/specs/001-reservation-lifecycle/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are included because the feature specification defines mandatory user scenarios with independent test criteria.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create Maven project skeleton in `pom.xml` (Java 21, Spring Boot 4.1.0, starters webmvc/data-jpa/validation/flyway, MapStruct, Lombok, H2, PostgreSQL, springdoc-openapi)
- [ ] T002 [P] Create main application class in `src/main/java/com/bistro/BistroApplication.java`
- [ ] T003 [P] Configure `src/main/resources/application.yml` with dev (H2) and prod (PostgreSQL) profiles
- [ ] T004 [P] Configure springdoc OpenAPI metadata in `src/main/java/com/bistro/shared/config/OpenApiConfig.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 Create global exception handler in `src/main/java/com/bistro/shared/exception/GlobalExceptionHandler.java` (`@RestControllerAdvice` → `ProblemDetail`)
- [ ] T006 [P] Create `ProblemDetail` configuration in `src/main/java/com/bistro/shared/exception/ProblemDetailConfig.java`
- [ ] T007 Create Flyway baseline migration in `src/main/resources/db/migration/V1__init.sql` (tables and reservations schema with unique constraints)
- [ ] T008 [P] Create sample table seed data in `src/main/resources/data.sql` (capacities 2, 4, 6, 8)
- [ ] T009 Create `Table` entity in `src/main/java/com/bistro/tables/model/Table.java`
- [ ] T010 Create `TableRepository` with pessimistic lock query in `src/main/java/com/bistro/tables/repository/TableRepository.java`
- [ ] T011 Create public `TableService` interface in `src/main/java/com/bistro/tables/service/TableService.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - El cliente solicita una reserva (Priority: P1) 🎯 MVP

**Goal**: Allow customers to submit a reservation request and receive an immediate terminal response: CONFIRMED (with assigned table) or REJECTED (no table available).

**Independent Test**: Send `POST /api/v1/reservations` with a valid payload and verify the response contains a unique `reservationCode` and a final status of `CONFIRMED` or `REJECTED`.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T012 [P] [US1] Write contract/controller test for `POST /api/v1/reservations` in `src/test/java/com/bistro/reservations/controller/ReservationControllerTest.java`
- [ ] T013 [P] [US1] Write service unit tests for reservation creation (confirmed, rejected, validation) in `src/test/java/com/bistro/reservations/service/ReservationServiceTest.java`
- [ ] T014 [US1] Write concurrency integration test for double-booking prevention in `src/test/java/com/bistro/reservations/service/ReservationConcurrencyTest.java`

### Implementation for User Story 1

- [ ] T015 [P] [US1] Create `ReservationStatus` enum in `src/main/java/com/bistro/reservations/model/ReservationStatus.java`
- [ ] T016 [P] [US1] Create `Reservation` entity in `src/main/java/com/bistro/reservations/model/Reservation.java`
- [ ] T017 [P] [US1] Create `ReservationRepository` in `src/main/java/com/bistro/reservations/repository/ReservationRepository.java`
- [ ] T018 [P] [US1] Create `ReservationRequest` DTO with Bean Validation in `src/main/java/com/bistro/reservations/controller/ReservationRequest.java`
- [ ] T019 [P] [US1] Create `ReservationResponse` DTO in `src/main/java/com/bistro/reservations/controller/ReservationResponse.java`
- [ ] T020 [P] [US1] Create `ReservationMapper` (MapStruct) in `src/main/java/com/bistro/reservations/controller/ReservationMapper.java`
- [ ] T021 [US1] Implement `ReservationService` in `src/main/java/com/bistro/reservations/service/ReservationService.java` (depends on T015-T017, T009-T011)
- [ ] T022 [US1] Implement `POST /api/v1/reservations` endpoint in `src/main/java/com/bistro/reservations/controller/ReservationController.java` (depends on T018-T021)

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - El cliente consulta el estado de su reserva (Priority: P2)

**Goal**: Allow customers to query the current status of their reservation using the reservation code.

**Independent Test**: Create a reservation and then call `GET /api/v1/reservations/{reservationCode}`; verify the returned status matches the created reservation. A non-existing code returns HTTP 404.

### Tests for User Story 2 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T023 [P] [US2] Write controller test for `GET /api/v1/reservations/{reservationCode}` in `src/test/java/com/bistro/reservations/controller/ReservationControllerTest.java`

### Implementation for User Story 2

- [ ] T024 [P] [US2] Create `ReservationStatusResponse` DTO in `src/main/java/com/bistro/reservations/controller/ReservationStatusResponse.java`
- [ ] T025 [P] [US2] Create `ReservationNotFoundException` in `src/main/java/com/bistro/reservations/service/ReservationNotFoundException.java`
- [ ] T026 [US2] Add status query method to `ReservationService` in `src/main/java/com/bistro/reservations/service/ReservationService.java`
- [ ] T027 [US2] Implement `GET /api/v1/reservations/{reservationCode}` endpoint in `src/main/java/com/bistro/reservations/controller/ReservationController.java`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T028 [P] Run quickstart validation scenarios S1-S6 from `quickstart.md`
- [ ] T029 [P] Verify constitution compliance (package structure, DTOs, `@RestControllerAdvice` + `ProblemDetail`, `/api/v1/` versioning)
- [ ] T030 [P] Code cleanup and final review

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Conceptually depends on US1 existing, but its endpoint is independently testable once a reservation exists; for isolated testing, seed data or helper setup can create the prerequisite reservation

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, both user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models and DTOs within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Write contract/controller test for POST /api/v1/reservations in src/test/java/com/bistro/reservations/controller/ReservationControllerTest.java"
Task: "Write service unit tests for reservation creation in src/test/java/com/bistro/reservations/service/ReservationServiceTest.java"

# Launch all models/DTOs for User Story 1 together:
Task: "Create ReservationStatus enum in src/main/java/com/bistro/reservations/model/ReservationStatus.java"
Task: "Create Reservation entity in src/main/java/com/bistro/reservations/model/Reservation.java"
Task: "Create ReservationRequest DTO in src/main/java/com/bistro/reservations/controller/ReservationRequest.java"
Task: "Create ReservationResponse DTO in src/main/java/com/bistro/reservations/controller/ReservationResponse.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently (quickstart S1-S3b and S6)
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence

---

## Phase 6: Convergence

- [x] T031 Add `@Max(12)` validation to `partySize` in `src/main/java/com/bistro/reservations/controller/ReservationRequest.java` per FR-012 (missing)
- [ ] T032 Add `CHECK (party_size <= 12)` to the reservations table in `src/main/resources/db/migration/V1__init.sql` per FR-012 (missing)
- [x] T033 Add test for `partySize > 12` returning HTTP 400 in `src/test/java/com/bistro/reservations/controller/ReservationCreationControllerTest.java` per US1/AC5 (missing)
