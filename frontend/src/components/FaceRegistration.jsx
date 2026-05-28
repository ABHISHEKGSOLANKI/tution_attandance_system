import { useEffect, useRef, useState } from "react";
import { registerFaceSamples } from "../api/face";
import Modal from "./Modal";
import Canvas from "./Canvas";

export default function FaceRegistration( { activeTab } ) {
  const [studentId, setStudentId] = useState("");
  const [samples, setSamples] = useState([]);
  const [status, setStatus] = useState("Start capture to open the camera, then collect 3 to 5 clear face samples.");
  const [busy, setBusy] = useState(false);
  const [canvasActive, setCanvasActive] = useState(false);
  const [modal, setModal] = useState({ open: false, tone: "success", title: "", message: "" });

  function openModal(tone, title, message) {
    setModal({ open: true, tone, title, message });
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
      <div className="rounded-3xl border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h3 className="text-xl font-semibold text-slate-900">Face Registration</h3>
            <p className="mt-1 text-sm text-slate-600">Capture multiple clear face samples before saving the student.</p>
          </div>
        </div>

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
            onClick={() => setCanvasActive(true)}
          >
            {canvasActive ? "Capture Sample" : "Start Capture"}
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

      <Canvas
        canvasActive={canvasActive}
        samples={samples}
        status={status}
        activeTab={activeTab}
        setCanvasActive={setCanvasActive}
        setStatus={setStatus}
        setSamples={setSamples}
        openModal={openModal}
      />
    </>
  );
}
