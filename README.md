# Maid Booking System

A modular Spring Boot backend for a home-services (maid) booking platform with H2 in-memory persistence, extensible discovery filters, pluggable booking/payment/cancellation strategies, and per-maid concurrency control.

## Tech Stack
- Java 17
- Spring Boot (WebMVC, Data JPA, Validation)
- H2 In-Memory Database (with `/h2-console` enabled)
- JUnit 5 & Mockito

## Build & Run

```bash
./mvnw spring-boot:run
```

Run unit tests:
```bash
./mvnw test
```

H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:interview_db`, User: `sa`, Password: empty).

Postman Collection: Import `maid-booking-postman-collection.json` from the project root (`baseUrl` defaults to `http://localhost:8080`).

---

## API Reference

All endpoints return a generic `ApiResponse<T>` envelope:
```json
{
  "success": true,
  "message": "OK",
  "data": { ... },
  "timestamp": "2026-11-25T19:00:00"
}
```

### 1. Onboard Maid
`POST /api/maids`

**Request Body:**
```json
{
  "name": "Asha",
  "locality": "Koramangala",
  "gender": "FEMALE",
  "rating": 4.8,
  "hourlyRate": 300,
  "skills": ["CLEANING", "COOKING"],
  "availabilityWindows": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "08:00:00",
      "endTime": "18:00:00"
    },
    {
      "dayOfWeek": "WEDNESDAY",
      "startTime": "08:00:00",
      "endTime": "18:00:00"
    }
  ]
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/maids \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Asha",
    "locality": "Koramangala",
    "gender": "FEMALE",
    "rating": 4.8,
    "hourlyRate": 300,
    "skills": ["CLEANING", "COOKING"],
    "availabilityWindows": [
      { "dayOfWeek": "MONDAY", "startTime": "08:00:00", "endTime": "18:00:00" }
    ]
  }'
```

---

### 2. Get Maid by ID
`GET /api/maids/{id}`

**Example:**
```bash
curl http://localhost:8080/api/maids/1
```

---

### 3. Discover Available Maids
`GET /api/maids/discover`

All query parameters are optional:
- `locality` (String, case-insensitive)
- `serviceType` (`CLEANING`, `COOKING`, `DISHWASHING`, `LAUNDRY`, `BABYSITTING`)
- `startTime` (ISO-8601 DateTime, e.g. `2026-11-30T09:00:00`)
- `durationMinutes` (Integer, used with `startTime` to filter out busy/unavailable maids)
- `minRating` (Double)
- `maxHourlyRate` (Decimal)
- `gender` (`FEMALE`, `MALE`, `OTHER`)

**Example:**
```bash
curl "http://localhost:8080/api/maids/discover?locality=Koramangala&serviceType=CLEANING&startTime=2026-11-30T09:00:00&durationMinutes=120&minRating=4.5&maxHourlyRate=400&gender=FEMALE"
```

---

### 4. Create Booking
`POST /api/bookings`

Supports three `bookingType` values: `INSTANT`, `SCHEDULED`, and `RECURRING`.

**Scheduled Booking Request:**
```json
{
  "customerId": "cust-101",
  "maidId": 1,
  "serviceType": "CLEANING",
  "bookingType": "SCHEDULED",
  "startTime": "2026-11-30T09:00:00",
  "durationMinutes": 120
}
```

**Instant Booking Request** (`startTime` optional; defaults to `now`):
```json
{
  "customerId": "cust-101",
  "maidId": 1,
  "serviceType": "CLEANING",
  "bookingType": "INSTANT",
  "durationMinutes": 60
}
```

**Recurring Booking Request** (`recurrencePattern`: `DAILY`, `WEEKDAYS`, or `WEEKLY`; `occurrences`: number of slots, defaults to `4`):
```json
{
  "customerId": "cust-101",
  "maidId": 1,
  "serviceType": "CLEANING",
  "bookingType": "RECURRING",
  "startTime": "2026-11-30T09:00:00",
  "durationMinutes": 120,
  "recurrencePattern": "WEEKDAYS",
  "occurrences": 3
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cust-101",
    "maidId": 1,
    "serviceType": "CLEANING",
    "bookingType": "SCHEDULED",
    "startTime": "2026-11-30T09:00:00",
    "durationMinutes": 120
  }'
```

