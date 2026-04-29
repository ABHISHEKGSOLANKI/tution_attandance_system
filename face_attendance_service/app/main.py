from fastapi import FastAPI

from app.api.routes import router
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(title="Face Attendance Service", version="1.0.0")
app.include_router(router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # for development
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)