# Phase-Wise Implementation Plan: Movie Ticket Booking System

## Purpose

This plan implements the provided HLD and LLD as a Spring Boot modular monolith. It preserves the stated technology and scope: Spring Boot, PostgreSQL, Flyway, Spring Security, asynchronous notifications, scheduled hold expiry, and unit/integration testing. It does not introduce a frontend, microservices, Docker, CI/CD, OAuth/SSO/MFA, a real payment gateway, or production observability.

Every phase has verification cases. Complete a phase only after its listed checks pass.

---

## Phase 1 - Application Foundation and Shared Behavior

### Objective

Establish the Spring Boot structure, PostgreSQL/Flyway configuration, shared enums, and consistent API error handling.

### Key Tasks

- Create the packages from the LLD: `common`, `config`, `user`, `city`, `theater`, `movie`, `show`, `booking`, `pricing`, `payment`, `refund`, and `notification`.
- Configure PostgreSQL, JPA schema validation, and Flyway in `application.properties`.
- Create the Flyway migration folder and migration naming convention.
- Enable scheduling and asynchronous execution.
- Implement `Role`, `SeatType`, `ShowSeatStatus`, `BookingStatus`, `PaymentStatus`, and `DiscountType` enums.
- Implement the common error-response DTO.
- Implement `@RestControllerAdvice` for request validation, missing resources, denied access, and business conflicts.

### Dependencies

- Spring Boot project and PostgreSQL database must exist.

### Deliverable

The application starts, connects to PostgreSQL, and exposes a consistent error format.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P1-T1 | Start the application with valid database settings. | Application starts and Flyway reports success. |
| P1-T2 | Start the application with invalid database credentials. | Startup fails with a clear datasource error. |
| P1-T3 | Send a request with an invalid DTO field. | `400 Bad Request` follows the common error contract. |
| P1-T4 | Request a non-existent endpoint. | A clear `404` response is returned. |

---

## Phase 2 - Users, Roles, and Security

### Objective

Create users and enforce the `ADMIN` and `CUSTOMER` role model.

### Key Tasks

- Add the `users` Flyway migration with unique email and role constraints.
- Implement `User` entity and repository.
- Configure Spring Security using basic authentication, as allowed by the HLD.
- Seed or otherwise create one admin and one customer user for testing.
- Restrict admin endpoints to `ADMIN`.
- Obtain the authenticated user in services; do not accept customer identity from request bodies.

### Dependencies

- Phase 1.

### Deliverable

Authenticated users can access only the endpoints allowed by their role.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P2-T1 | Call a protected endpoint without credentials. | `401 Unauthorized`. |
| P2-T2 | Customer calls an admin endpoint. | `403 Forbidden`. |
| P2-T3 | Admin calls an admin endpoint. | Request proceeds to the controller/service. |
| P2-T4 | Attempt to create duplicate user email. | Database/application rejects the duplicate. |

---

## Phase 3 - City, Theater, Auditorium, and Seat Inventory

### Objective

Implement the admin-managed physical theater hierarchy and seat layout.

### Key Tasks

- Add migrations for `cities`, `theaters`, `auditoriums`, and `seats`.
- Add `uq_seat_position` for `(auditorium_id, row_label, seat_number)`.
- Implement entities, repositories, services, DTOs, and controllers.
- Implement admin APIs for city, theater, auditorium, and seat creation.
- Validate each parent-child relationship.

### Dependencies

- Phases 1 and 2.

### Deliverable

An admin can create cities, theaters, auditoriums, and physical seat layouts through REST APIs.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P3-T1 | Admin creates a city. | City is persisted and returned successfully. |
| P3-T2 | Admin creates theater with valid city ID. | Theater is persisted under that city. |
| P3-T3 | Admin creates theater with unknown city ID. | `404 Not Found` or defined validation error. |
| P3-T4 | Admin adds a physical seat. | Seat is persisted with correct auditorium and type. |
| P3-T5 | Add same row/number twice in one auditorium. | Unique constraint prevents duplicate seat. |
| P3-T6 | Customer attempts inventory creation. | `403 Forbidden`. |

---

## Phase 4 - Movies, Shows, and ShowSeat Creation

### Objective

Create screenings and produce a per-show availability record for every physical seat.

### Key Tasks

- Add migrations for `movies`, `shows`, and `show_seats`.
- Add unique constraint `(show_id, seat_id)` and hold-expiry index `(status, hold_expiry)`.
- Implement `Movie`, `Show`, and `ShowSeat` entities/repositories/services.
- Implement admin APIs to create movies and shows.
- In `ShowService.createShow`, load auditorium seats and create one `AVAILABLE` `ShowSeat` per seat in one transaction.

### Dependencies

- Phase 3.

### Deliverable

