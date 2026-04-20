# Tuition Attendance Management System

Full-stack attendance management application for a tuition center handling classes 9th and 10th, built with:

- Backend: Spring Boot, Spring Security, JWT, JPA, MySQL
- Frontend: React + Vite
- Attendance flow: fingerprint-based scan simulation with a clean interface for later hardware integration

## Project Structure

```text
Tution_attendance_system/
  backend/   -> Spring Boot REST API
  frontend/  -> React application
```

## Features

- Student registration request flow with admin approval
- Admin login
- JWT-based authentication and role-based access
- Admin approval queue for pending student registrations
- Fingerprint ID registration per student
- Fingerprint-only attendance marking
- One attendance record per student per day
- First-login forced password change after credentials are emailed
- Student dashboard with attendance history and percentage
- Admin dashboard with student list, filters, analytics, and low-attendance visibility
- Email hook for sending credentials after admin approval
- Swagger/OpenAPI support through Springdoc
- Sample seeded admin, students, fingerprints, and attendance records

## Default Demo Credentials

- Admin
  - Email: `admin@tuition.local`
  - Password: `Admin@123`

- Students
  - Email: `aman@student.com`
  - Password: `Student@123`
  - Fingerprint ID: `FP-1001`
- Students
  - Email: `priya@student.com`
  - Password: `Student@123`
  - Fingerprint ID: `FP-1002`

## Backend Setup

1. Install Java 17 and Maven.
2. Install MySQL and create or allow creation of a database named `tuition_attendance`.
3. Update `backend/src/main/resources/application.yml` if your MySQL username/password differ from the default `root/root`.
4. Optionally configure SMTP in the same file. If SMTP is not configured, the app logs generated credentials instead of sending email.
5. Start the backend:

```bash
cd backend
mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Frontend Setup

1. Install Node.js 18+.
2. Start the React app:

```bash
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## Fingerprint Integration Design

- The current implementation stores a secure `fingerprintId` reference, not raw biometric image data.
- Attendance is only accepted through the `/api/attendance/scan` endpoint.
- The simulated scanner can later be replaced by a hardware adapter that resolves a real device scan to the same `fingerprintId`.
- Admins link students to fingerprint IDs using `/api/admin/students/{studentId}/fingerprint`.

## Main API Endpoints

### Auth

- `POST /api/auth/register` -> submit student registration request
- `POST /api/auth/login` -> login admin or student
- `GET /api/auth/me` -> current logged-in user
- `POST /api/auth/change-password` -> change temporary password after first login
- `GET /api/auth/admin/pending-registrations` -> list pending registration requests
- `POST /api/auth/admin/pending-registrations/{studentId}/approve` -> approve request and email credentials

### Student

- `GET /api/student/dashboard` -> personal attendance history and percentage

### Attendance

- `POST /api/attendance/scan` -> mark attendance through fingerprint ID only

### Admin

- `POST /api/admin/students/{studentId}/fingerprint` -> register student fingerprint ID
- `GET /api/admin/students` -> list students with fingerprint and attendance summary
- `GET /api/admin/attendance?studentId=&studentClass=&date=` -> filter attendance
- `GET /api/admin/analytics` -> daily, monthly, and student-wise attendance analytics

## Example Requests

### Submit Registration Request

```json
POST /api/auth/register
{
  "name": "Rohan Gupta",
  "studentClass": "CLASS_9",
  "email": "rohan@example.com"
}
```

### Login

```json
POST /api/auth/login
{
  "email": "admin@tuition.local",
  "password": "Admin@123"
}
```

### Mark Attendance by Fingerprint

```json
POST /api/attendance/scan
{
  "fingerprintId": "FP-1001"
}
```

## Security Notes

- Passwords are stored using BCrypt hashing.
- JWT secures authenticated API access.
- Role-based authorization protects admin-only endpoints.
- Student credentials are emailed only after admin approval.
- Approved users must change their temporary password on first login.
- Raw fingerprint images are not stored.
- Duplicate attendance for the same student on the same day is blocked.

## Suggested Production Hardening

- Move secrets and database credentials to environment variables or a secure secret store.
- Add refresh tokens or short-lived access tokens with rotation.
- Replace the simulated fingerprint resolver with a real hardware integration adapter.
- Add export endpoints for CSV/PDF if needed in the next phase.
- Add automated tests for auth, attendance rules, and analytics reporting.
