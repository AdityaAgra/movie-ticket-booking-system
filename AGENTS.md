# AGENTS.md

## Project Overview

Build a Spring Boot REST API for a Movie Ticket Booking System.

The application must support:

- Multiple cities, theaters, auditoriums, and shows
- Seat-level booking
- Temporary seat holds with expiry
- Pricing tiers and discount codes
- Payment simulation
- Booking confirmation
- Booking cancellation and refunds
- Admin and customer roles
- Booking history
- Prevention of double booking during concurrent requests

## Technology Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Spring Data JPA
- Spring Security
- Flyway
- JUnit 5

## Project Structure

Use feature-based packages:

```text
com.aditya.movieticketbooking
├── common
├── config
├── user
├── city
├── theater
├── movie
├── show
├── booking
├── pricing
├── payment
├── refund
└── notification
```

Each feature should contain its controller, service, repository, entity, DTOs, and exceptions where applicable.
## Coding Rules
- Use REST APIs only. Do not build a frontend.
- Keep controllers thin; business logic belongs in service classes.
- Use DTOs for request and response bodies.
- Do not expose JPA entities directly through API endpoints.
- Use Jakarta Validation for request validation.
- Handle errors using a global exception handler.
- Use meaningful names for classes, methods, variables, and API endpoints.
- Add comments only where logic is non-obvious.
- Keep methods small and focused.

## Database Rules
- Use PostgreSQL as the primary database.
- Use Flyway migrations for schema changes.
- Do not use ddl-auto=create or ddl-auto=update.
- Use spring.jpa.hibernate.ddl-auto=validate.
- Add indexes and constraints where needed.
- Never commit passwords or database credentials.

## Security Rules
- Support two roles: ADMIN and CUSTOMER.
- Admin users manage cities, theaters, auditoriums, seats, shows, pricing, discount codes, and refund policies.
- Customer users browse shows, hold seats, book tickets, cancel bookings, and view their own booking history.
- Customers must not access admin endpoints.
- Customers must not view or cancel another customer’s booking.

# Booking Rules
- A seat must be tracked per show using a ShowSeat entity.
- Seat states are AVAILABLE, HELD, and BOOKED.
- A hold must expire after a configured time.
- Expired holds must return seats to AVAILABLE.
- A seat must never be confirmed in two bookings for the same show.
- Use transactions and database locking or constraints to prevent double booking.
- Validate hold expiry again before confirming a booking.
- Payment is simulated; do not integrate a real payment provider.

## Testing Rules
Add unit and integration tests for:
1. Price calculation.
2. Discount validation.
3. Seat hold creation.
4. Expired hold release.
5. Concurrent booking attempts for the same seat.
6. Booking confirmation.
7. Cancellation and refund calculation.
8. Role-based authorization.

## Out of Scope
Do not implement:
- Frontend or UI
- Docker, deployment, CI/CD
- Microservices
- OAuth, SSO, or MFA
- Real payment-gateway integration
- Production monitoring or alerting

## Documentation Rules
- Update README.md with setup instructions, assumptions, API details, and architectural decisions.
- Record AI tools and skills used in SKILLS.md.
- Keep raw files such as API collections, diagrams, and development notes in the docs/ folder.
