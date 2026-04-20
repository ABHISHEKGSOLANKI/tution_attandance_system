# Biometric Attendance Middleware

Production-oriented Spring Boot middleware for offline-first biometric attendance capture and scheduled backend sync.

## What It Does

- Runs as a headless/background Spring Boot service
- Registers students by capturing a fingerprint template
- Stores fingerprint templates locally in MySQL after AES encryption
- Matches attendance scans locally without needing backend availability
- Syncs unsynced attendance records daily at `8:00 PM IST`
- Cleans up attendance records older than `30 days`
- Supports a mock fingerprint adapter today and a native SDK adapter path for Mantra/ZKTeco later
- Includes a JavaFX desktop UI for registration, attendance scanning, and dashboard monitoring

## Project Structure

```text
desktopapp/
  src/main/java/com/tuition/desktopapp/
    config/
    controller/
    dto/
    exception/
    model/
    repository/
    scheduler/
    service/
      fingerprint/
  src/main/resources/
    application.properties
    db/migration/V1__create_tables.sql
  schema.sql
```

## Main Endpoints

- `POST /students/register`
- `POST /attendance/scan`
- `POST /attendance/sync`

## Desktop UI Features

The JavaFX app includes three simple screens:

- Admin Dashboard
  - Register a student with `Student ID`, `Name`, and `Class`
  - Trigger fingerprint capture with a large action button
  - View all registered students in a table
- Attendance Screen
  - One large `Scan Finger` button for operators
  - Clear success or failure messages after each scan
  - Optional mock-template input for development/testing
- Dashboard
  - See today’s attendance list
  - See total present students
  - See sync status and pending-sync count
  - Trigger manual sync from the UI

## Registration Request

```json
POST /students/register
{
  "studentId": "STU-1001",
  "name": "Aman Verma",
  "studentClass": "9TH"
}
```

## Manual Scan Request

Real device mode:

```json
POST /attendance/scan
{}
```

Mock mode:

Use the predictable template `MOCK:<studentId>` for testing, for example:

```json
POST /attendance/scan
{
  "mockTemplate": "MOCK:STU-1001"
}
```

## Configuration

Edit [application.properties](./src/main/resources/application.properties):

- `spring.datasource.*` for MySQL
- `middleware.encryption.secret-key` for AES key material
- `middleware.backend.base-url` and `middleware.backend.attendance-bulk-path` for sync target
- `middleware.fingerprint.sdk-enabled=true` to switch from mock to SDK implementation
- `middleware.fingerprint.sdk-library-path` to point to the vendor DLL

## Scheduled Jobs

- Sync job: every day at `8:00 PM IST`
- Cleanup job: every day at `1:00 AM IST`

## How Sync Works

Unsynced local attendance rows are posted to:

- `POST {middleware.backend.base-url}{middleware.backend.attendance-bulk-path}`

Payload shape:

```json
{
  "records": [
    {
      "localRecordId": 1,
      "studentId": "STU-1001",
      "name": "Aman Verma",
      "studentClass": "9TH",
      "attendanceDate": "2026-04-15",
      "timestamp": "2026-04-15T09:00:00"
    }
  ]
}
```

On success, records are marked `is_synced = true`. On failure, they remain unsynced and retry in the next scheduled run.

## Plugging In A Real Fingerprint SDK

Current SDK integration seam is:

- [FingerprintService](./src/main/java/com/tuition/desktopapp/service/fingerprint/FingerprintService.java)
- [SdkFingerprintService](./src/main/java/com/tuition/desktopapp/service/fingerprint/SdkFingerprintService.java)

To integrate a real device:

1. Set `middleware.fingerprint.sdk-enabled=true`
2. Put the vendor DLL path in `middleware.fingerprint.sdk-library-path`
3. Replace the stub code in `SdkFingerprintService` with vendor method calls
4. Map vendor capture output into template strings
5. Map vendor match output into `FingerprintMatchResult`

Typical JNI/JNA work:

- Load DLL with JNA `Native.load(...)`
- Add SDK method signatures to `NativeFingerprintLibrary`
- Convert vendor byte buffers into Base64 or vendor-template strings
- Handle device disconnected/error codes and throw `DeviceUnavailableException`

## Running

Service mode only:

```bash
cd desktopapp
mvn spring-boot:run
```

JavaFX desktop UI:

```bash
cd desktopapp
mvn javafx:run
```

## Notes

- Raw fingerprint images are never stored.
- Matching is local and offline-first.
- One attendance record per student per day is enforced at both service and database levels.
- The mock implementation is intended for development and integration testing only.
