import { useEffect, useState } from "react";
import { approveRegistration, getPendingRegistrations, rejectRegistration } from "../api/admin";
import Modal from "./Modal";

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
      <div className="module-card">
        <div className="module-head">
          <div>
            <h3>Account Registration Approval Requests</h3>
            <p>Review pending student registrations and approve or reject them.</p>
          </div>
          <span className="pill">{requests.length} pending</span>
        </div>

        {requests.length === 0 ? (
          <div className="empty-state">No pending requests right now.</div>
        ) : (
          <div className="request-list">
            {requests.map((request) => (
              <div className="request-row card-row" key={request.id}>
                <div>
                  <strong>{request.name}</strong>
                  <p>{request.email}</p>
                  <span className="meta-line">
                    Class: {request.studentClass === "CLASS_9" ? "9th" : "10th"} | Status: Pending
                  </span>
                </div>

                <div className="inline-actions">
                  <button type="button" onClick={() => handleApprove(request.id)} disabled={busyId === request.id}>
                    {busyId === request.id ? "Processing..." : "Approve"}
                  </button>
                  <button className="ghost-button" type="button" onClick={() => handleReject(request.id)} disabled={busyId === request.id}>
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
