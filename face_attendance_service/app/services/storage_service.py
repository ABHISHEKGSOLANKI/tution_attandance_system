import base64
import json
import threading
from pathlib import Path

import numpy as np


class FaceEncodingStorage:
    def __init__(self, file_path: Path) -> None:
        self.file_path = file_path
        self._lock = threading.Lock()
        self.file_path.parent.mkdir(parents=True, exist_ok=True)
        if not self.file_path.exists():
            self.file_path.write_text("{}", encoding="utf-8")

    def load_all(self) -> dict[str, list[np.ndarray]]:
        with self._lock:
            raw = json.loads(self.file_path.read_text(encoding="utf-8"))
        return {
            student_id: [self._decode_encoding(item) for item in encodings]
            for student_id, encodings in raw.items()
        }

    def save_student_encodings(self, student_id: str, encodings: list[np.ndarray]) -> None:
        with self._lock:
            current = json.loads(self.file_path.read_text(encoding="utf-8"))
            current[student_id] = [self._encode_encoding(item) for item in encodings]
            self.file_path.write_text(json.dumps(current, indent=2), encoding="utf-8")

    def _encode_encoding(self, encoding: np.ndarray) -> str:
        return base64.b64encode(encoding.astype(np.float64).tobytes()).decode("utf-8")

    def _decode_encoding(self, encoded: str) -> np.ndarray:
        return np.frombuffer(base64.b64decode(encoded.encode("utf-8")), dtype=np.float64)
