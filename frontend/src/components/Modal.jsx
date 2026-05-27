export default function Modal({ open, tone = "success", title, message, onClose }) {
  if (!open) {
    return null;
  }

  const toneClassMap = {
    success: "shadow-[0_20px_60px_rgba(45,111,83,0.18)]",
    warning: "shadow-[0_20px_60px_rgba(160,108,30,0.18)]",
    error: "shadow-[0_20px_60px_rgba(168,46,46,0.18)]"
  };

  return (
    <div
      className="fixed inset-0 z-50 grid place-items-center bg-[rgba(9,17,28,0.42)] p-4"
      role="presentation"
      onClick={onClose}
    >
      <div
        className={`w-full max-w-[460px] rounded-3xl border border-slate-200 bg-white p-5 ${toneClassMap[tone] || toneClassMap.success}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex items-center justify-between gap-4">
          <h3 id="modal-title" className="text-lg font-semibold text-slate-900">
            {title}
          </h3>
          <button
            type="button"
            className="rounded-xl border border-[rgba(22,63,105,0.18)] bg-transparent px-4 py-2 text-sm font-medium text-[#163f69] transition hover:-translate-y-px"
            onClick={onClose}
          >
            Close
          </button>
        </div>
        <p className="mt-4 text-sm leading-6 text-slate-600">{message}</p>
      </div>
    </div>
  );
}
