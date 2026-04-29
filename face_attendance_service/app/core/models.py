from datetime import datetime
from pydantic import BaseModel, Field


class RegisterRequest(BaseModel):
    student_id: str = Field(alias="studentId", min_length=1)


class BrowserRegisterRequest(BaseModel):
    student_id: str = Field(alias="studentId", min_length=1)
    images: list[str] = Field(min_length=1)


class RegisterResponse(BaseModel):
    student_id: str = Field(alias="studentId")
    captures_saved: int = Field(alias="capturesSaved")
    message: str


class BrowserCaptureRequest(BaseModel):
    image: str


class CaptureResponse(BaseModel):
    matched: bool
    student_id: str | None = Field(default=None, alias="studentId")
    message: str
    timestamp: datetime
    backend_marked: bool = Field(alias="backendMarked")


class BackendMarkPayload(BaseModel):
    student_id: str = Field(alias="studentId")
    timestamp: datetime