An admin can create shows, with a complete independent `ShowSeat` inventory generated for each show.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P4-T1 | Create a movie with valid fields. | Movie persists successfully. |
| P4-T2 | Create a show for an auditorium with 20 seats. | Exactly 20 `ShowSeat` records are created. |
| P4-T3 | Inspect generated ShowSeats. | Every record starts as `AVAILABLE`. |
| P4-T4 | Create show with unknown movie/auditorium. | Request fails without partial records. |
| P4-T5 | Attempt duplicate ShowSeat for same show/seat. | Database unique constraint rejects it. |

---

## Phase 5 - Show Discovery and Seat Map

### Objective

Allow customers to browse shows and inspect show-specific seat availability.

### Key Tasks

- Implement `GET /api/v1/cities`.
- Implement `GET /api/v1/shows?cityId={id}&date={date}`.
- Implement `GET /api/v1/shows/{id}/seats`.
- Create response DTOs containing show information, physical seat details, and `ShowSeat` status.
- Query availability from `ShowSeat`, never directly from `Seat`.

### Dependencies

- Phase 4.

### Deliverable

Customers can browse shows by city/date and obtain a seat map for a selected show.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P5-T1 | Request cities. | Persisted cities are returned. |
| P5-T2 | Request shows for city/date. | Only matching shows are returned. |
| P5-T3 | Request a show seat map. | Each physical seat appears with its `ShowSeat` status. |
| P5-T4 | Request unknown show seat map. | `404 Not Found`. |
| P5-T5 | One show has a held/booked seat. | Another show's same physical seat remains independently available. |

---

## Phase 6 - Pricing, Discount Codes, and Refund Policies

### Objective

Implement configurable price, discount, and refund calculations that booking/cancellation flows will use.

### Key Tasks

- Add migrations for `discount_codes` and `refund_policies`.
- Implement their entities, repositories, services, DTOs, and admin APIs.
- Implement price calculation from base price, premium-seat pricing, and weekend pricing.
- Implement fixed-amount and percentage discounts.
- Validate active state and valid date range for a discount code.
- Select the applicable refund policy from `minimumHoursBeforeShow`.
- Unit-test pricing, discount validation, policy selection, and refund calculation independently.

### Dependencies

- Phases 2 and 4.

### Deliverable

Admins can configure discount/refund rules; services can calculate an accurate final price and refund.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P6-T1 | Calculate a regular weekday seat price. | Base price is used. |
| P6-T2 | Calculate a premium/weekend price. | Required price adjustments are applied. |
| P6-T3 | Apply a valid percentage discount. | Correct percentage is subtracted. |
| P6-T4 | Apply expired/inactive discount code. | Request is rejected with defined validation error. |
| P6-T5 | Cancel at different hours before show. | Correct refund policy and percentage are selected. |

---

## Phase 7 - Seat Holds and Concurrency Protection

### Objective

Implement the critical temporary seat-hold flow while preventing double allocation.

### Key Tasks

- Add migrations for `bookings` and `booking_seats`.
- Add booking-history index `(user_id, created_at DESC)`.
- Implement `Booking` and `BookingSeat` entities/repositories/services.
- Implement `POST /api/v1/shows/{id}/holds`.
- Implement the LLD `PESSIMISTIC_WRITE` repository query for selected `ShowSeat` rows.
- In one `@Transactional` service method: validate seats, lock them, release expired holds when applicable, calculate price, create `PENDING` booking, create `BookingSeat` records, and mark ShowSeats `HELD` with user/expiry metadata.
- Return `409 Conflict` if any selected seat is unavailable.

### Dependencies

- Phases 2, 4, 5, and 6.

### Deliverable

Customers can place timed holds on available seats, with database-backed protection against simultaneous holds on the same seat.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P7-T1 | Customer holds one available seat. | Pending booking exists; seat becomes `HELD`. |
| P7-T2 | Customer holds multiple available seats. | All requested seats are held in one booking. |
| P7-T3 | Request includes a seat not belonging to the show. | Request fails; no partial hold is created. |
| P7-T4 | Request includes booked seat. | `409 Conflict`; no requested seat is changed. |
| P7-T5 | Second customer holds an actively held seat. | `409 Conflict`. |
| P7-T6 | Two customers concurrently hold same seat. | Exactly one request succeeds; the other conflicts. |
| P7-T7 | Seat has an expired hold. | New customer can hold it successfully. |

---

## Phase 8 - Simulated Payment and Confirmation

### Objective

Convert a valid pending hold into a confirmed booking and booked show seats.

### Key Tasks

