class FaceAttendanceError(Exception):
    """Base exception for face attendance service."""


class CameraUnavailableError(FaceAttendanceError):
    """Raised when webcam cannot be accessed."""


class NoFaceDetectedError(FaceAttendanceError):
    """Raised when no face is detected."""


class MultipleFacesDetectedError(FaceAttendanceError):
    """Raised when more than one face is detected."""


class UnknownFaceError(FaceAttendanceError):
    """Raised when no known face matches."""
