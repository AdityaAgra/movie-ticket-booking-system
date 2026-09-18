# API Reference and Local Testing

This reference contains every API implemented through Phase 10.

Base URL: http://localhost:8080/api/v1

## Authentication

The health API is public. Every other endpoint uses HTTP Basic authentication.

| Role | Username | Password | Access |
|---|---|---|---|
| Admin | admin@moviebooking.local | admin123 | Inventory, shows, discounts, refund policies |
| Customer | customer@moviebooking.local | customer123 | Browse, holds, payment |

These are local-development users created when APP_BOOTSTRAP_ENABLED is not false. The examples use curl.exe, which avoids the PowerShell curl alias.

## Error response

~~~json
{
  "timestamp": "2026-12-25T10:00:00Z",
  "status": 409,
  "error": "SEAT_UNAVAILABLE",
  "message": "One or more selected seats are unavailable.",
  "path": "/api/v1/shows/<show-id>/holds",
  "fieldErrors": {}
}
~~~

Common errors are 400 BAD_REQUEST or VALIDATION_ERROR, 401 UNAUTHORIZED, 403 ACCESS_DENIED, 404 NOT_FOUND, and 409 CONFLICT or SEAT_UNAVAILABLE.

## Health

### GET /health

~~~powershell
curl.exe http://localhost:8080/api/v1/health
~~~

Response, 200 OK:

~~~json
{"status":"UP"}
~~~

## City APIs

### POST /cities, admin

~~~powershell
curl.exe -u "admin@moviebooking.local:admin123" -H "Content-Type: application/json" -d '{"name":"Bengaluru"}' http://localhost:8080/api/v1/cities
~~~

Response, 201 Created:

~~~json
{"id":"11111111-1111-1111-1111-111111111111","name":"Bengaluru"}
~~~

### GET /cities, authenticated

~~~powershell
curl.exe -u "customer@moviebooking.local:customer123" http://localhost:8080/api/v1/cities
~~~

Response, 200 OK:

~~~json
[{"id":"11111111-1111-1111-1111-111111111111","name":"Bengaluru"}]
~~~

## Theater, auditorium, and seat APIs

### POST /theaters, admin

~~~powershell
curl.exe -u "admin@moviebooking.local:admin123" -H "Content-Type: application/json" -d '{"name":"PVR Orion","address":"Rajajinagar, Bengaluru","cityId":"<city-id>"}' http://localhost:8080/api/v1/theaters
~~~

Response, 201 Created:

~~~json
{"id":"22222222-2222-2222-2222-222222222222","name":"PVR Orion","address":"Rajajinagar, Bengaluru","cityId":"11111111-1111-1111-1111-111111111111"}
~~~

### POST /auditoriums, admin

~~~powershell
curl.exe -u "admin@moviebooking.local:admin123" -H "Content-Type: application/json" -d '{"name":"Screen 1","theaterId":"<theater-id>"}' http://localhost:8080/api/v1/auditoriums
~~~

Response, 201 Created:

~~~json
{"id":"33333333-3333-3333-3333-333333333333","name":"Screen 1","theaterId":"22222222-2222-2222-2222-222222222222"}
~~~

### POST /auditoriums/{auditoriumId}/seats, admin

Create each physical seat separately. seatType accepts REGULAR or PREMIUM.

~~~powershell
curl.exe -u "admin@moviebooking.local:admin123" -H "Content-Type: application/json" -d '{"rowLabel":"A","seatNumber":1,"seatType":"REGULAR"}' http://localhost:8080/api/v1/auditoriums/<auditorium-id>/seats
~~~

Response, 201 Created:

~~~json
{"id":"44444444-4444-4444-4444-444444444444","rowLabel":"A","seatNumber":1,"seatType":"REGULAR","auditoriumId":"33333333-3333-3333-3333-333333333333"}
~~~

## Movie and show APIs

### POST /movies, admin

~~~powershell
curl.exe -u "admin@moviebooking.local:admin123" -H "Content-Type: application/json" -d '{"title":"Interstellar","durationMinutes":169}' http://localhost:8080/api/v1/movies
~~~

Response, 201 Created:

~~~json
{"id":"55555555-5555-5555-5555-555555555555","title":"Interstellar","durationMinutes":169}
~~~

### POST /shows, admin

startTime must be a future UTC timestamp. Creating a show creates a ShowSeat availability record for every physical auditorium seat.

~~~powershell
curl.exe -u "admin@moviebooking.local:admin123" -H "Content-Type: application/json" -d '{"movieId":"<movie-id>","auditoriumId":"<auditorium-id>","startTime":"2026-12-25T14:30:00Z","basePrice":250.00}' http://localhost:8080/api/v1/shows
~~~

Response, 201 Created:

~~~json
{"id":"66666666-6666-6666-6666-666666666666","movieId":"55555555-5555-5555-5555-555555555555","auditoriumId":"33333333-3333-3333-3333-333333333333","startTime":"2026-12-25T14:30:00Z","basePrice":250.00,"showSeatCount":2}
~~~

