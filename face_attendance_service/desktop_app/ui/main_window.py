from __future__ import annotations

import tkinter as tk
from tkinter import ttk

from desktop_app.service.api_service import AttendanceApiService
from desktop_app.service.camera_service import CameraService
from desktop_app.service.face_service import FaceService
from desktop_app.storage.encoding_store import EncodingStore
from desktop_app.ui.attendance_tab import AttendanceTab
from desktop_app.ui.register_tab import RegisterTab


class MainWindow(tk.Tk):
    def __init__(self) -> None:
        super().__init__()
        self.title("Face Recognition Attendance")
        self.geometry("1280x820")
        self.minsize(1100, 760)
        self.configure(bg="#eef3f8")

        self._configure_styles()

        self.camera_service = CameraService()
        self.store: EncodingStore | None = None
        self.face_service: FaceService | None = None
        self.api_service: AttendanceApiService | None = None
        self.register_tab: RegisterTab | None = None
        self.attendance_tab: AttendanceTab | None = None

    def build(self, store: EncodingStore, face_service: FaceService, api_service: AttendanceApiService) -> None:
        self.store = store
        self.face_service = face_service
        self.api_service = api_service

        container = ttk.Frame(self, padding=18, style="App.TFrame")
        container.pack(fill="both", expand=True)
        container.columnconfigure(0, weight=1)
        container.rowconfigure(1, weight=1)

        header = ttk.Frame(container, style="App.TFrame")
        header.grid(row=0, column=0, sticky="ew")
        ttk.Label(header, text="Biometric Face Attendance", style="Hero.TLabel").pack(anchor="w")
        ttk.Label(
            header,
            text="Register students, preview the webcam live, and mark attendance with backend duplicate protection.",
            style="Muted.TLabel",
        ).pack(anchor="w", pady=(6, 18))

        notebook = ttk.Notebook(container)
        notebook.grid(row=1, column=0, sticky="nsew")

        self.register_tab = RegisterTab(notebook, self.camera_service, face_service, store)
        self.attendance_tab = AttendanceTab(notebook, self.camera_service, face_service, api_service)
        notebook.add(self.register_tab, text="Register Student")
        notebook.add(self.attendance_tab, text="Attendance")

        notebook.bind("<<NotebookTabChanged>>", self._handle_tab_change)
        self._activate_tab(0)
        self.protocol("WM_DELETE_WINDOW", self._on_close)

    def _handle_tab_change(self, event) -> None:
        notebook = event.widget
        self._activate_tab(notebook.index(notebook.select()))

    def _activate_tab(self, index: int) -> None:
        if self.register_tab is None or self.attendance_tab is None:
            return

        self.register_tab.on_hide()
        self.attendance_tab.on_hide()
        if index == 0:
            self.register_tab.on_show()
        else:
            self.attendance_tab.on_show()

    def _configure_styles(self) -> None:
        style = ttk.Style(self)
        style.theme_use("clam")
        style.configure("App.TFrame", background="#eef3f8")
        style.configure("Title.TLabel", font=("Segoe UI Semibold", 20), background="#eef3f8", foreground="#12344d")
        style.configure("Hero.TLabel", font=("Segoe UI Semibold", 26), background="#eef3f8", foreground="#12344d")
        style.configure("Muted.TLabel", font=("Segoe UI", 10), background="#eef3f8", foreground="#5d7285")
        style.configure("Status.TLabel", font=("Segoe UI", 11), background="#eef3f8", foreground="#12344d")
        style.configure("Preview.TLabel", background="#1a2633", foreground="#f4f8fb", padding=14, anchor="center")
        style.configure("TNotebook", background="#eef3f8", borderwidth=0)
        style.configure("TNotebook.Tab", font=("Segoe UI Semibold", 11), padding=(22, 12), background="#d8e5ef")
        style.map("TNotebook.Tab", background=[("selected", "#ffffff")], foreground=[("selected", "#12344d")])
        style.configure("TFrame", background="#eef3f8")
        style.configure("TLabelframe", background="#ffffff", foreground="#12344d")
        style.configure("TLabelframe.Label", background="#ffffff", foreground="#12344d", font=("Segoe UI Semibold", 11))
        style.configure("Treeview", font=("Segoe UI", 10), rowheight=28)
        style.configure("Treeview.Heading", font=("Segoe UI Semibold", 10))
        style.configure("TButton", font=("Segoe UI Semibold", 11), padding=(10, 8))

    def _on_close(self) -> None:
        if self.register_tab is not None:
            self.register_tab.on_hide()
        if self.attendance_tab is not None:
            self.attendance_tab.on_hide()
        self.camera_service.release()
        self.destroy()
