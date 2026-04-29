from __future__ import annotations

from datetime import datetime
from time import monotonic, sleep
from typing import Callable

import cv2
import face_recognition
import numpy as np

from desktop_app.config import settings
from desktop_app.exceptions import (
    FaceAttendanceError,
    MultipleFacesDetectedError,
    NoFaceDetectedError,
    UnknownFaceError,
)
from desktop_app.service.camera_service import CameraService
from desktop_app.storage.encoding_store import EncodingStore

StatusCallback = Callable[[str], None]
WarningCallback = Callable[[str], None]


class FaceService:
    def __init__(self, store: EncodingStore) -> None:
        self.store = store

    def capture_registration_samples(
        self,
        camera_service: CameraService,
        status_callback: StatusCallback,
        stop_requested: Callable[[], bool],
    ) -> list[np.ndarray]:
        captured: list[np.ndarray] = []
        started_at = monotonic()
        last_sample_at = 0.0
        status_callback("Show one face clearly to capture samples.")

        while len(captured) < settings.register_target_samples:
            if stop_requested():
                raise FaceAttendanceError("Registration was cancelled.")
            if monotonic() - started_at > settings.register_session_seconds:
                break

            frame = camera_service.read_frame()
            try:
                _, encodings = self.extract_single_face_encoding(frame)
            except NoFaceDetectedError:
                status_callback("No face detected. Look at the camera.")
                sleep(settings.scan_frame_delay_seconds)
                continue
            except MultipleFacesDetectedError:
                status_callback("Multiple faces detected. Keep only one face in frame.")
                sleep(settings.scan_frame_delay_seconds)
                continue

            now = monotonic()
            if now - last_sample_at < settings.sample_spacing_seconds:
                sleep(settings.scan_frame_delay_seconds)
                continue

            captured.append(encodings[0])
            last_sample_at = now
            status_callback(f"Captured sample {len(captured)}/{settings.register_target_samples}")
            sleep(settings.sample_spacing_seconds)

        if len(captured) < settings.register_min_samples:
            raise NoFaceDetectedError(
                f"Only {len(captured)} valid samples were captured. Please try again with better lighting and one visible face."
            )
        return captured

    def save_registration(self, student_id: str, encodings: list[np.ndarray]) -> None:
        if not student_id.strip():
            raise FaceAttendanceError("Student ID is required.")
        self.store.save_student_encodings(student_id, encodings)

    def scan_for_match(
        self,
        camera_service: CameraService,
        status_callback: StatusCallback,
        warning_callback: WarningCallback,
        stop_requested: Callable[[], bool],
    ) -> tuple[str, datetime]:
        multiple_faces_shown = False
        status_callback("Scanning for a known face.")

        while not stop_requested():
            frame = camera_service.read_frame()
            try:
                _, encodings = self.extract_single_face_encoding(frame)
            except NoFaceDetectedError:
                status_callback("Waiting for a face...")
                sleep(settings.scan_frame_delay_seconds)
                continue
            except MultipleFacesDetectedError:
                status_callback("Multiple faces detected. Keep only one face in frame.")
                if not multiple_faces_shown:
                    warning_callback("Multiple faces detected. Please allow only one student in front of the camera.")
                    multiple_faces_shown = True
                sleep(settings.scan_frame_delay_seconds)
                continue

            multiple_faces_shown = False
            try:
                student_id = self.match_encoding(encodings[0])
            except UnknownFaceError:
                status_callback("Face not recognized. Trying again...")
                sleep(settings.scan_frame_delay_seconds)
                continue

            status_callback(f"Matched student: {student_id}")
            return student_id, datetime.now()

        raise FaceAttendanceError("Attendance scan was stopped.")

    def match_encoding(self, unknown_encoding: np.ndarray) -> str:
        all_encodings = self.store.load_all()
        best_match_student_id: str | None = None
        best_distance = 1.0

        for student_id, known_encodings in all_encodings.items():
            if not known_encodings:
                continue

            matches = face_recognition.compare_faces(
                known_encodings,
                unknown_encoding,
                tolerance=settings.comparison_tolerance,
            )
            face_distances = face_recognition.face_distance(known_encodings, unknown_encoding)
            if len(face_distances) == 0:
                continue

            min_distance = float(np.min(face_distances))
            if any(matches) and min_distance < best_distance:
                best_distance = min_distance
                best_match_student_id = student_id

        if best_match_student_id is None:
            raise UnknownFaceError("Face not recognized.")
        return best_match_student_id

    def extract_single_face_encoding(self, frame: np.ndarray) -> tuple[list[tuple[int, int, int, int]], list[np.ndarray]]:
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        face_locations = face_recognition.face_locations(rgb_frame, model="hog")
        if not face_locations:
            raise NoFaceDetectedError("No face detected.")
        if len(face_locations) > 1:
            raise MultipleFacesDetectedError("Multiple faces detected.")

        encodings = face_recognition.face_encodings(rgb_frame, known_face_locations=face_locations)
        if not encodings:
            raise NoFaceDetectedError("Face detected but encoding could not be created.")
        return face_locations, encodings
