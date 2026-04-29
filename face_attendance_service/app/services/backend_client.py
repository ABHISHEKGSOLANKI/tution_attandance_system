from datetime import datetime

import requests

from app.core.config import settings


class BackendAttendanceClient:
    def mark_attendance(self, student_id: str, timestamp: datetime) -> bool:
        response = requests.post(
            settings.backend_attendance_url,
            json={"studentId": str(student_id), "timestamp": timestamp.isoformat()},
            timeout=settings.backend_timeout_seconds,
        )
        response.raise_for_status()
        return True
