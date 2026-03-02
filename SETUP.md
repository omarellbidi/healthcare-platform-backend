[← Back to Main README](README.md)

# Setup Guide

Local development setup for the Healthcare Appointment Booking API.

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **Java** | 21+ | Runtime & compilation |
| **Maven** | 3.9+ | Build tool (or use the included `mvnw` wrapper) |
| **PostgreSQL** | 15+ | Database |
| **Docker** | 24+ | Optional — for containerized setup |

---

## Option 1: Docker Compose (Recommended)

The fastest way to get everything running. Docker Compose starts both PostgreSQL and the application.

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/HealthServicesApp.git
cd HealthServicesApp

# 2. Create your environment file
cp .env.example .env

# 3. Start everything
docker compose up --build
```

The API will be available at:
- **API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

To stop:
```bash
docker compose down
```

---

## Option 2: Manual Setup

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/HealthServicesApp.git
cd HealthServicesApp
```

### 2. Create a PostgreSQL Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE healthapp;

# Exit
\q
```

### 3. Configure Environment Variables

Copy the example file and fill in your local values:

```bash
cp .env.example .env
```

Edit `.env` with your local settings:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/healthapp
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_local_password

# JWT (generate a random 64+ character hex string)
JWT_SECRET=your-random-jwt-secret-key-at-least-64-characters-long-for-hs512
JWT_EXPIRATION=86400000

# Email (create a free SendGrid account for testing)
SENDGRID_API_KEY=your-sendgrid-api-key
EMAIL_FROM=your-email@example.com

# Frontend URLs (for email verification links)
PATIENT_FRONTEND_URL=http://localhost:3000
DOCTOR_FRONTEND_URL=http://localhost:3001
```

### 4. Run the Application

```bash
# Using the Maven wrapper (no Maven installation needed)
./mvnw spring-boot:run
```

The app will start on port `8080`. Hibernate will automatically create all database tables on first run.

---

## Running Tests

The project includes **123 automated tests** (unit + integration). Tests use an in-memory **H2 database**, so no PostgreSQL setup is needed for testing.

```bash
# Run all tests
./mvnw test
```

Expected output:
```
Tests run: 123, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## API Documentation

Once the application is running, interactive API documentation is available via Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

You can test all 43 endpoints directly from the browser. Use the **Authorize** button to set your JWT token for protected endpoints.

---

## Project Structure

```
HealthServicesApp/
├── src/
│   ├── main/
│   │   ├── java/com/healthapp/backend/
│   │   │   ├── config/          # Security, CORS, Swagger
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Request/Response objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── enums/           # Enumerations
│   │   │   ├── exception/       # Global error handling
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── security/        # JWT + Spring Security
│   │   │   ├── service/         # Business logic
│   │   │   └── converter/       # Type converters
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # 123 automated tests
├── Dockerfile                   # Multi-stage Docker build
├── docker-compose.yml           # Local dev with PostgreSQL
├── pom.xml                      # Maven dependencies
└── .env.example                 # Environment variable template
```

---

[← Back to Main README](README.md)
