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
      <div className="grid gap-5 xl:grid-cols-3 ">
        <section className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
          <p className="text-sm font-medium text-slate-600">Total Students</p>
          <h2 className="mt-1 text-5xl font-bold text-slate-900">{loading ? "..." : summary.totalStudents}</h2>
          <span className="mt-2 block text-sm text-slate-500">Approved students available for attendance</span>
        </section>

        <section className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
          <p className="text-sm font-medium text-slate-600">Today Attendance Count</p>
          <h2 className="mt-1 text-5xl font-bold text-slate-900">{loading ? "..." : summary.todayAttendanceCount}</h2>
          <span className="mt-2 block text-sm text-slate-500">Records marked for {new Date().toLocaleDateString()}</span>
        </section>

        <section className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[linear-gradient(135deg,rgba(22,63,105,0.94),rgba(57,119,132,0.92))] p-6 text-white backdrop-blur-[14px]">
          <p className="text-sm font-medium text-white/80">Pending Approvals</p>
          <h2 className="mt-1 text-5xl font-bold">{loading ? "..." : summary.pendingApprovals}</h2>
          <span className="mt-2 block text-sm text-white/80">Student accounts awaiting admin action</span>
        </section>

        <section className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px] xl:col-span-3">
          <h3 className="text-xl font-semibold text-slate-900">Admin Overview</h3>
          <p className="mt-3 text-sm leading-7 text-slate-600">
            Use the Registration page to capture student faces and process account approvals, the Attendance page to
            scan and mark attendance, and Reports to filter attendance history by date, class, or student name.
          </p>
        </section>
      </div>
    </Layout>
  );
}
