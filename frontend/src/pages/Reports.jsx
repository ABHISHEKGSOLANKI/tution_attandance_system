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
      <div className="stacked-page">
        <div className="module-card">
          <div className="module-head">
            <div>
              <h3>Attendance Filters</h3>
              <p>Use a single date or a range. Leave fields blank to broaden the report.</p>
            </div>
          </div>

          <form className="field-grid report-grid" onSubmit={handleSubmit}>
            <label>
              <span>Date</span>
              <input type="date" value={filters.date} onChange={(event) => setFilters({ ...filters, date: event.target.value })} />
            </label>
            <label>
              <span>Start Date</span>
              <input type="date" value={filters.startDate} onChange={(event) => setFilters({ ...filters, startDate: event.target.value })} />
            </label>
            <label>
              <span>End Date</span>
              <input type="date" value={filters.endDate} onChange={(event) => setFilters({ ...filters, endDate: event.target.value })} />
            </label>
            <label>
              <span>Class / Standard</span>
              <select value={filters.studentClass} onChange={(event) => setFilters({ ...filters, studentClass: event.target.value })}>
                <option value="">All classes</option>
                <option value="CLASS_9">9th</option>
                <option value="CLASS_10">10th</option>
              </select>
            </label>
            <label>
              <span>Student Name</span>
              <input type="text" placeholder="Search by name" value={filters.name} onChange={(event) => setFilters({ ...filters, name: event.target.value })} />
            </label>
            <div className="report-actions">
              <button type="submit">{loading ? "Loading..." : "Apply Filters"}</button>
            </div>
          </form>
        </div>

        <div className="module-card">
          <div className="module-head">
            <div>
              <h3>Attendance Report</h3>
              <p>{rows.length} records found.</p>
            </div>
          </div>

          <div className="table-shell">
            <table>
              <thead>
                <tr>
                  <th>Student Name</th>
                  <th>Class</th>
                  <th>Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id}>
                    <td>{row.studentName}</td>
                    <td>{row.studentClass === "CLASS_9" ? "9th" : "10th"}</td>
                    <td>{row.attendanceDate}</td>
                    <td>{row.status}</td>
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
