class FaceAttendanceError(Exception):
    """Base exception for desktop face attendance flows."""


class CameraUnavailableError(FaceAttendanceError):
    """Raised when the webcam cannot be opened or read."""


class NoFaceDetectedError(FaceAttendanceError):
    """Raised when no face is detected in the frame."""


class MultipleFacesDetectedError(FaceAttendanceError):
    """Raised when more than one face is visible in the frame."""


class UnknownFaceError(FaceAttendanceError):
    """Raised when a face does not match any known student."""
