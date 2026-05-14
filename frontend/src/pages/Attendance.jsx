import { useEffect, useRef, useState } from "react";
import { captureAttendanceFrame } from "../api/face";
import Layout from "../components/Layout";
import Modal from "../components/Modal";

const SCAN_INTERVAL_MS = 1800;
const MATCH_COOLDOWN_MS = 15000;
const ERROR_ANNOUNCEMENT_COOLDOWN_MS = 5000;

export default function Attendance() {
  const [status, setStatus] = useState("Start attendance to open the camera and begin continuous face scanning.");
  const [result, setResult] = useState(null);
  const [cameraActive, setCameraActive] = useState(false);
  const [isScanning, setIsScanning] = useState(false);
  const [modal, setModal] = useState({ open: false, tone: "success", title: "", message: "" });

  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const loopTimeoutRef = useRef(null);
  const scanningRef = useRef(false);
  const inFlightRef = useRef(false);
  const announcedStudentsRef = useRef(new Map());
  const lastErrorAnnouncementRef = useRef(0);

  useEffect(() => {
    function handleVisibilityChange() {
      if (document.hidden) {
        stopAttendance("Attendance paused because the browser tab is no longer active.");
      }
    }

    function handleWindowBlur() {
      stopAttendance("Attendance paused because the browser window lost focus.");
    }

    document.addEventListener("visibilitychange", handleVisibilityChange);
    window.addEventListener("blur", handleWindowBlur);

    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
      window.removeEventListener("blur", handleWindowBlur);
      stopAttendance();
      window.speechSynthesis.cancel();
    };
  }, []);

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
      setStatus("Camera ready. Attendance scanning is active. Keep one face visible.");
      return true;
    } catch (error) {
      setModal({
        open: true,
        tone: "error",
        title: "Camera unavailable",
        message: "Please allow browser camera access and make sure a webcam is connected."
      });
      setStatus("Unable to start the camera.");
      return false;
    }
  }

  function stopCamera(resetStatus = true) {
    if (loopTimeoutRef.current) {
      window.clearTimeout(loopTimeoutRef.current);
      loopTimeoutRef.current = null;
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }
    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }
    setCameraActive(false);
    setIsScanning(false);
    scanningRef.current = false;
    inFlightRef.current = false;
    if (resetStatus) {
      setStatus("Attendance stopped. Start attendance to resume scanning.");
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

  function speak(message) {
    if (!("speechSynthesis" in window) || !message) {
      return;
    }

    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(message);
    utterance.rate = 1;
    utterance.pitch = 1;
    window.speechSynthesis.speak(utterance);
  }

  function scheduleNextScan() {
    if (!scanningRef.current) {
      return;
    }

    loopTimeoutRef.current = window.setTimeout(() => {
      scanFrame();
    }, SCAN_INTERVAL_MS);
  }

  function shouldAnnounceStudent(studentId) {
    const now = Date.now();
    const lastSeen = announcedStudentsRef.current.get(studentId) || 0;
    if (now - lastSeen < MATCH_COOLDOWN_MS) {
      return false;
    }

    announcedStudentsRef.current.set(studentId, now);
    return true;
  }

  function announceError() {
    const now = Date.now();
    if (now - lastErrorAnnouncementRef.current < ERROR_ANNOUNCEMENT_COOLDOWN_MS) {
      return;
    }

    lastErrorAnnouncementRef.current = now;
    speak("Error while taking attendance");
  }

  async function scanFrame() {
    if (!scanningRef.current || inFlightRef.current) {
      return;
    }

    const image = captureFrame();
    if (!image) {
      scheduleNextScan();
      return;
    }

    try {
      inFlightRef.current = true;
      const response = await captureAttendanceFrame({ image });
      setResult(response);

      if (response.matched && response.studentId) {
        setStatus(response.message || `${response.studentId} present`);
        if (shouldAnnounceStudent(response.studentId)) {
          speak(`${response.studentId} present`);
        }
      } else {
        setStatus("Face not recognized. Keep one face centered and try again.");
      }
    } catch (error) {
      const detail = error.response?.data?.detail || "Could not complete attendance capture.";

      if (detail === "No face detected in frame") {
        setStatus("No face detected. Keep one face centered in the frame.");
      } else if (detail === "Multiple faces detected in frame") {
        setStatus("Multiple faces detected. Please keep only one face in the frame.");
      } else {
        setStatus("Attendance failed.");
        announceError();
        setModal({
          open: true,
          tone: "error",
          title: "Attendance failed",
          message: detail
        });
      }
    } finally {
      inFlightRef.current = false;
      scheduleNextScan();
    }
  }

  function stopAttendance(nextStatus) {
    stopCamera(false);
    if (nextStatus) {
      setStatus(nextStatus);
    } else {
      setStatus("Attendance stopped. Start attendance to resume scanning.");
    }
  }

  async function handleAttendance() {
    if (isScanning) {
      stopAttendance();
      return;
    }

    try {
      const started = await startCamera();
      if (!started) {
        return;
      }

      setStatus("Attendance scanning started. Keep one face visible.");
      scanningRef.current = true;
      setIsScanning(true);
      setResult(null);
      loopTimeoutRef.current = window.setTimeout(() => {
        scanFrame();
      }, 250);
    } catch (error) {
      setStatus("Attendance failed to start.");
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
          <div className="camera-overlay">
            {cameraActive ? "Keep one face centered" : "Camera is off"}
          </div>
        </div>
        <canvas ref={canvasRef} style={{ display: "none" }} />

        <div className="action-row">
          <button type="button" onClick={handleAttendance}>
            {isScanning ? "Stop Attendance" : "Start Attendance"}
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
