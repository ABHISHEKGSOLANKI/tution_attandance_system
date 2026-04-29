from __future__ import annotations

import queue
import threading
import tkinter as tk
from tkinter import messagebox, ttk

import cv2
from PIL import Image, ImageTk

from desktop_app.config import settings
from desktop_app.exceptions import CameraUnavailableError, FaceAttendanceError
from desktop_app.service.camera_service import CameraService
from desktop_app.service.face_service import FaceService
from desktop_app.storage.encoding_store import EncodingStore


class RegisterTab(ttk.Frame):
    def __init__(self, master: tk.Misc, camera_service: CameraService, face_service: FaceService, store: EncodingStore) -> None:
        super().__init__(master, padding=20)
        self.camera_service = camera_service
        self.face_service = face_service
        self.store = store
        self.preview_job: str | None = None
        self.active = False
        self.capture_thread: threading.Thread | None = None
        self.capture_stop = threading.Event()
        self.event_queue: queue.Queue[tuple[str, object]] = queue.Queue()
        self.pending_encodings = []
        self.preview_image: ImageTk.PhotoImage | None = None

        self.student_id_var = tk.StringVar()
        self.status_var = tk.StringVar(value="Enter a student ID, then capture 3 to 5 samples.")
        self.sample_var = tk.StringVar(value="No samples captured yet.")

        self.columnconfigure(0, weight=3)
        self.columnconfigure(1, weight=2)
        self.rowconfigure(0, weight=1)

        self._build_layout()
        self._poll_events()
        self.refresh_students()

    def _build_layout(self) -> None:
        left = ttk.Frame(self)
        left.grid(row=0, column=0, sticky="nsew", padx=(0, 16))
        left.columnconfigure(0, weight=1)

        title = ttk.Label(left, text="Register Student", style="Title.TLabel")
        title.grid(row=0, column=0, sticky="w")
        subtitle = ttk.Label(left, text="Capture multiple samples before saving the student.")
        subtitle.grid(row=1, column=0, sticky="w", pady=(4, 14))

        form = ttk.Frame(left)
        form.grid(row=2, column=0, sticky="ew")
        form.columnconfigure(0, weight=1)
        ttk.Label(form, text="Student ID").grid(row=0, column=0, sticky="w")
        ttk.Entry(form, textvariable=self.student_id_var, font=("Segoe UI", 12)).grid(row=1, column=0, sticky="ew", pady=(6, 12))

        preview_card = ttk.LabelFrame(left, text="Webcam Preview", padding=12)
        preview_card.grid(row=3, column=0, sticky="nsew")
        preview_card.columnconfigure(0, weight=1)
        preview_card.rowconfigure(0, weight=1)
        left.rowconfigure(3, weight=1)

        self.preview_label = ttk.Label(preview_card, text="Camera preview will appear here.", anchor="center", style="Preview.TLabel")
        self.preview_label.grid(row=0, column=0, sticky="nsew")

        status_card = ttk.Frame(left)
        status_card.grid(row=4, column=0, sticky="ew", pady=(14, 0))
        status_card.columnconfigure(0, weight=1)
        ttk.Label(status_card, textvariable=self.status_var, style="Status.TLabel").grid(row=0, column=0, sticky="ew")
        ttk.Label(status_card, textvariable=self.sample_var, style="Muted.TLabel").grid(row=1, column=0, sticky="w", pady=(6, 0))

        actions = ttk.Frame(left)
        actions.grid(row=5, column=0, sticky="ew", pady=(16, 0))
        actions.columnconfigure((0, 1), weight=1)
        self.capture_button = ttk.Button(actions, text="Capture Face", command=self.start_capture)
        self.capture_button.grid(row=0, column=0, sticky="ew", padx=(0, 8), ipady=10)
        self.register_button = ttk.Button(actions, text="Register", command=self.register_student)
        self.register_button.grid(row=0, column=1, sticky="ew", ipady=10)

        right = ttk.LabelFrame(self, text="Registered Students", padding=12)
        right.grid(row=0, column=1, sticky="nsew")
        right.columnconfigure(0, weight=1)
        right.rowconfigure(0, weight=1)

        columns = ("student_id", "samples", "updated_at")
        self.student_table = ttk.Treeview(right, columns=columns, show="headings", height=12)
        self.student_table.heading("student_id", text="Student ID")
        self.student_table.heading("samples", text="Samples")
        self.student_table.heading("updated_at", text="Updated")
        self.student_table.column("student_id", width=140)
        self.student_table.column("samples", width=90, anchor="center")
        self.student_table.column("updated_at", width=180)
        self.student_table.grid(row=0, column=0, sticky="nsew")

        scrollbar = ttk.Scrollbar(right, orient="vertical", command=self.student_table.yview)
        scrollbar.grid(row=0, column=1, sticky="ns")
        self.student_table.configure(yscrollcommand=scrollbar.set)

    def on_show(self) -> None:
        self.active = True
        self._schedule_preview()

    def on_hide(self) -> None:
        self.active = False
        self.capture_stop.set()
        if self.preview_job is not None:
            self.after_cancel(self.preview_job)
            self.preview_job = None
        self.camera_service.release()

    def start_capture(self) -> None:
        student_id = self.student_id_var.get().strip()
        if not student_id:
            messagebox.showerror("Missing student ID", "Enter a student ID before capturing face samples.")
            return
        if self.capture_thread and self.capture_thread.is_alive():
            return

        self.pending_encodings = []
        self.capture_stop.clear()
        self.status_var.set("Capturing samples. Keep one face visible and look at the camera.")
        self.sample_var.set("Preparing capture session...")
        self.capture_button.configure(state="disabled")

        def worker() -> None:
            try:
                encodings = self.face_service.capture_registration_samples(
                    self.camera_service,
                    lambda message: self.event_queue.put(("status", message)),
                    self.capture_stop.is_set,
                )
                self.event_queue.put(("captured", encodings))
            except (FaceAttendanceError, CameraUnavailableError) as exc:
                self.event_queue.put(("error", str(exc)))
            finally:
                self.event_queue.put(("capture_done", None))

        self.capture_thread = threading.Thread(target=worker, daemon=True)
        self.capture_thread.start()

    def register_student(self) -> None:
        student_id = self.student_id_var.get().strip()
        if not student_id:
            messagebox.showerror("Missing student ID", "Enter a student ID before registering.")
            return
        if len(self.pending_encodings) < settings.register_min_samples:
            messagebox.showwarning(
                "Capture needed",
                f"Capture at least {settings.register_min_samples} valid face samples before registering.",
            )
            return

        try:
            self.face_service.save_registration(student_id, self.pending_encodings)
        except FaceAttendanceError as exc:
            messagebox.showerror("Registration failed", str(exc))
            return

        self.status_var.set("Registration successful.")
        self.sample_var.set(f"Saved {len(self.pending_encodings)} samples for {student_id}.")
        messagebox.showinfo("Registration successful", f"Student {student_id} was registered successfully.")
        self.pending_encodings = []
        self.refresh_students()

    def refresh_students(self) -> None:
        for item in self.student_table.get_children():
            self.student_table.delete(item)
        for student in self.store.list_students():
            self.student_table.insert("", "end", values=(student.student_id, student.samples, student.updated_at or "-"))

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
            elif event == "captured":
                self.pending_encodings = list(payload)
                self.sample_var.set(f"{len(self.pending_encodings)} samples captured and ready to register.")
                self.status_var.set("Samples captured successfully. Click Register to save the student.")
                messagebox.showinfo("Capture complete", f"Captured {len(self.pending_encodings)} valid face samples.")
            elif event == "error":
                self.pending_encodings = []
                self.sample_var.set("No valid samples available.")
                self.status_var.set("Capture failed.")
                messagebox.showerror("Capture failed", str(payload))
            elif event == "capture_done":
                self.capture_button.configure(state="normal")

        self.after(100, self._poll_events)

    def _to_photo(self, frame) -> ImageTk.PhotoImage:
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        image = Image.fromarray(rgb_frame)
        image = image.resize((settings.preview_width, settings.preview_height))
        return ImageTk.PhotoImage(image)
