import { useEffect, useState } from "react";
import { getDashboardSummary } from "../api/admin";
import Layout from "../components/Layout";

export default function AdminDashboard() {
  const [summary, setSummary] = useState({
    totalStudents: 0,
    todayAttendanceCount: 0,
    pendingApprovals: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadSummary() {
      try {
        const response = await getDashboardSummary();
        setSummary(response);
      } finally {
        setLoading(false);
      }
    }

    loadSummary();
  }, []);

  return (
    <Layout title="Dashboard" subtitle="Track registrations, attendance activity, and approval load from one place.">
      <div className="dashboard-grid modern-grid">
        <section className="summary-card">
          <p>Total Students</p>
          <h2>{loading ? "..." : summary.totalStudents}</h2>
          <span>Approved students available for attendance</span>
        </section>

        <section className="summary-card">
          <p>Today Attendance Count</p>
          <h2>{loading ? "..." : summary.todayAttendanceCount}</h2>
          <span>Records marked for {new Date().toLocaleDateString()}</span>
        </section>

        <section className="summary-card accent-card">
          <p>Pending Approvals</p>
          <h2>{loading ? "..." : summary.pendingApprovals}</h2>
          <span>Student accounts awaiting admin action</span>
        </section>

        <section className="summary-panel wide-panel">
          <h3>Admin Overview</h3>
          <p>
            Use the Registration page to capture student faces and process account approvals, the Attendance page to
            scan and mark attendance, and Reports to filter attendance history by date, class, or student name.
          </p>
        </section>
      </div>
    </Layout>
  );
}