---

### 5. Get Booking by ID
`GET /api/bookings/{id}`

**Example:**
```bash
curl http://localhost:8080/api/bookings/1
```

---

### 6. Process Payment
`POST /api/payments`

Supports `CARD`, `UPI`, and `WALLET`.
- Pass `occurrenceId` to pay for a single occurrence (useful for `RECURRING` bookings), or omit it to pay for all `PENDING_PAYMENT` occurrences in the booking.
- Pass `idempotencyKey` to safely retry requests without duplicate charges.
- Required `details` per method:
  - `CARD`: `{ "cardLast4": "4242" }`
  - `UPI`: `{ "upiId": "user@upi" }`
  - `WALLET`: `{ "walletId": "wallet-123" }`

**Example (UPI for a single occurrence):**
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "occurrenceId": 1,
    "paymentMethod": "UPI",
    "idempotencyKey": "pay-req-001",
    "details": {
      "upiId": "user@upi"
    }
  }'
```

---

### 7. Cancel Booking or Single Occurrence
`POST /api/bookings/{id}/cancel`

- Pass `{"occurrenceId": 1}` to cancel a single occurrence in a recurring booking.
- Pass `{}` or omit the body to cancel all remaining occurrences in the booking.
- Refunds are computed via `TimeBasedCancellationPolicy` (`>= 24h`: 100%, `>= 2h`: 50%, `< 2h`: 0%) and the maid's slot is freed immediately.

**Example (Cancel single occurrence):**
```bash
curl -X POST http://localhost:8080/api/bookings/1/cancel \
  -H "Content-Type: application/json" \
  -d '{ "occurrenceId": 1 }'
```

**Example (Cancel entire booking):**
```bash
curl -X POST http://localhost:8080/api/bookings/1/cancel
```

---

## Key Design Decisions & Abstractions

1. **Clean Separation of Domain & Persistence**: Domain models (`Maid`, `Booking`, `BookingOccurrence`, `Payment`, `TimeSlot`, `AvailabilityWindow`) are decoupled from JPA entities via repository ports (`MaidRepository`, `BookingRepository`, `PaymentRepository`).
2. **Extensible Discovery (`MaidFilter`)**: Search criteria are evaluated through a list of `MaidFilter` Spring components (`LocalityFilter`, `ServiceTypeFilter`, `RatingFilter`, `PriceFilter`, `GenderFilter`). Adding a new filter requires only a new component.
3. **Booking Strategies (`BookingStrategy`)**: `InstantBookingStrategy`, `ScheduledBookingStrategy`, and `RecurringBookingStrategy` generate `TimeSlot`s behind a `BookingStrategyFactory`.
4. **Concurrency & Double-Booking Prevention**: `MaidLockManager` uses per-maid `ReentrantLock`s so concurrent booking requests for the same maid and overlapping time slots cannot double-book.
5. **Pluggable Payment & Cancellation**: `PaymentMethodStrategy` + `PaymentGateway` (`MockPaymentGateway`) handle method validation, idempotency, and gateway abstraction. `CancellationPolicy` (`TimeBasedCancellationPolicy`) computes refunds and frees cancelled slots immediately.

## Key Assumptions
- Availability is modeled as recurring weekly windows (`DayOfWeek` + `startTime` to `endTime`).
- A booking occurrence blocks a maid's slot while in `PENDING_PAYMENT` or `CONFIRMED` state; cancelling frees the slot.
- Authentication/authorization is out of scope (`customerId` is passed in the request).

## With More Time
- Add Flyway migrations and a production database profile (e.g., PostgreSQL) with pessimistic/optimistic DB locking for multi-instance deployments.
- Add OpenAPI/Swagger documentation and integration tests using `MockMvc`.
- Support dynamic/surge pricing strategies and maid rating updates after `COMPLETED` occurrences.
# booking-app