- Add the `payments` migration.
- Implement `Payment` entity, repository, service, and relevant DTOs.
- Implement `POST /api/v1/bookings/{id}/pay`.
- Lock/reload booking seats before confirmation.
- Verify booking ownership, `PENDING` state, and unexpired hold.
- Create `SUCCESS` payment record, mark ShowSeats `BOOKED`, clear hold metadata, and mark booking `CONFIRMED` in one transaction.
- Publish a booking-confirmation event after transaction commit.

### Dependencies

- Phase 7.

### Deliverable

Customers can pay for valid holds and receive confirmed bookings without risking stale-hold confirmation.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P8-T1 | Customer pays for valid pending hold. | Payment is `SUCCESS`; booking is `CONFIRMED`; seats are `BOOKED`. |
| P8-T2 | Customer pays after hold expiry. | `409 Conflict`; no successful payment/confirmation occurs. |
| P8-T3 | Another customer pays a booking they do not own. | `403 Forbidden` or `404 Not Found`. |
| P8-T4 | Customer pays already confirmed booking. | Request is rejected; no duplicate payment is created. |

---

## Phase 9 - Hold Expiry, Notifications, and Booking History

### Objective

Automate expired-hold cleanup, send non-blocking notifications, and expose a customer's booking history.

### Key Tasks

- Implement a scheduled task that runs every minute.
- Release `HELD` ShowSeats with `holdExpiry < now` and clear hold metadata.
- Mark corresponding pending bookings as `EXPIRED` where appropriate.
- Implement asynchronous booking-confirmation and cancellation/refund notifications.
- Implement optional reminder scheduling for confirmed bookings.
- Implement `GET /api/v1/bookings/me` using the authenticated user.

### Dependencies

- Phases 2, 7, and 8.

### Deliverable

Holds automatically expire, notifications do not block booking responses, and customers can view only their own history.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P9-T1 | Run expiry job with expired held seat. | Seat becomes `AVAILABLE`; hold fields are cleared. |
| P9-T2 | Run expiry job with unexpired hold. | Seat remains `HELD`. |
| P9-T3 | Confirm booking while notification service is slow. | Booking API completes without waiting for notification completion. |
| P9-T4 | Customer requests `/bookings/me`. | Only that customer's bookings are returned, newest first. |
| P9-T5 | Customer attempts another user's history access through request data. | User identity is ignored/rejected; only authenticated user's records return. |

---

## Phase 10 - Cancellation and Refunds

### Objective

Allow a customer to cancel a confirmed booking and apply the configured refund policy safely.

### Key Tasks

- Implement `POST /api/v1/bookings/{id}/cancel`.
- Verify booking ownership and require `CONFIRMED` booking status.
- Determine hours remaining until show start.
- Select the applicable active refund policy.
- Calculate refund from stored booking total.
- Record/refund payment with `REFUNDED` status.
- Mark booking `CANCELLED`.
- Release the associated ShowSeats only when the configured policy permits resale.
- Publish cancellation/refund notification asynchronously.

### Dependencies

- Phases 6, 8, and 9.

### Deliverable

Customers can cancel eligible confirmed bookings, with correct status changes and refunds based on configured policy.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P10-T1 | Owner cancels eligible confirmed booking. | Booking becomes `CANCELLED`; refund record reflects correct amount. |
| P10-T2 | Cancel booking at multiple lead times. | Correct refund policy is selected each time. |
| P10-T3 | Cancel a pending/expired/cancelled booking. | Request is rejected with business conflict. |
| P10-T4 | Another customer cancels the booking. | `403 Forbidden` or `404 Not Found`. |
| P10-T5 | Cancellation permits resale. | Associated seats become available as required. |

---

## Phase 11 - End-to-End Verification and Submission Readiness

### Objective

Verify the complete core flow and finalize required project artifacts.

### Key Tasks

- Run all unit and integration tests.
- Verify Flyway migrations from an empty database.
- Verify validation, error responses, authorization, and ownership checks across APIs.
- Prepare/update `README.md`, `AGENTS.md`, `SKILLS.md`, HLD, LLD, API collection, and raw development artifacts.
- Ensure Git history contains multiple meaningful commits.

### Dependencies

- All prior phases.

### Deliverable

A tested, documented repository ready for GitHub submission and video demonstration.

### Verification Test Cases

| ID | Scenario | Expected result |
|---|---|---|
| P11-T1 | Start from a clean database and run application. | All Flyway migrations apply successfully. |
| P11-T2 | Execute full test suite. | Unit and integration tests pass. |
| P11-T3 | Execute end-to-end flow: catalog setup -> show -> hold -> pay -> history -> cancel. | Each state transition and response matches the HLD/LLD. |
| P11-T4 | Execute concurrent same-seat hold test repeatedly. | Never more than one successful hold for the same ShowSeat. |
| P11-T5 | Review repository contents. | Required documentation and raw development artifacts are present. |

