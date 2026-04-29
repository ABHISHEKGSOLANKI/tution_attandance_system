from __future__ import annotations

from datetime import datetime
import base64

import cv2
import face_recognition
import numpy as np
import requests

from app.core.config import settings
from app.core.exceptions import (
    CameraUnavailableError,
    MultipleFacesDetectedError,
    NoFaceDetectedError,
    UnknownFaceError,
)
from app.services.backend_client import BackendAttendanceClient
from app.services.storage_service import FaceEncodingStorage


class FaceRecognitionService:
    def __init__(self, storage: FaceEncodingStorage, backend_client: BackendAttendanceClient) -> None:
        self.storage = storage
        self.backend_client = backend_client

    def register_face(self, student_id: str, show_preview: bool = True) -> int:
        encodings: list[np.ndarray] = []
        camera = cv2.VideoCapture(settings.camera_index)
        if not camera.isOpened():
            raise CameraUnavailableError("Webcam is not available")

        try:
            while len(encodings) < settings.capture_count:
                frame = self._read_frame(camera)
                try:
                    locations, face_encodings = self._extract_single_face_encoding(frame)
                    encodings.append(face_encodings[0])
                    self._render_preview(
                        frame,
                        locations,
                        f"Face detected - sample {len(encodings)}/{settings.capture_count}",
                        show_preview,
                    )
                    cv2.waitKey(700)
                except (NoFaceDetectedError, MultipleFacesDetectedError) as exc:
                    self._render_preview(frame, [], str(exc), show_preview)
                    cv2.waitKey(400)
            self.storage.save_student_encodings(student_id, encodings)
            return len(encodings)
        finally:
            camera.release()
            cv2.destroyAllWindows()

    def register_face_from_images(self, student_id: str, images: list[str]) -> int:
        encodings: list[np.ndarray] = []
        for image in images:
            frame = self._decode_image(image)
            _, face_encodings = self._extract_single_face_encoding(frame)
            encodings.append(face_encodings[0])
        self.storage.save_student_encodings(student_id, encodings)
        return len(encodings)

    def capture_and_match(self, show_preview: bool = True) -> tuple[str, datetime, bool]:
        camera = cv2.VideoCapture(settings.camera_index)
        if not camera.isOpened():
            raise CameraUnavailableError("Webcam is not available")

        try:
            frame = self._read_frame(camera)
            locations, face_encodings = self._extract_single_face_encoding(frame)
            try:
                match_student_id = self._match_encoding(face_encodings[0])
            except UnknownFaceError:
                self._render_preview(frame, locations, "Not recognized", show_preview)
                cv2.waitKey(1200)
                raise
            timestamp = datetime.now()
            self._render_preview(frame, locations, f"Matched: {match_student_id}", show_preview)
            try:
                backend_marked = self.backend_client.mark_attendance(match_student_id, timestamp)
            except requests.RequestException:
                backend_marked = False
            cv2.waitKey(1200)
            return match_student_id, timestamp, backend_marked
        finally:
            camera.release()
            cv2.destroyAllWindows()

    def capture_and_match_from_image(self, image: str) -> tuple[str, datetime, bool]:
        frame = self._decode_image(image)
        _, face_encodings = self._extract_single_face_encoding(frame)
        match_student_id = self._match_encoding(face_encodings[0])
        timestamp = datetime.now()
        try:
            backend_marked = self.backend_client.mark_attendance(match_student_id, timestamp)
        except requests.RequestException:
            backend_marked = False
        return match_student_id, timestamp, backend_marked

    def _match_encoding(self, unknown_encoding: np.ndarray) -> str:
        all_encodings = self.storage.load_all()
        best_match_student_id: str | None = None
        best_distance = 1.0

        for student_id, known_encodings in all_encodings.items():
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
            raise UnknownFaceError("Face not recognized")
        return best_match_student_id

    def _extract_single_face_encoding(self, frame: np.ndarray) -> tuple[list[tuple[int, int, int, int]], list[np.ndarray]]:
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        face_locations = face_recognition.face_locations(rgb_frame, model="hog")
        if not face_locations:
            raise NoFaceDetectedError("No face detected. Please look at the camera and try again.")
        if len(face_locations) > 1:
            raise MultipleFacesDetectedError("Multiple faces detected. Only one face should be visible.")

        encodings = face_recognition.face_encodings(rgb_frame, known_face_locations=face_locations)
        if not encodings:
            raise NoFaceDetectedError("Face detected, but encoding could not be generated.")
        return face_locations, encodings

    def _read_frame(self, camera: cv2.VideoCapture) -> np.ndarray:
        success, frame = camera.read()
        if not success or frame is None:
            raise CameraUnavailableError("Failed to read from webcam")
        return frame

    def _decode_image(self, image: str) -> np.ndarray:
        if "," in image:
            image = image.split(",", 1)[1]
        image_bytes = base64.b64decode(image)
        frame = cv2.imdecode(np.frombuffer(image_bytes, dtype=np.uint8), cv2.IMREAD_COLOR)
        if frame is None:
            raise NoFaceDetectedError("Invalid image data received from browser")
        return frame

    def _render_preview(
        self,
        frame: np.ndarray,
        face_locations: list[tuple[int, int, int, int]],
        status_text: str,
        show_preview: bool,
    ) -> None:
        if not show_preview:
            return
        preview = frame.copy()
        for top, right, bottom, left in face_locations:
            cv2.rectangle(preview, (left, top), (right, bottom), (0, 180, 0), 2)
        cv2.putText(preview, status_text, (20, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 180, 0), 2)
        cv2.imshow(settings.preview_window_name, preview)
