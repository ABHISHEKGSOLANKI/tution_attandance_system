from pathlib import Path


class DesktopSettings:
    base_dir = Path(__file__).resolve().parents[1]
    data_dir = base_dir / "data"
    encodings_file = data_dir / "face_encodings.json"

    camera_index = 0
    comparison_tolerance = 0.5
    register_min_samples = 3
    register_target_samples = 5
    register_session_seconds = 20
    sample_spacing_seconds = 0.8
    scan_frame_delay_seconds = 0.2
    preview_width = 700
    preview_height = 420
    backend_attendance_url = "http://localhost:8082/admin/attendance/face-mark"
    backend_timeout_seconds = 10
    preview_poll_ms = 40


settings = DesktopSettings()
