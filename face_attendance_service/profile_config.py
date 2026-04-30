from __future__ import annotations

import os
from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parent


def _read_env_file(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    if not path.exists():
        return values

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def load_profile_settings() -> dict[str, str]:
    profile = os.getenv("FACE_APP_PROFILE") or os.getenv("APP_PROFILE") or "dev"
    env_values = _read_env_file(ROOT_DIR / f".env.{profile}")
    merged = {**env_values, **os.environ}
    merged.setdefault("FACE_APP_PROFILE", profile)
    return merged


def get_setting(name: str, default: str) -> str:
    return load_profile_settings().get(name, default)


def get_int(name: str, default: int) -> int:
    return int(get_setting(name, str(default)))


def get_float(name: str, default: float) -> float:
    return float(get_setting(name, str(default)))


def get_bool(name: str, default: bool) -> bool:
    return get_setting(name, str(default)).lower() in {"1", "true", "yes", "on"}