### GET /shows?cityId={cityId}&date={YYYY-MM-DD}, authenticated

The date is evaluated in UTC.

~~~powershell
curl.exe -u "customer@moviebooking.local:customer123" "http://localhost:8080/api/v1/shows?cityId=<city-id>&date=2026-12-25"
~~~

Response, 200 OK:

~~~json
[{"id":"66666666-6666-6666-6666-666666666666","movieId":"55555555-5555-5555-5555-555555555555","movieTitle":"Interstellar","auditoriumId":"33333333-3333-3333-3333-333333333333","auditoriumName":"Screen 1","startTime":"2026-12-25T14:30:00Z","basePrice":250.00}]
~~~

### GET /shows/{showId}/seats, authenticated

Use the seatId value from this response to make a hold. Do not use the response id value.

~~~powershell
curl.exe -u "customer@moviebooking.local:customer123" http://localhost:8080/api/v1/shows/<show-id>/seats
~~~

Response, 200 OK:

~~~json
[{"id":"77777777-7777-7777-7777-777777777777","seatId":"44444444-4444-4444-4444-444444444444","rowLabel":"A","seatNumber":1,"seatType":"REGULAR","status":"AVAILABLE"}]
~~~

Show-seat status accepts AVAILABLE, HELD, and BOOKED.

## Discount and refund-policy APIs

### POST /discount-codes, admin

discountType accepts PERCENTAGE or FIXED_AMOUNT. Codes are normalized to uppercase.

~~~powershell
curl.exe -u "admin@moviebooking.local:admin123" -H "Content-Type: application/json" -d '{"code":"WEEKEND10","discountType":"PERCENTAGE","value":10.00,"validFrom":"2026-01-01T00:00:00Z","validTo":"2027-01-01T00:00:00Z","active":true}' http://localhost:8080/api/v1/discount-codes
~~~

Response, 201 Created:

~~~json
{"id":"88888888-8888-8888-8888-888888888888","code":"WEEKEND10","discountType":"PERCENTAGE","value":10.00,"validFrom":"2026-01-01T00:00:00Z","validTo":"2027-01-01T00:00:00Z","active":true}
~~~

### POST /refund-policies, admin

The policy controls the refund percentage selected during cancellation.

~~~powershell
curl.exe -u "admin@moviebooking.local:admin123" -H "Content-Type: application/json" -d '{"minimumHoursBeforeShow":24,"refundPercentage":100.00,"active":true}' http://localhost:8080/api/v1/refund-policies
~~~

Response, 201 Created:

~~~json
{"id":"99999999-9999-9999-9999-999999999999","minimumHoursBeforeShow":24,"refundPercentage":100.00,"active":true}
~~~

## Booking APIs

### GET /bookings/me, customer

Returns only the authenticated customer’s bookings, ordered from newest to oldest. The API does not accept a user identifier, so a request cannot select another customer’s history.

~~~powershell
curl.exe -u "customer@moviebooking.local:customer123" http://localhost:8080/api/v1/bookings/me
~~~

Response, 200 OK:

~~~json
[
  {
    "bookingId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "status":"CONFIRMED",
    "totalAmount":495.00,
    "createdAt":"2026-12-25T10:00:00Z",
    "showId":"66666666-6666-6666-6666-666666666666",
    "movieTitle":"Interstellar",
    "showStartTime":"2026-12-25T14:30:00Z"
  }
]
~~~

### POST /shows/{showId}/holds, customer

Use physical seat IDs returned by the seat map. The default hold duration is five minutes. Pricing applies configured seat-type and weekend surcharges plus an optional valid discount code.

~~~powershell
curl.exe -u "customer@moviebooking.local:customer123" -H "Content-Type: application/json" -d '{"seatIds":["<seat-id-1>","<seat-id-2>"],"discountCode":"WEEKEND10"}' http://localhost:8080/api/v1/shows/<show-id>/holds
~~~

Response, 201 Created:

~~~json
{"bookingId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa","status":"PENDING","holdExpiresAt":"2026-12-25T10:05:00Z","totalAmount":495.00}
~~~

If any seat is BOOKED or held by someone else, the endpoint returns 409 SEAT_UNAVAILABLE and does not create a partial booking.

### POST /bookings/{bookingId}/pay, customer

Only the owner can pay an unexpired PENDING booking. There is no request body because payment is simulated.

~~~powershell
curl.exe -X POST -u "customer@moviebooking.local:customer123" http://localhost:8080/api/v1/bookings/<booking-id>/pay
~~~

Response, 200 OK:

