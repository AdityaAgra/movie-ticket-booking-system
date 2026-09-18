# Low-Level Design: Movie Ticket Booking System

## 1. Package Layout

```text
com.aditya.movieticketbooking
|-- common        # shared errors, API responses, utilities
|-- config        # security, scheduling, async configuration
|-- user          # User, Role, authentication support
|-- city          # City administration
|-- theater       # Theater, Auditorium, Seat administration
|-- movie         # Movie administration
|-- show          # Show and ShowSeat availability
|-- booking       # holds, bookings, booking seats, booking history
|-- pricing       # price tiers and discount codes
|-- payment       # simulated payments
|-- refund        # refund policy and calculation
`-- notification  # async confirmation/reminder events
```

Every feature uses the following pattern when applicable:

```text
FeatureController -> FeatureService -> FeatureRepository -> Entity
                                   -> dto/ request and response objects
                                   -> exception/ feature-specific errors
```

## 2. Domain Model

### 2.1 Enumerations

```java
public enum Role { ADMIN, CUSTOMER }
public enum SeatType { REGULAR, PREMIUM }
public enum ShowSeatStatus { AVAILABLE, HELD, BOOKED }
public enum BookingStatus { PENDING, CONFIRMED, CANCELLED, EXPIRED }
public enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED }
public enum DiscountType { PERCENTAGE, FIXED_AMOUNT }
```

### 2.2 Entities

| Entity | Important fields | Notes |
|---|---|---|
| `User` | id, name, email, passwordHash, role | Email is unique. |
| `City` | id, name | City name is unique. |
| `Theater` | id, name, address, city | Belongs to one city. |
| `Auditorium` | id, name, theater | Belongs to one theater. |
| `Seat` | id, rowLabel, seatNumber, seatType, auditorium | Physical seat; unique per auditorium position. |
| `Movie` | id, title, durationMinutes | Movie metadata. |
| `Show` | id, movie, auditorium, startTime, basePrice | One screening in one auditorium. |
| `ShowSeat` | id, show, seat, status, heldByUser, holdExpiry | Source of truth for a seat's per-show availability. |
| `Booking` | id, user, show, status, totalAmount, createdAt | Created as `PENDING` at hold time. |
| `BookingSeat` | booking, showSeat, priceAtBooking | Join record retaining booked price. |
| `Payment` | id, booking, amount, status, createdAt | Simulated payment/refund record. |
| `DiscountCode` | id, code, type, value, validFrom, validTo, active | Code may be percentage or fixed amount. |
| `RefundPolicy` | id, minimumHoursBeforeShow, refundPercentage, active | Select the best matching policy during cancellation. |

## 3. Database Constraints and Indexes

```sql
ALTER TABLE seats
  ADD CONSTRAINT uq_seat_position UNIQUE (auditorium_id, row_label, seat_number);

ALTER TABLE show_seats
  ADD CONSTRAINT uq_show_seat UNIQUE (show_id, seat_id);

CREATE INDEX idx_show_seats_hold_expiry
  ON show_seats (status, hold_expiry);

CREATE INDEX idx_bookings_user_created
  ON bookings (user_id, created_at DESC);
```

The `show_seats` unique constraint prevents duplicate availability records. It does not replace transaction locking; both are required.

## 4. API Design

Base URL: `/api/v1`

### 4.1 Admin APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/cities` | Create city. |
| POST | `/theaters` | Create theater in a city. |
| POST | `/auditoriums` | Create auditorium. |
| POST | `/auditoriums/{id}/seats` | Add seat layout. |
| POST | `/movies` | Create movie. |
| POST | `/shows` | Create show and generate its ShowSeats. |
| POST | `/discount-codes` | Create discount code. |
| POST | `/refund-policies` | Create refund policy. |

All require `ADMIN`.

