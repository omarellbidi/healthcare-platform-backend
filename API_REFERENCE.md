[← Back to Main README](README.md)

# API Reference

**43 endpoints** across 7 domains. All endpoints return JSON. Errors follow a consistent `{ success, message }` format.

**Authentication:** Protected endpoints require a JWT token in the `Authorization: Bearer <token>` header.

---

## Table of Contents

- [Authentication](#authentication-7-endpoints)
- [Patient & Doctor Profiles](#patient--doctor-profiles-6-endpoints)
- [Availability Management](#availability-management-8-endpoints)
- [Appointment Booking](#appointment-booking-6-endpoints)
- [Doctor Search](#doctor-search-2-endpoints)
- [Reviews & Ratings](#reviews--ratings-4-endpoints)
- [Admin Panel](#admin-panel-9-endpoints)
- [System](#system-1-endpoint)

---

## Authentication (7 endpoints)

All endpoints are **public** (no authentication required).

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register a new patient or doctor account |
| `POST` | `/api/auth/verify-email` | Verify email address with token |
| `POST` | `/api/auth/login` | Authenticate and receive JWT token |
| `POST` | `/api/auth/logout` | Invalidate current session |
| `POST` | `/api/auth/forgot-password` | Request a password reset email |
| `POST` | `/api/auth/reset-password` | Reset password using token |
| `GET`  | `/api/auth/health` | Health check for the auth service |

---

## Patient & Doctor Profiles (6 endpoints)

All endpoints require **JWT authentication** and the appropriate role.

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/profile/patient` | Patient | Get current patient's profile |
| `PUT` | `/api/profile/patient` | Patient | Update current patient's profile |
| `DELETE` | `/api/profile/patient` | Patient | Delete patient account and profile |
| `GET` | `/api/profile/doctor` | Doctor | Get current doctor's profile |
| `PUT` | `/api/profile/doctor` | Doctor | Update current doctor's profile |
| `DELETE` | `/api/profile/doctor` | Doctor | Delete doctor account and profile |

---

## Availability Management (8 endpoints)

Doctor schedule and time-off management. Time slot viewing is **public**.

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/availability` | Doctor | Create weekly availability schedule |
| `GET` | `/api/availability` | Doctor | Get current availability schedule |
| `PUT` | `/api/availability/{id}` | Doctor | Update an availability schedule entry |
| `DELETE` | `/api/availability/{id}` | Doctor | Delete an availability schedule entry |
| `POST` | `/api/availability/unavailability` | Doctor | Mark dates as unavailable (vacation, sick, etc.) |
| `GET` | `/api/availability/unavailability` | Doctor | Get all unavailability periods |
| `DELETE` | `/api/availability/unavailability/{id}` | Doctor | Remove an unavailability period |
| `GET` | `/api/availability/slots/{doctorId}` | Public | Get available time slots for a doctor on a given date |

---

## Appointment Booking (6 endpoints)

All endpoints require **JWT authentication**.

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/appointments` | Patient | Book a new appointment with a doctor |
| `GET` | `/api/appointments` | Patient / Doctor | List all appointments for the current user |
| `GET` | `/api/appointments/{id}` | Patient / Doctor | Get details of a specific appointment |
| `PUT` | `/api/appointments/{id}/cancel` | Patient / Doctor | Cancel an appointment (24-hour policy) |
| `PUT` | `/api/appointments/{id}/reschedule` | Patient | Reschedule to a new date/time (max 1 reschedule) |
| `PUT` | `/api/appointments/{id}/status` | Doctor | Update status (confirm / complete) |

---

## Doctor Search (2 endpoints)

Both endpoints are **public** (no authentication required).

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/doctors/search` | Search doctors with filters (name, specialization, languages, rating, availability, sorting, pagination) |
| `GET` | `/api/doctors/{id}` | Get a doctor's public profile (excludes sensitive data) |

---

## Reviews & Ratings (4 endpoints)

Creating, updating, and deleting reviews requires **Patient** role. Viewing is **public**.

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/reviews` | Patient | Submit a review for a completed appointment |
| `GET` | `/api/reviews/doctor/{doctorId}` | Public | Get all reviews for a doctor (paginated) |
| `PUT` | `/api/reviews/{id}` | Patient | Update an existing review (author only) |
| `DELETE` | `/api/reviews/{id}` | Patient | Delete a review (author only) |

---

## Admin Panel (9 endpoints)

All endpoints require **Admin** role.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/dashboard` | Get system-wide dashboard statistics |
| `GET` | `/api/admin/users` | List all users with search and pagination |
| `GET` | `/api/admin/users/{id}` | Get detailed user information |
| `PUT` | `/api/admin/users/{id}/suspend` | Suspend a user account |
| `PUT` | `/api/admin/users/{id}/activate` | Reactivate a suspended account |
| `GET` | `/api/admin/doctors/pending` | List doctors awaiting approval |
| `PUT` | `/api/admin/doctors/{id}/approve` | Approve a doctor's application |
| `PUT` | `/api/admin/doctors/{id}/reject` | Reject a doctor's application with reason |
| `GET` | `/api/admin/audit-logs` | Get filtered audit logs with pagination |

---

## System (1 endpoint)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | API welcome message and status check |

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | Success |
| `201` | Resource created |
| `400` | Validation error |
| `401` | Missing or invalid JWT token |
| `403` | Insufficient role permissions |
| `404` | Resource not found |
| `409` | Business rule conflict (double-booking, duplicate review, etc.) |
| `500` | Internal server error |

---

[← Back to Main README](README.md)
