from pathlib import Path

from profile_config import get_bool, get_float, get_int, get_setting


class Settings:
    base_dir = Path(__file__).resolve().parents[2]
    data_dir = base_dir / "data"
    encodings_file = Path(get_setting("FACE_ENCODINGS_FILE", str(data_dir / "face_encodings.json")))
    sample_file = Path(get_setting("FACE_SAMPLE_FILE", str(data_dir / "sample_face_encodings.json")))

    profile = get_setting("FACE_APP_PROFILE", "dev")
    camera_index = get_int("FACE_CAMERA_INDEX", 0)
    capture_count = get_int("FACE_CAPTURE_COUNT", 5)
    comparison_tolerance = get_float("FACE_COMPARISON_TOLERANCE", 0.5)
    preview_window_name = get_setting("FACE_PREVIEW_WINDOW_NAME", "Face Attendance Preview")
    preview_enabled = get_bool("FACE_PREVIEW_ENABLED", True)
    backend_attendance_url = get_setting("FACE_BACKEND_ATTENDANCE_URL", "http://127.0.0.1:8085/api/admin/attendance/face-mark")
    backend_timeout_seconds = get_int("FACE_BACKEND_TIMEOUT_SECONDS", 10)


settings = Settings()