### 4.2 Customer APIs

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/cities` | Browse cities. |
| GET | `/shows?cityId={id}&date={date}` | Browse shows. |
| GET | `/shows/{id}/seats` | View seat map and availability. |
| POST | `/shows/{id}/holds` | Hold selected seats. |
| POST | `/bookings/{id}/pay` | Simulate payment and confirm. |
| POST | `/bookings/{id}/cancel` | Cancel and apply refund. |
| GET | `/bookings/me` | View current user's history. |

### 4.3 Hold Request/Response

```json
POST /api/v1/shows/{showId}/holds
{
  "seatIds": ["uuid-1", "uuid-2"],
  "discountCode": "WEEKEND10"
}
```

```json
{
  "bookingId": "uuid",
  "status": "PENDING",
  "holdExpiresAt": "2026-09-18T12:05:00",
  "totalAmount": 650.00
}
```

## 5. Critical Service Contracts

### 5.1 `ShowService.createShow`

1. Validate auditorium and movie.
2. Create the show.
3. Read every physical `Seat` in the auditorium.
4. Create one `ShowSeat` for each physical seat with `AVAILABLE` status.
5. Save the operation in one transaction.

### 5.2 `BookingService.holdSeats`

1. Authenticate customer and validate non-empty seat IDs.
2. Lock the requested `ShowSeat` rows with `PESSIMISTIC_WRITE`.
3. Reject missing seats, `BOOKED` seats, and unexpired holds owned by another user.
4. Treat expired holds as releasable before evaluating availability.
5. Calculate pricing and discount.
6. Create a `PENDING` booking and `BookingSeat` rows.
7. Set each ShowSeat to `HELD`, store the customer ID and expiration timestamp.
8. Commit transaction and return booking ID plus expiry.

### 5.3 `BookingService.confirmPayment`

1. Load booking and verify it belongs to the authenticated customer.
2. Lock/reload booking seats.
3. Require `PENDING` status and unexpired hold.
4. Create `SUCCESS` payment record.
5. Mark all ShowSeats `BOOKED`, clear hold metadata, and mark booking `CONFIRMED`.
6. Publish a notification event after the transaction commits.

### 5.4 `RefundService.cancelBooking`

1. Confirm customer owns a `CONFIRMED` booking.
2. Determine hours between now and show start.
3. Select the matching active refund policy.
4. Calculate refund amount from stored booking total.
5. Create a `REFUNDED` payment entry or update payment status.
6. Mark booking `CANCELLED` and release booking seats if policy permits resale.

## 6. Locking Repository Query

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT ss FROM ShowSeat ss
    WHERE ss.show.id = :showId
      AND ss.seat.id IN :seatIds
    """)
List<ShowSeat> findByShowIdAndSeatIdsForUpdate(
        UUID showId,
        List<UUID> seatIds);
```

The hold service must call this inside a `@Transactional` method. Never check availability in an unlocked read and update it later in a separate transaction.

## 7. Background Processing

| Job | Frequency | Action |
|---|---|---|
| Hold expiry | Every minute | Release `HELD` ShowSeats where `holdExpiry < now`; expire pending bookings. |
| Reminder notification | Optional scheduled job | Notify confirmed-booking customers before a show. |

Use `@EnableScheduling` and `@Scheduled`. Use `@Async` or application events for notifications so HTTP booking confirmation does not wait for notification delivery.

## 8. Error Response Contract

```json
{
  "timestamp": "2026-09-18T12:00:00Z",
  "status": 409,
  "error": "SEAT_UNAVAILABLE",
  "message": "One or more selected seats are unavailable.",
  "path": "/api/v1/shows/{showId}/holds"
}
```

Implement a `@RestControllerAdvice` for validation, resource-not-found, access-denied, and business-conflict errors.

## 9. Required Tests

| Test | Type |
|---|---|
| Price by seat type/weekend/discount | Unit |
| Refund percentage selection | Unit |
| Show creation creates ShowSeats | Integration |
| Hold changes available seats to held | Integration |
| Expired hold releases a seat | Integration |
| Two simultaneous holds for the same seat result in one success and one conflict | Integration |
| Payment confirms a valid hold | Integration |
| Payment rejects an expired hold | Integration |
| Customer cannot access admin endpoints or another customer's booking | Integration |

