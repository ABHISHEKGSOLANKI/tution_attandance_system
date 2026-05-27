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
      const response = await client.post("/admin/attendance/mark", { fingerprintId: scanFingerprintId });
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
        <div className="grid gap-5 xl:grid-cols-3">
          <section className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
            <p className="text-xs uppercase tracking-[0.18em] text-slate-500">Attendance Percentage</p>
            <h2 className="mt-3 text-5xl font-bold text-slate-900">{dashboard.attendancePercentage}%</h2>
            <p className="mt-2 text-sm text-slate-600">{dashboard.student.studentClass === "CLASS_9" ? "Class 9th" : "Class 10th"}</p>
          </section>

          <section className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
            <h3 className="text-xl font-semibold text-slate-900">Simulated Fingerprint Scan</h3>
            <form className="mt-4 grid gap-3" onSubmit={handleScan}>
              <input
                className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3.5 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200"
                placeholder="Enter your fingerprint ID"
                value={scanFingerprintId}
                onChange={(e) => setScanFingerprintId(e.target.value)}
                required
              />
              <button
                type="submit"
                className="rounded-2xl bg-slate-800 px-5 py-3 text-sm font-semibold text-white transition hover:-translate-y-px hover:bg-slate-700"
              >
                Mark Attendance
              </button>
            </form>
            {message && <p className="mt-3 text-sm text-slate-600">{message}</p>}
          </section>

          <section className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px] xl:col-span-3">
            <div className="mb-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <h3 className="text-xl font-semibold text-slate-900">Attendance History</h3>
              <span className="text-sm text-slate-500">{dashboard.attendanceHistory.length} records</span>
            </div>
            <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="border-b border-slate-200 px-2 py-4 text-left text-sm font-semibold text-slate-700">Date</th>
                  <th className="border-b border-slate-200 px-2 py-4 text-left text-sm font-semibold text-slate-700">Fingerprint ID</th>
                  <th className="border-b border-slate-200 px-2 py-4 text-left text-sm font-semibold text-slate-700">Scanned At</th>
                </tr>
              </thead>
              <tbody>
                {dashboard.attendanceHistory.map((item) => (
                  <tr key={item.id}>
                    <td className="border-b border-slate-200 px-2 py-4 text-sm text-slate-600">{item.attendanceDate}</td>
                    <td className="border-b border-slate-200 px-2 py-4 text-sm text-slate-600">{item.fingerprintId}</td>
                    <td className="border-b border-slate-200 px-2 py-4 text-sm text-slate-600">{new Date(item.scannedAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          </section>
        </div>
      )}
    </StudentLayout>
  );
}
