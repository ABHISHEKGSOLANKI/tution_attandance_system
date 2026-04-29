from pathlib import Path


class Settings:
    base_dir = Path(__file__).resolve().parents[2]
    data_dir = base_dir / "data"
    encodings_file = data_dir / "face_encodings.json"
    sample_file = data_dir / "sample_face_encodings.json"

    camera_index = 0
    capture_count = 5
    comparison_tolerance = 0.5
    preview_window_name = "Face Attendance Preview"
    preview_enabled = True
    backend_attendance_url = "http://localhost:8082/admin/attendance/face-mark"
    backend_timeout_seconds = 10


settings = Settings()
