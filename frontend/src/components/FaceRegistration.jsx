import { useEffect, useRef, useState } from "react";
import { registerFaceSamples } from "../api/face";
import Modal from "./Modal";

export default function FaceRegistration( { activeTab } ) {
  const [studentId, setStudentId] = useState("");
  const [samples, setSamples] = useState([]);
  const [status, setStatus] = useState("Start capture to open the camera, then collect 3 to 5 clear face samples.");
  const [busy, setBusy] = useState(false);
  const [cameraActive, setCameraActive] = useState(false);
  const [modal, setModal] = useState({ open: false, tone: "success", title: "", message: "" });

  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);

  useEffect(() => {
    if (activeTab !== "face") {
      stopCamera();
    }
  }, [activeTab]);

  useEffect(() => {
    function handleVisibilityChange() {
      if (document.hidden) {
        stopCamera("Camera stopped because the browser tab is no longer active.");
      }
    }

    function handleWindowBlur() {
      stopCamera("Camera stopped because the browser window lost focus.");
    }

    document.addEventListener("visibilitychange", handleVisibilityChange);
    window.addEventListener("blur", handleWindowBlur);

    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
      window.removeEventListener("blur", handleWindowBlur);
      stopCamera();
    };
  }, []);

  function openModal(tone, title, message) {
    setModal({ open: true, tone, title, message });
  }

  async function startCamera() {
    if (streamRef.current) {
      return true;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
      setCameraActive(true);
      setStatus("Camera ready. Capture 3 to 5 samples.");
      return true;
    } catch (error) {
      openModal("error", "Camera unavailable", "Please allow camera access and make sure a webcam is connected.");
      setStatus("Unable to start the camera.");
      return false;
    }
  }

  function stopCamera(nextStatus) {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }

    setCameraActive(false);
    if (nextStatus) {
      setStatus(nextStatus);
    }
  }

  function captureFrame() {
    const canvas = canvasRef.current;
    const video = videoRef.current;
    if (!canvas || !video || !video.videoWidth || !video.videoHeight) {
      return null;
    }
    const context = canvas.getContext("2d");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0, canvas.width, canvas.height);
    return canvas.toDataURL("image/jpeg", 0.92);
  }

  async function handleCapture() {
    if (!cameraActive) {
      await startCamera();
      return;
    }

    if (samples.length >= 5) {
      setStatus("You already captured the maximum 5 samples.");
      return;
    }

    const image = captureFrame();
    if (!image) {
      setStatus("Camera is still warming up. Please try capturing again.");
      return;
    }

    setSamples((current) => [...current, image]);
    setStatus(`Captured sample ${samples.length + 1}/5.`);
  }

  function handleClear() {
    setSamples([]);
    setStatus("Samples cleared. Capture fresh images.");
  }

  async function handleRegister() {
    if (!studentId || samples.length < 3) {
      openModal("warning", "More samples needed", "Enter a student ID and capture at least 3 images before registering.");
      return;
    }

    try {
      setBusy(true);
      const response = await registerFaceSamples({ studentId, images: samples });
      openModal("success", "Registration successful", response.message || "Face registration completed successfully.");
      setStatus("Registration successful.");
      setSamples([]);
      setStudentId("");
    } catch (error) {
      openModal("error", "Registration failed", error.response?.data?.detail || "Face registration failed.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <div className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h3 className="text-xl font-semibold text-slate-900">Face Registration</h3>
            <p className="mt-1 text-sm text-slate-600">Capture multiple clear face samples before saving the student.</p>
          </div>
          <span className="rounded-full bg-[#edf4fb] px-3 py-2 text-sm font-bold text-[#163f69]">{samples.length}/5 samples</span>
        </div>

        <div className="relative mx-auto aspect-[4/3] w-full max-w-[420px] overflow-hidden rounded-2xl bg-[#09111c]">
          <video ref={videoRef} autoPlay playsInline muted className="h-full w-full object-cover" />
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 rounded-full bg-[rgba(9,17,28,0.62)] px-4 py-2 text-sm text-white">
            {cameraActive ? "Align one face inside the frame" : "Camera is off"}
          </div>
        </div>

        <canvas ref={canvasRef} style={{ display: "none" }} />

        <div className="mt-5 grid gap-3">
          <label className="grid gap-2 text-sm font-semibold text-slate-700">
            <span>Student ID</span>
            <input
              type="text"
              placeholder="Enter user ID, username, or admission ID"
              value={studentId}
              onChange={(event) => setStudentId(event.target.value)}
              className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3.5 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200"
            />
          </label>
        </div>

        <div className="mt-5 flex flex-wrap gap-3">
          <button
            className="rounded-2xl border border-[rgba(22,63,105,0.18)] bg-transparent px-5 py-3 text-sm font-semibold text-[#163f69] transition hover:-translate-y-px"
            type="button"
            onClick={handleCapture}
          >
            {cameraActive ? "Capture Sample" : "Start Capture"}
          </button>
          <button
            className="rounded-2xl bg-slate-800 px-5 py-3 text-sm font-semibold text-white transition hover:-translate-y-px hover:bg-slate-700 disabled:opacity-60"
            type="button"
            onClick={handleRegister}
            disabled={busy}
          >
            {busy ? "Registering..." : "Register"}
          </button>
          <button
            className="rounded-2xl border border-[rgba(22,63,105,0.18)] bg-transparent px-5 py-3 text-sm font-semibold text-[#163f69] transition hover:-translate-y-px"
            type="button"
            onClick={handleClear}
          >
            Clear
          </button>
          {cameraActive && (
            <button
              className="rounded-2xl border border-[rgba(22,63,105,0.18)] bg-transparent px-5 py-3 text-sm font-semibold text-[#163f69] transition hover:-translate-y-px"
              type="button"
              onClick={() => stopCamera("Camera stopped.")}
            >
              Stop Camera
            </button>
          )}
        </div>

        <div className="mt-5 rounded-2xl bg-[#eff5fb] px-4 py-4 text-sm text-slate-600">{status}</div>
        <p className="mt-3 text-sm text-slate-600">
          Use the approved student's database ID, username, or admission ID so attendance can be written back to the backend.
        </p>

        <div className="mt-5 grid grid-cols-2 gap-3 md:grid-cols-5">
          {samples.map((image, index) => (
            <img key={index} src={image} alt={`sample ${index + 1}`} className="h-24 w-full rounded-2xl object-cover" />
          ))}
        </div>
      </div>

      <Modal
        open={modal.open}
        tone={modal.tone}
        title={modal.title}
        message={modal.message}
        onClose={() => setModal((current) => ({ ...current, open: false }))}
      />
    </>
  );
}
