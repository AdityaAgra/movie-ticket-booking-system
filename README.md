# Movie Ticket Booking System

A Spring Boot REST API for a movie ticket booking system. The project is a monolithic backend application backed by PostgreSQL and Flyway database migrations.

## Prerequisites

Install the following before running the project:

- Java 21 or later
- PostgreSQL
- Git

The repository includes the Maven Wrapper, so a separate Maven installation is not required.

## Local Database Setup

1. In pgAdmin, create the database:

   ```sql
   CREATE DATABASE movie_ticket_booking;
   ```

2. Create an application user:

   ```sql
   CREATE USER movie_user WITH PASSWORD 'choose-a-strong-local-password';
   GRANT ALL PRIVILEGES ON DATABASE movie_ticket_booking TO movie_user;
   ```

3. Connect to `movie_ticket_booking` as the `postgres` administrator, then allow Flyway to create and manage schema objects:

   ```sql
   GRANT USAGE, CREATE ON SCHEMA public TO movie_user;
   ```

## Configure Local Environment Variables

The application reads database values from environment variables. In PowerShell, from the `backend` directory, set them for the current terminal session:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/movie_ticket_booking"
$env:DB_USERNAME = "movie_user"
$env:DB_PASSWORD = "your-local-postgres-password"
```

Do not commit passwords or a local `.env` file to Git.

## Run the Application

From the repository root:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The service starts on port `8080`.

## Verify Health Check

Keep the application running, then open a second PowerShell window and run:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/health
```

Expected response:

```json
{
  "status": "UP"
}
```

You may also open `http://localhost:8080/api/v1/health` directly in a browser.

## Verify PostgreSQL Connectivity

The application is connected when its startup logs contain both of these messages:

```text
HikariPool - Start completed.
Database: jdbc:postgresql://localhost:5432/movie_ticket_booking
```

The second message is emitted by Flyway after it has connected to PostgreSQL. In pgAdmin, refresh the `movie_ticket_booking` database. A successful first run creates the `flyway_schema_history` table in the `public` schema.

If startup fails with `permission denied for schema public`, run this as the `postgres` administrator while connected to `movie_ticket_booking`:

```sql
GRANT USAGE, CREATE ON SCHEMA public TO movie_user;
```

## Run Tests

From the `backend` directory, with the database environment variables set:

```powershell
.\mvnw.cmd test
```

## Local Development Users

On first startup, the application creates these local users when `APP_BOOTSTRAP_ENABLED` is not set to `false`:

| Role | Username | Password |
|---|---|---|
| Admin | `admin@moviebooking.local` | `admin123` |
| Customer | `customer@moviebooking.local` | `customer123` |

These credentials are for local development only. Override them with `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `CUSTOMER_EMAIL`, and `CUSTOMER_PASSWORD` before sharing or deploying the application.

## Project Documentation

- [High-Level Design](docs/HLD.md)
- [Low-Level Design](docs/LLD.md)
- [Implementation Plan](docs/IMPLEMENTATION_PLAN.md)
- [API Reference and Local Testing](docs/API_REFERENCE.md)
- [Postman Collection](docs/Movie-Ticket-Booking-System.postman_collection.json)
- [Agent Instructions](AGENTS.md)
- [Skills Used](SKILLS.md)
