from __future__ import annotations

import threading

import cv2
import numpy as np

from desktop_app.config import settings
from desktop_app.exceptions import CameraUnavailableError


class CameraService:
    def __init__(self, camera_index: int = settings.camera_index) -> None:
        self.camera_index = camera_index
        self._camera: cv2.VideoCapture | None = None
        self._lock = threading.Lock()

    def read_frame(self) -> np.ndarray:
        with self._lock:
            if self._camera is None or not self._camera.isOpened():
                self._camera = cv2.VideoCapture(self.camera_index)
            if self._camera is None or not self._camera.isOpened():
                raise CameraUnavailableError("Camera is not available. Please check the webcam connection.")

            success, frame = self._camera.read()
            if not success or frame is None:
                raise CameraUnavailableError("Failed to read a frame from the camera.")
            return frame.copy()

    def release(self) -> None:
        with self._lock:
            if self._camera is not None:
                self._camera.release()
                self._camera = None
