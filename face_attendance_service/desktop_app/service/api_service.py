from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from enum import Enum

import requests

from desktop_app.config import settings


class AttendanceApiStatus(str, Enum):
    SUCCESS = "success"
    DUPLICATE = "duplicate"
    ERROR = "error"


@dataclass
class AttendanceApiResult:
    status: AttendanceApiStatus
    message: str


class AttendanceApiService:
    def __init__(self, base_url: str = settings.backend_attendance_url) -> None:
        self.base_url = base_url

    def mark_attendance(self, student_id: str, timestamp: datetime) -> AttendanceApiResult:
        try:
            response = requests.post(
                self.base_url,
                json={"studentId": student_id, "timestamp": timestamp.isoformat()},
                timeout=settings.backend_timeout_seconds,
            )
        except requests.RequestException as exc:
            return AttendanceApiResult(
                status=AttendanceApiStatus.ERROR,
                message=f"Could not connect to backend: {exc}",
            )

        if response.status_code == 200:
            return AttendanceApiResult(
                status=AttendanceApiStatus.SUCCESS,
                message="Attendance marked successfully.",
            )
        if response.status_code == 409:
            return AttendanceApiResult(
                status=AttendanceApiStatus.DUPLICATE,
                message="Attendance already marked today.",
            )

        try:
            error_payload = response.json()
            detail = error_payload.get("message") or error_payload.get("detail") or response.text
        except ValueError:
            detail = response.text or "Unknown backend error"

        return AttendanceApiResult(
            status=AttendanceApiStatus.ERROR,
            message=f"Backend error ({response.status_code}): {detail}",
        )