~~~json
{"paymentId":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb","bookingId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa","paymentStatus":"SUCCESS","bookingStatus":"CONFIRMED","amount":495.00}
~~~

Payment locks and revalidates each selected seat. On success, seats become BOOKED and their hold fields are cleared.

### POST /bookings/{bookingId}/cancel, customer

Only the owner can cancel a CONFIRMED booking. The system chooses the best active refund policy for the remaining time before the show, records a REFUNDED payment, changes the booking to CANCELLED, and returns its seats to AVAILABLE for resale.

~~~powershell
curl.exe -X POST -u "customer@moviebooking.local:customer123" http://localhost:8080/api/v1/bookings/<booking-id>/cancel
~~~

Response, 200 OK:

~~~json
{
  "bookingId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "bookingStatus":"CANCELLED",
  "refundPaymentId":"cccccccc-cccc-cccc-cccc-cccccccccccc",
  "refundPaymentStatus":"REFUNDED",
  "refundAmount":495.00,
  "refundPercentage":100.00,
  "seatsReleased":true
}
~~~

Pending, expired, or already cancelled bookings return 409 CONFLICT. A customer cannot cancel another customer’s booking.

## Complete local testing flow

### 1. Create the PostgreSQL database

In pgAdmin, run as the PostgreSQL administrator:

~~~sql
CREATE DATABASE movie_ticket_booking;
CREATE USER movie_user WITH PASSWORD 'your-local-password';
GRANT ALL PRIVILEGES ON DATABASE movie_ticket_booking TO movie_user;
~~~

Connect to movie_ticket_booking and run:

~~~sql
GRANT USAGE, CREATE ON SCHEMA public TO movie_user;
~~~

### 2. Configure and start the application

Open PowerShell in the backend directory:

~~~powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/movie_ticket_booking"
$env:DB_USERNAME = "movie_user"
$env:DB_PASSWORD = "your-local-password"
.\mvnw.cmd spring-boot:run
~~~

Keep this terminal open. Flyway applies the database migrations during startup.

### 3. Verify service and database connectivity

In a second PowerShell window:

~~~powershell
curl.exe http://localhost:8080/api/v1/health
~~~

The expected result is status UP. In the application log, confirm HikariPool - Start completed and a Flyway Database: jdbc:postgresql line.

### 4. Create test inventory

Use the admin API commands in this exact order:

1. POST /cities. Copy its id as city-id.
2. POST /theaters using city-id. Copy its id as theater-id.
3. POST /auditoriums using theater-id. Copy its id as auditorium-id.
4. POST /auditoriums/{auditorium-id}/seats at least twice. Copy each returned id as a seat-id.
5. POST /movies. Copy its id as movie-id.
6. POST /shows using movie-id and auditorium-id. Use a genuinely future startTime. Copy its id as show-id.
7. Optionally POST /discount-codes using a currently valid date range.

Each command and expected response is shown earlier in this document.

### 5. Browse and select seats as customer

1. GET /cities.
2. GET /shows with city-id and the date from your show startTime.
3. GET /shows/{show-id}/seats.
4. Verify the desired rows have status AVAILABLE.
5. Copy their seatId values.

### 6. Create and validate a hold

1. POST /shows/{show-id}/holds with the physical seatId values.
2. Copy bookingId and note holdExpiresAt from the response.
3. Call GET /shows/{show-id}/seats again.
4. Verify the selected seats are HELD.

### 7. Simulate payment

1. Before holdExpiresAt, POST /bookings/{booking-id}/pay.
2. Verify paymentStatus is SUCCESS and bookingStatus is CONFIRMED.
3. Call GET /shows/{show-id}/seats again.
4. Verify the selected seats are BOOKED.

### 8. Cancel a confirmed booking and verify the refund

1. POST /bookings/{booking-id}/cancel.
2. Verify bookingStatus is CANCELLED and refundPaymentStatus is REFUNDED.
3. GET /shows/{show-id}/seats and verify the cancelled seats are AVAILABLE.
4. GET /bookings/me and verify the booking status is CANCELLED.

### 9. Check booking history and hold expiry

1. GET /bookings/me to verify that the booking belongs to the authenticated customer.
2. To test expiry manually, create a separate hold and do not pay for it.
3. After its holdExpiresAt time passes, wait up to one minute for scheduled cleanup.
4. GET /shows/{show-id}/seats again and verify that unpaid seat is AVAILABLE.
5. GET /bookings/me and verify that unpaid booking has status EXPIRED.

### 10. Run automated verification

From backend, with the same database environment variables:

~~~powershell
.\mvnw.cmd test
~~~

The test suite covers API validation, price calculations, refund-rule selection, show creation, hold expiry, concurrent holds, and payment confirmation.

## Useful negative tests

- Call an admin endpoint using customer credentials: expect 403 ACCESS_DENIED.
- Hold an already BOOKED seat: expect 409 SEAT_UNAVAILABLE.
- Pay after holdExpiresAt: expect 409 CONFLICT.
- Pay a confirmed booking again: expect 409 CONFLICT.
- Use an unknown resource ID: expect 404 NOT_FOUND.
