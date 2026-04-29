from desktop_app.config import settings
from desktop_app.service.api_service import AttendanceApiService
from desktop_app.service.face_service import FaceService
from desktop_app.storage.encoding_store import EncodingStore
from desktop_app.ui.main_window import MainWindow


def main() -> None:
    store = EncodingStore(settings.encodings_file)
    face_service = FaceService(store)
    api_service = AttendanceApiService()

    app = MainWindow()
    app.build(store, face_service, api_service)
    app.mainloop()


if __name__ == "__main__":
    main()
