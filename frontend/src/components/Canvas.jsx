import { useEffect, useRef, useState } from "react";

export default function Canvas({
  canvasActive,
  samples,
  status,
  activeTab,
  setCanvasActive,
  setStatus,
  setSamples,
  openModal
}) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);

    const [cameraActive, setCameraActive] = useState(false);

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

  function stopCamera(nextStatus) {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }

    setCanvasActive(false);
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

  if (!canvasActive) {
    return null;
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
      setCanvasActive(true);
        setCameraActive(true);
      setStatus("Camera ready. Capture 3 to 5 samples.");
      return true;
    } catch (error) {
      openModal("error", "Camera unavailable", "Please allow camera access and make sure a webcam is connected.");
      setStatus("Unable to start the camera.");
      return false;
    }
  }

    async function handleCapture() {
  
      if (!cameraActive) {
        await startCamera();
        return;
      }
  
      if (samples.length >= 5) {
        setStatus("You already captured the maximum 5 samples.");
        stopCamera("Maximum samples captured. Camera stopped.");
        setCanvasActive(false);
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

  return (
<div
  className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-2 sm:p-4 backdrop-blur-sm"
  onClick={() => stopCamera("Camera stopped")}
>
  <div
    className="relative w-full max-w-3xl overflow-hidden rounded-2xl sm:rounded-3xl bg-slate-950 shadow-2xl"
    onClick={(e) => e.stopPropagation()}
  >

    {/* HEADER */}
    <div className="flex items-center justify-between gap-2 border-b border-slate-800 px-3 py-3 sm:px-5 sm:py-4">

      <div className="flex min-w-0 items-center gap-2 sm:gap-4">
        <h3 className="truncate text-sm font-semibold text-white sm:text-lg">
          Camera Preview
        </h3>

        <span className="whitespace-nowrap rounded-full bg-[#edf4fb] px-2 py-1 text-[10px] font-bold text-[#163f69] sm:px-3 sm:py-2 sm:text-sm">
          {samples.length}/5
        </span>
      </div>

      <button
        type="button"
        onClick={() => stopCamera("Camera stopped")}
        className="rounded-lg border border-slate-700 px-2 py-1 text-xs text-white transition hover:bg-slate-800 sm:rounded-xl sm:px-4 sm:py-2 sm:text-sm"
      >
        Close
      </button>
    </div>

    {/* CAMERA */}
    <div className="relative bg-black">

      <video
        ref={videoRef}
        autoPlay
        playsInline
        muted
        className="max-h-[75vh] min-h-[320px] w-full object-cover"
      />

      <canvas
        ref={canvasRef}
        className="hidden"
      />

      {/* FACE FRAME */}
      <div className="pointer-events-none absolute inset-0 flex items-center justify-center">

        <div className="
          h-48 w-36
          rounded-[80px]
          border-4 border-white/80

          sm:h-72
          sm:w-56
          sm:rounded-[100px]

          shadow-[0_0_0_9999px_rgba(0,0,0,0.45)]
        " />

      </div>

      {/* TEXT */}
      <div className="absolute bottom-20 left-1/2 -translate-x-1/2 rounded-full bg-black/60 px-3 py-1 text-[10px] text-white backdrop-blur-md sm:bottom-28 sm:px-5 sm:py-2 sm:text-sm">
        Align your face inside the frame
      </div>

      {/* CAPTURE BUTTON */}
      <div className="absolute bottom-4 left-1/2 -translate-x-1/2 sm:bottom-6">

        <button
          type="button"
          onClick={handleCapture}
          className="
            flex items-center justify-center
            h-14 w-14
            rounded-full
            border-4 border-white
            bg-white/20
            backdrop-blur-md
            transition hover:scale-105

            sm:h-20 sm:w-20
          "
        >
          <div className="h-8 w-8 rounded-full bg-white sm:h-14 sm:w-14" />
        </button>

      </div>
    </div>
  </div>
</div>
  );
}