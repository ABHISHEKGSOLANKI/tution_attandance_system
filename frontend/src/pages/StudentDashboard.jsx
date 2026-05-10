import { useEffect, useState } from "react";
import client from "../api/client";
import StudentLayout from "../components/StudentLayout";

export default function StudentDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [scanFingerprintId, setScanFingerprintId] = useState("");
  const [message, setMessage] = useState("");

  async function loadDashboard() {
    const response = await client.get("/student/dashboard");
    setDashboard(response.data);
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  async function handleScan(event) {
    event.preventDefault();
    try {
      const response = await client.post("/attendance/scan", { fingerprintId: scanFingerprintId });
      setMessage(response.data.message);
      setScanFingerprintId("");
      loadDashboard();
    } catch (error) {
      setMessage(error.response?.data?.error || "Fingerprint scan failed");
    }
  }

  return (
    <StudentLayout title="Student Dashboard" subtitle="Review your attendance history and mark today's attendance with a valid fingerprint ID.">
      {dashboard && (
        <div className="dashboard-grid">
          <section className="panel stat-panel">
            <p className="eyebrow">Attendance Percentage</p>
            <h2>{dashboard.attendancePercentage}%</h2>
            <p>{dashboard.student.studentClass === "CLASS_9" ? "Class 9th" : "Class 10th"}</p>
          </section>

          <section className="panel">
            <h3>Simulated Fingerprint Scan</h3>
            <form className="inline-form" onSubmit={handleScan}>
              <input
                placeholder="Enter your fingerprint ID"
                value={scanFingerprintId}
                onChange={(e) => setScanFingerprintId(e.target.value)}
                required
              />
              <button type="submit">Mark Attendance</button>
            </form>
            {message && <p className="helper-text">{message}</p>}
          </section>

          <section className="panel table-panel wide">
            <div className="panel-header">
              <h3>Attendance History</h3>
              <span>{dashboard.attendanceHistory.length} records</span>
            </div>
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Fingerprint ID</th>
                  <th>Scanned At</th>
                </tr>
              </thead>
              <tbody>
                {dashboard.attendanceHistory.map((item) => (
                  <tr key={item.id}>
                    <td>{item.attendanceDate}</td>
                    <td>{item.fingerprintId}</td>
                    <td>{new Date(item.scannedAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        </div>
      )}
    </StudentLayout>
  );
}
