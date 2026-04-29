from __future__ import annotations

import queue
import threading
import tkinter as tk
from tkinter import messagebox, ttk

import cv2
from PIL import Image, ImageTk

from desktop_app.config import settings
from desktop_app.exceptions import CameraUnavailableError, FaceAttendanceError
from desktop_app.service.api_service import AttendanceApiService, AttendanceApiStatus
from desktop_app.service.camera_service import CameraService
from desktop_app.service.face_service import FaceService


class AttendanceTab(ttk.Frame):
    def __init__(self, master: tk.Misc, camera_service: CameraService, face_service: FaceService, api_service: AttendanceApiService) -> None:
        super().__init__(master, padding=20)
        self.camera_service = camera_service
        self.face_service = face_service
        self.api_service = api_service
        self.preview_job: str | None = None
        self.active = False
        self.scan_thread: threading.Thread | None = None
        self.scan_stop = threading.Event()
        self.event_queue: queue.Queue[tuple[str, object]] = queue.Queue()
        self.preview_image: ImageTk.PhotoImage | None = None

        self.status_var = tk.StringVar(value="Ready to scan attendance.")

        self.columnconfigure(0, weight=1)
        self.rowconfigure(1, weight=1)

        self._build_layout()
        self._poll_events()

    def _build_layout(self) -> None:
        ttk.Label(self, text="Attendance", style="Title.TLabel").grid(row=0, column=0, sticky="w")
        ttk.Label(self, text="Keep one student in front of the camera and start attendance scanning.").grid(
            row=1, column=0, sticky="w", pady=(4, 14)
        )

        preview_card = ttk.LabelFrame(self, text="Webcam Preview", padding=12)
        preview_card.grid(row=2, column=0, sticky="nsew")
        preview_card.columnconfigure(0, weight=1)
        preview_card.rowconfigure(0, weight=1)
        self.rowconfigure(2, weight=1)

        self.preview_label = ttk.Label(preview_card, text="Camera preview will appear here.", anchor="center", style="Preview.TLabel")
        self.preview_label.grid(row=0, column=0, sticky="nsew")

        action_row = ttk.Frame(self)
        action_row.grid(row=3, column=0, sticky="ew", pady=(16, 0))
        action_row.columnconfigure(0, weight=1)
        self.start_button = ttk.Button(action_row, text="Start Attendance", command=self.start_attendance)
        self.start_button.grid(row=0, column=0, sticky="ew", ipady=12)

        ttk.Label(self, textvariable=self.status_var, style="Status.TLabel").grid(row=4, column=0, sticky="ew", pady=(14, 0))

    def on_show(self) -> None:
        self.active = True
        self._schedule_preview()

    def on_hide(self) -> None:
        self.active = False
        self.scan_stop.set()
        if self.preview_job is not None:
            self.after_cancel(self.preview_job)
            self.preview_job = None
        self.camera_service.release()

    def start_attendance(self) -> None:
        if self.scan_thread and self.scan_thread.is_alive():
            return

        self.scan_stop.clear()
        self.status_var.set("Scanning for a known face...")
        self.start_button.configure(state="disabled")

        def worker() -> None:
            try:
                student_id, timestamp = self.face_service.scan_for_match(
                    self.camera_service,
                    lambda message: self.event_queue.put(("status", message)),
                    lambda message: self.event_queue.put(("warning", message)),
                    self.scan_stop.is_set,
                )
                result = self.api_service.mark_attendance(student_id, timestamp)
                self.event_queue.put(("api_result", (student_id, result)))
            except (FaceAttendanceError, CameraUnavailableError) as exc:
                self.event_queue.put(("error", str(exc)))
            finally:
                self.event_queue.put(("scan_done", None))

        self.scan_thread = threading.Thread(target=worker, daemon=True)
        self.scan_thread.start()

    def _schedule_preview(self) -> None:
        if not self.active:
            return
        self._update_preview()
        self.preview_job = self.after(settings.preview_poll_ms, self._schedule_preview)

    def _update_preview(self) -> None:
        try:
            frame = self.camera_service.read_frame()
        except CameraUnavailableError as exc:
            self.preview_label.configure(text=str(exc), image="")
            return

        preview = self._to_photo(frame)
        self.preview_image = preview
        self.preview_label.configure(image=preview, text="")

    def _poll_events(self) -> None:
        while True:
            try:
                event, payload = self.event_queue.get_nowait()
            except queue.Empty:
                break

            if event == "status":
                self.status_var.set(str(payload))
            elif event == "warning":
                messagebox.showwarning("Multiple faces detected", str(payload))
            elif event == "api_result":
                student_id, result = payload
                if result.status == AttendanceApiStatus.SUCCESS:
                    self.status_var.set(f"Attendance marked for {student_id}.")
                    messagebox.showinfo("Attendance marked", f"Attendance marked successfully for {student_id}.")
                elif result.status == AttendanceApiStatus.DUPLICATE:
                    self.status_var.set(f"Attendance already marked today for {student_id}.")
                    messagebox.showwarning("Already marked", result.message)
                else:
                    self.status_var.set(f"Matched {student_id}, but backend update failed.")
                    messagebox.showerror("Backend error", result.message)
            elif event == "error":
                self.status_var.set("Attendance scan stopped.")
                if str(payload) != "Attendance scan was stopped.":
                    messagebox.showerror("Attendance error", str(payload))
            elif event == "scan_done":
                self.start_button.configure(state="normal")

        self.after(100, self._poll_events)

    def _to_photo(self, frame) -> ImageTk.PhotoImage:
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        image = Image.fromarray(rgb_frame)
        image = image.resize((settings.preview_width, settings.preview_height))
        return ImageTk.PhotoImage(image)
