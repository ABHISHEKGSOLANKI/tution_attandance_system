import { useEffect, useState } from "react";
import { getAttendanceReport } from "../api/admin";
import Layout from "../components/Layout";
import Modal from "../components/Modal";

export default function Reports() {
  const [filters, setFilters] = useState({
    date: "",
    startDate: "",
    endDate: "",
    studentClass: "",
    name: ""
  });
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState({ open: false, tone: "error", title: "", message: "" });

  useEffect(() => {
    loadReport({});
  }, []);

  async function loadReport(nextFilters) {
    try {
      setLoading(true);
      const response = await getAttendanceReport(nextFilters);
      setRows(response);
    } catch (error) {
      setModal({
        open: true,
        tone: "error",
        title: "Report load failed",
        message: error.response?.data?.detail || "Could not load attendance report."
      });
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    await loadReport(filters);
  }

  return (
    <Layout title="Reports" subtitle="Filter attendance history by date, class, or student name.">
      <div className="grid gap-5">
        <div className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h3 className="text-xl font-semibold text-slate-900">Attendance Filters</h3>
              <p className="mt-1 text-sm text-slate-600">Use a single date or a range. Leave fields blank to broaden the report.</p>
            </div>
          </div>

          <form className="grid items-end gap-4 lg:grid-cols-3" onSubmit={handleSubmit}>
            <label className="grid gap-2 text-sm font-semibold text-slate-700">
              <span>Date</span>
              <input className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3.5 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200" type="date" value={filters.date} onChange={(event) => setFilters({ ...filters, date: event.target.value })} />
            </label>
            <label className="grid gap-2 text-sm font-semibold text-slate-700">
              <span>Start Date</span>
              <input className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3.5 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200" type="date" value={filters.startDate} onChange={(event) => setFilters({ ...filters, startDate: event.target.value })} />
            </label>
            <label className="grid gap-2 text-sm font-semibold text-slate-700">
              <span>End Date</span>
              <input className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3.5 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200" type="date" value={filters.endDate} onChange={(event) => setFilters({ ...filters, endDate: event.target.value })} />
            </label>
            <label className="grid gap-2 text-sm font-semibold text-slate-700">
              <span>Class / Standard</span>
              <select className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3.5 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200" value={filters.studentClass} onChange={(event) => setFilters({ ...filters, studentClass: event.target.value })}>
                <option value="">All classes</option>
                <option value="CLASS_9">9th</option>
                <option value="CLASS_10">10th</option>
              </select>
            </label>
            <label className="grid gap-2 text-sm font-semibold text-slate-700">
              <span>Student Name</span>
              <input className="w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3.5 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200" type="text" placeholder="Search by name" value={filters.name} onChange={(event) => setFilters({ ...filters, name: event.target.value })} />
            </label>
            <div className="flex flex-wrap gap-3">
              <button
                className="rounded-2xl bg-slate-800 px-5 py-3 text-sm font-semibold text-white transition hover:-translate-y-px hover:bg-slate-700"
                type="submit"
              >
                {loading ? "Loading..." : "Apply Filters"}
              </button>
            </div>
          </form>
        </div>

        <div className="rounded-[24px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.78)] p-6 backdrop-blur-[14px]">
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h3 className="text-xl font-semibold text-slate-900">Attendance Report</h3>
              <p className="mt-1 text-sm text-slate-600">{rows.length} records found.</p>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="border-b border-slate-200 px-2 py-4 text-left text-sm font-semibold text-slate-700">Student Name</th>
                  <th className="border-b border-slate-200 px-2 py-4 text-left text-sm font-semibold text-slate-700">Class</th>
                  <th className="border-b border-slate-200 px-2 py-4 text-left text-sm font-semibold text-slate-700">Date</th>
                  <th className="border-b border-slate-200 px-2 py-4 text-left text-sm font-semibold text-slate-700">Status</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id}>
                    <td className="border-b border-slate-200 px-2 py-4 text-sm text-slate-600">{row.studentName}</td>
                    <td className="border-b border-slate-200 px-2 py-4 text-sm text-slate-600">{row.studentClass === "CLASS_9" ? "9th" : "10th"}</td>
                    <td className="border-b border-slate-200 px-2 py-4 text-sm text-slate-600">{row.attendanceDate}</td>
                    <td className="border-b border-slate-200 px-2 py-4 text-sm text-slate-600">{row.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <Modal
        open={modal.open}
        tone={modal.tone}
        title={modal.title}
        message={modal.message}
        onClose={() => setModal((current) => ({ ...current, open: false }))}
      />
    </Layout>
  );
}
