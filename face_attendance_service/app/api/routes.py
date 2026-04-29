from datetime import datetime

from fastapi import APIRouter, HTTPException

from app.core.config import settings
from app.core.exceptions import (
    CameraUnavailableError,
    MultipleFacesDetectedError,
    NoFaceDetectedError,
    UnknownFaceError,
)
from app.core.models import (
    BrowserCaptureRequest,
    BrowserRegisterRequest,
    CaptureResponse,
    RegisterRequest,
    RegisterResponse,
)
from app.services.backend_client import BackendAttendanceClient
from app.services.face_service import FaceRecognitionService
from app.services.storage_service import FaceEncodingStorage

router = APIRouter()

storage = FaceEncodingStorage(settings.encodings_file)
backend_client = BackendAttendanceClient()
face_service = FaceRecognitionService(storage, backend_client)


@router.post("/register", response_model=RegisterResponse)
def register_face(request: RegisterRequest) -> RegisterResponse:
    try:
        captures_saved = face_service.register_face(request.student_id, show_preview=settings.preview_enabled)
        return RegisterResponse(
            studentId=request.student_id,
            capturesSaved=captures_saved,
            message="Face registration completed successfully",
        )
    except (CameraUnavailableError, NoFaceDetectedError, MultipleFacesDetectedError) as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/register/browser", response_model=RegisterResponse)
def register_face_from_browser(request: BrowserRegisterRequest) -> RegisterResponse:
    try:
        captures_saved = face_service.register_face_from_images(request.student_id, request.images)
        return RegisterResponse(
            studentId=request.student_id,
            capturesSaved=captures_saved,
            message="Face registration completed successfully from browser camera",
        )
    except (CameraUnavailableError, NoFaceDetectedError, MultipleFacesDetectedError) as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/capture", response_model=CaptureResponse)
def capture_attendance() -> CaptureResponse:
    try:
        student_id, timestamp, backend_marked = face_service.capture_and_match(show_preview=settings.preview_enabled)
        return CaptureResponse(
            matched=True,
            studentId=student_id,
            message=f"Matched: {student_id}" if backend_marked else f"Matched locally: {student_id}, but backend marking failed",
            timestamp=timestamp,
            backendMarked=backend_marked,
        )
    except UnknownFaceError:
        return CaptureResponse(
            matched=False,
            studentId=None,
            message="Unknown",
            timestamp=datetime.now(),
            backendMarked=False,
        )
    except (CameraUnavailableError, NoFaceDetectedError, MultipleFacesDetectedError) as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/capture/browser", response_model=CaptureResponse)
def capture_attendance_from_browser(request: BrowserCaptureRequest) -> CaptureResponse:
    try:
        student_id, timestamp, backend_marked = face_service.capture_and_match_from_image(request.image)
        return CaptureResponse(
            matched=True,
            studentId=student_id,
            message=f"Matched: {student_id}" if backend_marked else f"Matched locally: {student_id}, but backend marking failed",
            timestamp=timestamp,
            backendMarked=backend_marked,
        )
    except UnknownFaceError:
        return CaptureResponse(
            matched=False,
            studentId=None,
            message="Unknown",
            timestamp=datetime.now(),
            backendMarked=False,
        )
    except (CameraUnavailableError, NoFaceDetectedError, MultipleFacesDetectedError) as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
