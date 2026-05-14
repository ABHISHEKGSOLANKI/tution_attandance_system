from pathlib import Path

from profile_config import get_float, get_int, get_setting


class DesktopSettings:
    base_dir = Path(__file__).resolve().parents[1]
    data_dir = base_dir / "data"
    encodings_file = Path(get_setting("FACE_ENCODINGS_FILE", str(data_dir / "face_encodings.json")))

    profile = get_setting("FACE_APP_PROFILE", "dev")
    camera_index = get_int("FACE_CAMERA_INDEX", 0)
    comparison_tolerance = get_float("FACE_COMPARISON_TOLERANCE", 0.5)
    register_min_samples = get_int("FACE_REGISTER_MIN_SAMPLES", 3)
    register_target_samples = get_int("FACE_REGISTER_TARGET_SAMPLES", 5)
    register_session_seconds = get_int("FACE_REGISTER_SESSION_SECONDS", 20)
    sample_spacing_seconds = get_float("FACE_SAMPLE_SPACING_SECONDS", 0.8)
    scan_frame_delay_seconds = get_float("FACE_SCAN_FRAME_DELAY_SECONDS", 0.2)
    preview_width = get_int("FACE_PREVIEW_WIDTH", 700)
    preview_height = get_int("FACE_PREVIEW_HEIGHT", 420)
    backend_attendance_url = get_setting("FACE_BACKEND_ATTENDANCE_URL", "http://127.0.0.1:8085/api/admin/attendance/face-mark")
    backend_timeout_seconds = get_int("FACE_BACKEND_TIMEOUT_SECONDS", 10)
    preview_poll_ms = get_int("FACE_PREVIEW_POLL_MS", 40)


settings = DesktopSettings()
