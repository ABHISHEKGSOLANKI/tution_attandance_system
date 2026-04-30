# Face Attendance Service

This project now supports two ways of working with face attendance:

- a FastAPI service for REST-based registration and capture
- a desktop Tkinter application with a live webcam preview and two operator tabs

Both flows use:

- OpenCV
- `face_recognition`
- local JSON storage for multiple encodings per student
- REST integration with the Java backend attendance API

## Desktop Features

The desktop application includes:

- `Register Student` tab
- `Attendance` tab
- live webcam preview inside the desktop window
- threaded capture so the UI stays responsive
- popup dialogs for success, warning, duplicate attendance, and backend failures
- local storage of multiple encodings per student
- backend call to `POST /attendance/mark`

Desktop behavior:

- Registration captures `3-5` valid samples, then saves only when `Register` is clicked.
- Attendance scans continuously until a known face is matched.
- If the backend returns `409`, the app shows `Attendance already marked today`.

## Project Structure

```text
face_attendance_service/
  app/
    api/
    core/
    services/
    main.py
  desktop_app/
    service/
      api_service.py
      camera_service.py
      face_service.py
    storage/
      encoding_store.py
    ui/
      attendance_tab.py
      main_window.py
      register_tab.py
    config.py
    exceptions.py
    main.py
  data/
    face_encodings.json
    sample_face_encodings.json
  run_desktop.py
  requirements.txt
  README.md
```

## Install

```bash
cd face_attendance_service
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

## Profiles

The face service supports four profiles through root env files:

- `.env.dev`
- `.env.qa`
- `.env.staging`
- `.env.prod`

Set the profile with:

```bash
set FACE_APP_PROFILE=dev
```

or in PowerShell:

```powershell
$env:FACE_APP_PROFILE="dev"
```

Replace `dev` with `qa`, `staging`, or `prod` as needed.

## Run The Desktop App

```bash
cd face_attendance_service
python run_desktop.py
```

If you use the Windows launcher instead of `python`:

```bash
py run_desktop.py
```

## Run The FastAPI Service

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8095
```

## Desktop Workflow

### Register Student Tab

1. Enter the student ID.
2. Click `Capture Face`.
3. Keep one face visible until `3-5` samples are collected.
4. Click `Register` to save the encodings locally.

### Attendance Tab

1. Click `Start Attendance`.
2. Keep one student in front of the camera.
3. The app keeps scanning until a face matches.
4. On match, it calls the Java backend and stops scanning.

## API Endpoints

### Register Face

```http
POST /register
Content-Type: application/json

{
  "studentId": "STU-1001"
}
```

### Browser Registration

```http
POST /register/browser
```

### Capture Attendance

```http
POST /capture
```

### Browser Attendance Capture

```http
POST /capture/browser
```

### Health Check

```http
GET /health
```

## Matching Logic

- `face_recognition.compare_faces(...)`
- default tolerance: `0.5`
- multiple encodings per student are supported
- only one face is allowed in frame during registration and attendance

## Storage

Face encodings are stored locally in `data/face_encodings.json`.

Each student entry keeps:

- multiple face encodings
- last update timestamp

## Java Backend Integration

The desktop app and FastAPI service both call:

- `POST /admin/attendance/face-mark`

Default backend URL:

- `http://localhost:8082/admin/attendance/face-mark`

Desktop configuration lives in:

- `desktop_app/config.py`

FastAPI configuration lives in:

- `app/core/config.py`

Payload:

```json
{
  "studentId": "STU-1001",
  "timestamp": "2026-04-26T10:30:00"
}
```

## Notes

- Keep only one face in front of the camera at a time.
- Good lighting improves registration quality a lot.
- Small datasets such as `50-100` students are a good fit for this approach.
- `face_recognition` requires `dlib`, so dependency setup must be done on the target Windows machine.
- The desktop UI opens the camera only while the active tab needs it and releases it on tab switch or app close.
