import { useEffect, useRef, useState } from "react";
import { registerFaceSamples } from "../api/face";
import Modal from "./Modal";

export default function FaceRegistration() {
  const [studentId, setStudentId] = useState("");
  const [samples, setSamples] = useState([]);
  const [status, setStatus] = useState("Start the camera, then capture 3 to 5 clear face samples.");
  const [busy, setBusy] = useState(false);
  const [modal, setModal] = useState({ open: false, tone: "success", title: "", message: "" });

  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);

  useEffect(() => {
    startCamera();
    return () => stopCamera();
  }, []);

  function openModal(tone, title, message) {
    setModal({ open: true, tone, title, message });
  }

  async function startCamera() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
      setStatus("Camera ready. Capture 3 to 5 samples.");
    } catch (error) {
      openModal("error", "Camera unavailable", "Please allow camera access and make sure a webcam is connected.");
    }
  }

  function stopCamera() {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }
  }

  function captureFrame() {
    const canvas = canvasRef.current;
    const video = videoRef.current;
    const context = canvas.getContext("2d");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0, canvas.width, canvas.height);
    return canvas.toDataURL("image/jpeg", 0.92);
  }

  function handleCapture() {
    if (samples.length >= 5) {
      setStatus("You already captured the maximum 5 samples.");
      return;
    }

    const image = captureFrame();
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
      <div className="module-card">
        <div className="module-head">
          <div>
            <h3>Face Registration</h3>
            <p>Capture multiple clear face samples before saving the student.</p>
          </div>
          <span className="pill">{samples.length}/5 samples</span>
        </div>

        <div className="camera-frame">
          <video ref={videoRef} autoPlay playsInline muted />
          <div className="camera-overlay">Align one face inside the frame</div>
        </div>

        <canvas ref={canvasRef} style={{ display: "none" }} />

        <div className="field-grid">
          <label>
            <span>Student ID</span>
            <input
              type="text"
              placeholder="Enter student ID"
              value={studentId}
              onChange={(event) => setStudentId(event.target.value)}
            />
          </label>
        </div>

        <div className="action-row">
          <button className="secondary-button" type="button" onClick={handleCapture}>
            Start Face Capture
          </button>
          <button type="button" onClick={handleRegister} disabled={busy}>
            {busy ? "Registering..." : "Register"}
          </button>
          <button className="ghost-button" type="button" onClick={handleClear}>
            Clear
          </button>
        </div>

        <div className="status-banner">{status}</div>

        <div className="shot-grid">
          {samples.map((image, index) => (
            <img key={index} src={image} alt={`sample ${index + 1}`} />
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
