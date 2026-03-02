<div align="center">

# Healthcare Appointment Booking API

Healthcare Appointment Booking RESTful API. Originally developed as a Capstone Project for the Software Praktikum, this platform provides secure scheduling, role-based access control, and comprehensive user management. It seamlessly handles everything from robust doctor-patient interaction and real-time scheduling to reliable cloud deployment.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![SendGrid](https://img.shields.io/badge/SendGrid-Email-1A82E2?style=for-the-badge&logo=twilio&logoColor=white)
![GCP](https://img.shields.io/badge/GCP-Cloud_Run-4285F4?style=for-the-badge&logo=google-cloud&logoColor=white)

[![Tests](https://img.shields.io/badge/tests-123_passing-brightgreen?style=flat-square)](#testing)
[![Endpoints](https://img.shields.io/badge/endpoints-43-blue?style=flat-square)](API_REFERENCE.md)
[![Tables](https://img.shields.io/badge/database-10_tables-orange?style=flat-square)](#database-schema)

</div>

---

## Live Demo

| | URL |
|---|---|
| **Base API** | https://healthapp-backend-v2-186862202342.us-central1.run.app |
| **Swagger UI** | https://healthapp-backend-v2-186862202342.us-central1.run.app/swagger-ui/index.html |

---

## Table of Contents

- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Testing](#testing)
- [Getting Started](#getting-started)

---

## Key Features

- **JWT Authentication & RBAC** — Three distinct roles (`Patient`, `Doctor`, `Admin`) with email verification and password reset via SendGrid
- **Intelligent Scheduling** — Double-booking prevention at both application and database level, 24-hour cancellation policy, maximum one reschedule per appointment
- **Doctor Discovery** — Public multi-criteria search with filtering, sorting, and pagination
- **Reviews & Ratings** — Denormalized average ratings for O(1) read performance on search results
- **Admin Panel** — Dashboard statistics, user management (suspend/activate), doctor approval workflow, audit logging
- **Cloud Deployment** — Multi-stage Docker build, Google Cloud Run with auto-scaling, Cloud SQL managed PostgreSQL

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.7 (Web, Security, Data JPA, Validation, Actuator) |
| **Database** | PostgreSQL 15 (Cloud SQL in production, H2 for tests) |
| **Authentication** | Spring Security + JWT (jjwt 0.12.3) |
| **Email** | SendGrid Web API |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Build** | Maven + Lombok |
| **Containerization** | Docker + Docker Compose |
| **Cloud** | Google Cloud Run + Cloud SQL + Artifact Registry |
| **Testing** | JUnit 5 + Mockito + Spring Boot Test + MockMvc |

---

## Architecture

The application follows a **layered architecture** pattern:

```
HTTP Request
     |
  JWT Authentication Filter          -- Validates token before request reaches controller
     |
  Controller Layer                   -- REST endpoints + @PreAuthorize role checks
     |
  Service Layer                      -- Business logic + @Transactional atomicity
     |
  Repository Layer                   -- JPA repositories + custom @Query methods
     |
  PostgreSQL Database                -- Constraints, indexes, JSONB support
```

**Project Structure:**
```
src/main/java/com/healthapp/backend/
├── config/          # Security, CORS, Swagger configuration
├── controller/      # 8 REST controllers (43 endpoints)
├── dto/             # Request/Response DTOs with validation
├── entity/          # JPA entities (10 tables)
├── enums/           # Role, AppointmentStatus, DayOfWeek
├── exception/       # Global exception handler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT provider, auth filter, UserDetails
├── service/         # Business logic services
└── converter/       # JSONB and List type converters
```

---

## Database Schema

**10 tables** with foreign key relationships, indexes, and constraints:

```mermaid
erDiagram
    User ||--o| Patient : "has profile"
    User ||--o| Doctor : "has profile"
    User ||--o{ Notification : "receives"
    User ||--o{ AuditLog : "generates"
    Patient ||--o| MedicalHistory : "has"
    Patient ||--o{ Appointment : "books"
    Patient ||--o{ Review : "writes"
    Doctor ||--o{ Appointment : "attends"
    Doctor ||--o{ Availability : "sets schedule"
    Doctor ||--o{ Unavailability : "marks time off"
    Doctor ||--o{ Review : "receives"
    Appointment ||--o| Review : "has"

    User {
        UUID id PK
        VARCHAR email UK
        VARCHAR password
        ENUM role "PATIENT | DOCTOR | ADMIN"
        BOOLEAN verified
        TIMESTAMP created_at
    }

    Patient {
        UUID id PK
        UUID user_id FK
        VARCHAR first_name
        VARCHAR last_name
        DATE dob
        VARCHAR phone
        VARCHAR gender
        TEXT address
        TEXT insurance_info
    }

    Doctor {
        UUID id PK
        UUID user_id FK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR license_number UK
        VARCHAR specialization
        INTEGER experience
        TEXT education
        VARCHAR bio
        JSON languages
        TEXT clinic_address
        BOOLEAN approved
        DECIMAL average_rating
        INTEGER review_count
    }

    MedicalHistory {
        UUID id PK
        UUID patient_id FK
        JSONB questionnaire
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    Availability {
        UUID id PK
        UUID doctor_id FK
        ENUM day_of_week
        TIME start_time
        TIME end_time
        INTEGER slot_duration
        TIME break_start
        TIME break_end
    }

    Unavailability {
        UUID id PK
        UUID doctor_id FK
        DATE start_date
        DATE end_date
        TEXT reason
    }

    Appointment {
        UUID id PK
        UUID patient_id FK
        UUID doctor_id FK
        DATE date
        TIME start_time
        ENUM status "PENDING | CONFIRMED | COMPLETED | CANCELLED"
        TEXT reason
        INTEGER reschedule_count
        TIMESTAMP created_at
    }

    Review {
        UUID id PK
        UUID appointment_id FK, UK
        UUID patient_id FK
        UUID doctor_id FK
        INTEGER rating "1-5"
        VARCHAR comment
        TIMESTAMP created_at
    }

    Notification {
        UUID id PK
        UUID user_id FK
        ENUM type "EMAIL | SYSTEM"
        TEXT message
        BOOLEAN read
        TIMESTAMP sent_at
    }

    AuditLog {
        UUID id PK
        UUID user_id FK
        VARCHAR action
        VARCHAR resource
        TIMESTAMP timestamp
    }
```

---

## API Reference

The API exposes **43 endpoints** across 7 domains (Authentication, Profiles, Availability, Appointments, Doctor Search, Reviews, Admin).

**[Full API Reference (API_REFERENCE.md)](API_REFERENCE.md)** — complete list with HTTP methods, paths, roles, and descriptions.

> Interactive Swagger UI available at `/swagger-ui/index.html` when the app is running.

---

## Testing

```
Tests run: 123, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**123 automated tests** covering both unit and integration layers:

| Test Suite | Type | Tests |
|-----------|------|:-----:|
| AuthenticationServiceTest | Unit | 11 |
| UserServiceTest | Unit | 11 |
| ProfileServiceTest | Unit | 13 |
| AvailabilityServiceTest | Unit | 9 |
| AppointmentServiceTest | Unit | 5 |
| ReviewServiceTest | Unit | 14 |
| SearchServiceTest | Unit | 11 |
| AuthControllerTest | Integration | 14 |
| AvailabilityControllerTest | Integration | 14 |
| AppointmentControllerTest | Integration | 3 |
| ReviewControllerTest | Integration | 2 |
| DoctorControllerTest | Integration | 17 |

**Business rules validated:** double-booking prevention, 24-hour cancellation policy, one review per appointment, author-only review modification, automatic rating recalculation, public vs. protected endpoint access, sorting-after-filtering correctness.

---

## Getting Started

For full local setup instructions (PostgreSQL, environment variables, Maven, Docker):

**[Setup Guide (SETUP.md)](SETUP.md)**

**Quick start with Docker Compose:**
```bash
git clone https://github.com/<your-username>/HealthServicesApp.git
cd HealthServicesApp
cp .env.example .env
docker compose up
```

The API will be available at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

---

## License

Distributed under the MIT License. See  for more information.
