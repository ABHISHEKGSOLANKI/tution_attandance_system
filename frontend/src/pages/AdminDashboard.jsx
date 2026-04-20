import { useEffect, useMemo, useState } from "react";
import client from "../api/client";
import Layout from "../components/Layout";

export default function AdminDashboard() {
  const [students, setStudents] = useState([]);
  const [pendingRegistrations, setPendingRegistrations] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [attendance, setAttendance] = useState([]);
  const [filters, setFilters] = useState({ studentId: "", studentClass: "", date: "" });
  const [fingerprintForm, setFingerprintForm] = useState({ studentId: "", fingerprintId: "", provider: "SIMULATED_SCANNER" });
  const [message, setMessage] = useState("");

  async function loadAll() {
    const [studentsResponse, pendingResponse, analyticsResponse, attendanceResponse] = await Promise.all([
      client.get("/admin/students"),
      client.get("/auth/admin/pending-registrations"),
      client.get("/admin/analytics"),
      client.get("/admin/attendance")
    ]);
    setStudents(studentsResponse.data);
    setPendingRegistrations(pendingResponse.data);
    setAnalytics(analyticsResponse.data);
    setAttendance(attendanceResponse.data);
  }

  useEffect(() => {
    loadAll();
  }, []);

  async function applyFilters(event) {
    event.preventDefault();
    const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value));
    const response = await client.get("/admin/attendance", { params });
    setAttendance(response.data);
  }

  async function registerFingerprint(event) {
    event.preventDefault();
    try {
      await client.post(`/admin/students/${fingerprintForm.studentId}/fingerprint`, {
        fingerprintId: fingerprintForm.fingerprintId,
        provider: fingerprintForm.provider
      });
      setMessage("Fingerprint registered successfully");
      setFingerprintForm({ studentId: "", fingerprintId: "", provider: "SIMULATED_SCANNER" });
      loadAll();
    } catch (error) {
      setMessage(error.response?.data?.error || "Fingerprint registration failed");
    }
  }

  async function approveRegistration(studentId) {
    try {
      const response = await client.post(`/auth/admin/pending-registrations/${studentId}/approve`);
      setMessage(response.data.message);
      loadAll();
    } catch (error) {
      setMessage(error.response?.data?.error || "Approval failed");
    }
  }

  const lowAttendance = useMemo(
    () => students.filter((student) => student.attendancePercentage < 75),
    [students]
  );

  return (
    <Layout title="Admin Dashboard" subtitle="Manage students, register fingerprint IDs, and monitor class-wise attendance analytics.">
      <div className="dashboard-grid">
        <section className="panel">
          <p className="eyebrow">Low Attendance Alerts</p>
          <h2>{lowAttendance.length}</h2>
          <p>Students below 75% attendance.</p>
        </section>

        <section className="panel">
          <p className="eyebrow">Pending Requests</p>
          <h2>{pendingRegistrations.length}</h2>
          <p>Students waiting for approval and credential email.</p>
        </section>

        <section className="panel wide">
          <h3>Registration Requests</h3>
          {pendingRegistrations.length === 0 ? (
            <p className="helper-text">No pending registration requests right now.</p>
          ) : (
            <div className="request-list">
              {pendingRegistrations.map((request) => (
                <div className="request-row" key={request.id}>
                  <div>
                    <strong>{request.name}</strong>
                    <p className="helper-text">{request.email} - {request.studentClass === "CLASS_9" ? "9th" : "10th"}</p>
                  </div>
                  <button type="button" onClick={() => approveRegistration(request.id)}>Approve & Send Credentials</button>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="panel wide">
          <h3>Register Student Fingerprint</h3>
          <form className="inline-form" onSubmit={registerFingerprint}>
            <select value={fingerprintForm.studentId} onChange={(e) => setFingerprintForm({ ...fingerprintForm, studentId: e.target.value })} required>
              <option value="">Select student</option>
              {students.map((student) => (
                <option key={student.id} value={student.id}>
                  {student.name} ({student.studentClass === "CLASS_9" ? "9th" : "10th"})
                </option>
              ))}
            </select>
            <input
              placeholder="Fingerprint ID"
              value={fingerprintForm.fingerprintId}
              onChange={(e) => setFingerprintForm({ ...fingerprintForm, fingerprintId: e.target.value })}
              required
            />
            <button type="submit">Register</button>
          </form>
          {message && <p className="helper-text">{message}</p>}
        </section>

        <section className="panel table-panel wide">
          <div className="panel-header">
            <h3>Students</h3>
            <span>{students.length} approved</span>
          </div>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Class</th>
                <th>Email</th>
                <th>Status</th>
                <th>Fingerprint</th>
                <th>Attendance %</th>
              </tr>
            </thead>
            <tbody>
              {students.map((student) => (
                <tr key={student.id}>
                  <td>{student.name}</td>
                  <td>{student.studentClass === "CLASS_9" ? "9th" : "10th"}</td>
                  <td>{student.email}</td>
                  <td>{student.approved ? "Approved" : "Pending"}</td>
                  <td>{student.fingerprintRegistered ? "Registered" : "Pending"}</td>
                  <td>{student.attendancePercentage}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>

        <section className="panel wide">
          <h3>Attendance Filters</h3>
          <form className="inline-form" onSubmit={applyFilters}>
            <select value={filters.studentId} onChange={(e) => setFilters({ ...filters, studentId: e.target.value })}>
              <option value="">All students</option>
              {students.map((student) => (
                <option key={student.id} value={student.id}>{student.name}</option>
              ))}
            </select>
            <select value={filters.studentClass} onChange={(e) => setFilters({ ...filters, studentClass: e.target.value })}>
              <option value="">All classes</option>
              <option value="CLASS_9">9th</option>
              <option value="CLASS_10">10th</option>
            </select>
            <input type="date" value={filters.date} onChange={(e) => setFilters({ ...filters, date: e.target.value })} />
            <button type="submit">Apply</button>
          </form>
        </section>

        <section className="panel table-panel wide">
          <div className="panel-header">
            <h3>Attendance Records</h3>
            <span>{attendance.length} entries</span>
          </div>
          <table>
            <thead>
              <tr>
                <th>Student</th>
                <th>Class</th>
                <th>Date</th>
                <th>Fingerprint ID</th>
                <th>Scanned At</th>
              </tr>
            </thead>
            <tbody>
              {attendance.map((item) => (
                <tr key={item.id}>
                  <td>{item.studentName}</td>
                  <td>{item.studentClass === "CLASS_9" ? "9th" : "10th"}</td>
                  <td>{item.attendanceDate}</td>
                  <td>{item.fingerprintId}</td>
                  <td>{new Date(item.scannedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </Layout>
  );
}
