import { useEffect, useRef, useState } from "react";
import { captureAttendanceFrame } from "../api/face";
import Layout from "../components/Layout";
import Modal from "../components/Modal";

export default function Attendance() {
  const [status, setStatus] = useState("Ready to start attendance scanning.");
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);
  const [modal, setModal] = useState({ open: false, tone: "success", title: "", message: "" });

  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);

  useEffect(() => {
    startCamera();
    return () => stopCamera();
  }, []);

  async function startCamera() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
      setStatus("Camera ready. Keep one face visible, then start attendance.");
    } catch (error) {
      setModal({
        open: true,
        tone: "error",
        title: "Camera unavailable",
        message: "Please allow browser camera access and make sure a webcam is connected."
      });
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

  async function handleAttendance() {
    try {
      setBusy(true);
      setStatus("Scanning face and matching attendance...");
      const response = await captureAttendanceFrame({ image: captureFrame() });
      setResult(response);
      setStatus(response.message || "Attendance completed.");
      setModal({
        open: true,
        tone: response.matched ? "success" : "warning",
        title: response.matched ? "Attendance result" : "Face not recognized",
        message: response.message || "Attendance completed."
      });
    } catch (error) {
      setModal({
        open: true,
        tone: "error",
        title: "Attendance failed",
        message: error.response?.data?.detail || "Could not complete attendance capture."
      });
      setStatus("Attendance failed.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <Layout title="Attendance" subtitle="Capture a live face and mark attendance through the Python recognition service.">
      <div className="module-card">
        <div className="module-head">
          <div>
            <h3>Face Attendance Capture</h3>
            <p>Use the live camera preview to scan one student at a time.</p>
          </div>
        </div>

        <div className="camera-frame large-frame">
          <video ref={videoRef} autoPlay playsInline muted />
          <div className="camera-overlay">Keep one face centered</div>
        </div>
        <canvas ref={canvasRef} style={{ display: "none" }} />

        <div className="action-row">
          <button type="button" onClick={handleAttendance} disabled={busy}>
            {busy ? "Scanning..." : "Start Attendance"}
          </button>
        </div>

        <div className="status-banner">{status}</div>

        {result && (
          <div className="result-card">
            <h4>Last Result</h4>
            <p>
              <strong>Student ID:</strong> {result.studentId || "Unknown"}
            </p>
            <p>
              <strong>Message:</strong> {result.message}
            </p>
          </div>
        )}
      </div>

      <Modal
        open={modal.open}
        tone={modal.tone}
        title={modal.title}
        message={modal.message}
        onClose={() => setModal((current) => ({ ...current, open: false }))}
      />
    </Layout>
  );
}
