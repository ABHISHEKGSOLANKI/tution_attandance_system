import { useEffect, useState } from "react";
import { approveRegistration, getPendingRegistrations, rejectRegistration } from "../api/admin";
import Modal from "./Modal";
import { backendUrl } from "../api/client";

export default function ApprovalRequests() {
  const [requests, setRequests] = useState([]);
  const [busyId, setBusyId] = useState(null);
  const [modal, setModal] = useState({ open: false, tone: "success", title: "", message: "" });

  useEffect(() => {
    loadRequests();
  }, []);

  async function loadRequests() {
    const response = await getPendingRegistrations();
    setRequests(response);
  }

  async function handleApprove(id) {
    try {
      setBusyId(id);
      const response = await approveRegistration(id);
      setModal({ open: true, tone: "success", title: "Approved", message: response.message });
      await loadRequests();
    } catch (error) {
      setModal({
        open: true,
        tone: "error",
        title: "Approval failed",
        message: error.response?.data?.error || "Could not approve the registration request."
      });
    } finally {
      setBusyId(null);
    }
  }

  async function handleReject(id) {
    try {
      setBusyId(id);
      const response = await rejectRegistration(id);
      setModal({ open: true, tone: "warning", title: "Rejected", message: response.message });
      await loadRequests();
    } catch (error) {
      setModal({
        open: true,
        tone: "error",
        title: "Rejection failed",
        message: error.response?.data?.error || "Could not reject the registration request."
      });
    } finally {
      setBusyId(null);
    }
  }

  return (
    <>
      <div className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h3 className="text-xl font-semibold text-slate-900">Account Registration Approval Requests</h3>
            <p className="mt-1 text-sm text-slate-600">Review pending student registrations and approve or reject them.</p>
          </div>
          <span className="rounded-full bg-[#edf4fb] px-3 py-2 text-sm font-bold text-[#163f69]">{requests.length} pending</span>
        </div>

        {requests.length === 0 ? (
          <div className="rounded-2xl bg-[#eff5fb] px-4 py-4 text-sm text-slate-600">No pending requests right now.</div>
        ) : (
          <div className="grid gap-4">
            {requests.map((request) => (
            <div
              className="flex flex-col gap-4 rounded-[18px] border border-slate-200 bg-[rgba(255,255,255,0.86)] p-4 xl:flex-row xl:items-center xl:justify-between"
              key={request.id}
            >
              <div className="flex items-start gap-3">
                <img
                  src={request.photoUrl ? `${backendUrl}${request.photoUrl.replace(/\\/g, "/")}` : ""}
                  alt="student"
                  className="h-[70px] w-[70px] rounded-[10px] object-cover"
                />

                <div>
                  <strong className="block text-sm font-semibold text-slate-900">
                    {request.admissionId} - {request.fullName || `${request.firstName} ${request.lastName}`}
                  </strong>
                  <p className="mt-1 text-sm text-slate-600">{request.email} | {request.mobile}</p>
                  <span className="mt-2 block text-sm text-slate-500">
                    Class: {request.standard === "CLASS_9" ? "9th" : "10th"} | Status: {request.status}
                  </span>
                </div>
              </div>

              <div className="flex flex-wrap gap-3">
                <button
                  className="rounded-2xl bg-slate-800 px-5 py-3 text-sm font-semibold text-white transition hover:-translate-y-px hover:bg-slate-700 disabled:opacity-60"
                  onClick={() => handleApprove(request.id)}
                  disabled={busyId === request.id}
                >
                  {busyId === request.id ? "Processing..." : "Approve"}
                </button>
                <button
                  className="rounded-2xl border border-[rgba(22,63,105,0.18)] bg-transparent px-5 py-3 text-sm font-semibold text-[#163f69] transition hover:-translate-y-px disabled:opacity-60"
                  onClick={() => handleReject(request.id)}
                  disabled={busyId === request.id}
                >
                  Reject
                </button>
              </div>
            </div>
            ))}
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
    </>
  );
}
