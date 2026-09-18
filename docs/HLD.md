# High-Level Design: Movie Ticket Booking System

## 1. Purpose

This document describes the architecture for a Spring Boot REST API that lets customers browse shows, temporarily hold seats, pay for tickets, cancel bookings, and receive refunds. Administrators manage cities, theaters, seat layouts, shows, pricing, discounts, and refund rules.

The most important system guarantee is that a seat for a particular show cannot be confirmed in more than one booking.

## 2. Scope

### In scope

- REST APIs with PostgreSQL persistence.
- Basic `ADMIN` and `CUSTOMER` role-based access control.
- Show browsing, seat availability, holds, simulated payment, confirmation, cancellation, and refunds.
- Regular, premium, and weekend pricing with discount codes.
- Asynchronous confirmation and reminder notifications.
- Unit and integration tests for core flows.

### Out of scope

- Frontend/UI, deployment, CI/CD, Docker, microservices, OAuth/SSO/MFA, real payment gateways, and production-grade monitoring.

## 3. Architecture

```text
Client (Postman / future UI)
          |
          v
Spring Boot REST API
  |-- Security layer: authentication and role authorization
  |-- Controller layer: HTTP request/response handling
  |-- Service layer: business rules and transactions
  |-- Repository layer: JPA data access and locking
          |
          v
PostgreSQL
          |
          +-- Flyway schema migrations

Background tasks
  |-- Expired-hold release
  +-- Asynchronous notifications
```

The application is a single modular monolith. This is deliberate: the assignment excludes distributed systems, and a single application allows booking transactions to be strongly consistent.

## 4. Main Components

| Component | Responsibilities |
|---|---|
| Auth and User | Login/basic identity handling and user roles. |
| Admin Catalog | Cities, theaters, auditoriums, seats, movies, shows, pricing, discounts, and refund policies. |
| Show Discovery | Browse shows and retrieve a show-specific seat map. |
| Booking | Hold seats, create pending bookings, prevent double allocation, and confirm bookings. |
| Payment | Simulate payment result and record payment status. |
| Refund | Apply cancellation/refund policy and record refund status. |
| Notification | Send booking confirmations and reminders outside the request transaction. |
| Scheduled Jobs | Release expired holds periodically. |

## 5. Core Data Relationships

```text
City -> Theater -> Auditorium -> Seat
Movie -> Show -> ShowSeat <- Seat
User -> Booking -> BookingSeat -> ShowSeat
Booking -> Payment
```

`Seat` represents a physical auditorium seat. `ShowSeat` represents that seat's availability for one specific show. Booking operations always act on `ShowSeat` records.

## 6. Primary Workflows

### 6.1 Seat hold and booking

```text
Customer selects seats
  -> API locks requested ShowSeat rows in one transaction
  -> API verifies every seat is available or has an expired hold
  -> API creates a PENDING booking
  -> API changes ShowSeats to HELD and stores hold expiry
  -> customer pays before expiry
  -> API revalidates the hold
  -> API records successful simulated payment
  -> API changes ShowSeats to BOOKED and booking to CONFIRMED
  -> notification task is queued asynchronously
```

### 6.2 Hold expiry

```text
Scheduled job runs every minute
  -> finds HELD ShowSeats whose hold expiry is in the past
  -> changes them to AVAILABLE and clears hold metadata
  -> cancels the related pending booking where appropriate
```

The payment/confirmation path also checks expiry. This ensures correctness even if the scheduler has not run at the exact expiration time.

### 6.3 Cancellation and refund

```text
Customer requests cancellation
  -> API checks booking ownership and confirmed status
  -> Refund service calculates percentage from configured policy
  -> API creates/refunds simulated payment record
  -> API marks booking CANCELLED
  -> API releases its ShowSeats when the business rule permits
  -> cancellation/refund notification is queued
```

## 7. Concurrency Strategy

The database is the source of truth for availability.

1. The seat-hold service starts a transaction.
2. It reads the requested `ShowSeat` rows with pessimistic write locking.
3. It rejects any row that is `BOOKED` or actively `HELD` by another customer.
4. It creates the hold and pending booking before committing.
5. The payment service locks/revalidates the booking's seats again before confirmation.

Additional database protections:

- A unique constraint exists on `(show_id, seat_id)` in `show_seats`.
- A unique constraint exists on `(booking_id, show_seat_id)` in `booking_seats`.
- All hold, payment confirmation, and cancellation state changes are transactional.

## 8. Security Model

| Role | Allowed actions |
|---|---|
| `ADMIN` | Manage catalog data, prices, discounts, shows, seat layouts, and refund policies. |
| `CUSTOMER` | Browse, hold, pay, cancel their bookings, and view only their own booking history. |

Use Spring Security for endpoint authorization. Basic authentication is sufficient for the first version; JWT can be added only if time permits.

## 9. Failure Handling

| Situation | API behavior |
|---|---|
| Seat does not belong to show | `404 Not Found` or `400 Bad Request`. |
| Seat is booked/actively held | `409 Conflict`. |
| Hold expired before payment | `409 Conflict`. |
| Invalid request body | `400 Bad Request` with field errors. |
| Customer accesses another booking | `403 Forbidden` or `404 Not Found`. |
| Invalid discount or cancellation policy | `400 Bad Request`. |

## 10. Quality and Test Strategy

- Unit-test pricing, discounts, and refunds as independent services.
- Integration-test database migrations and REST APIs.
- Add a concurrent integration test where two customers attempt to hold the same seat simultaneously; exactly one request must succeed.
- Test expiry, payment confirmation, cancellation, access control, validation, and error responses.

