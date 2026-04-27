import client from "./client";

export async function getDashboardSummary() {
  const today = new Date().toISOString().slice(0, 10);
  const [studentsResponse, pendingResponse, reportResponse] = await Promise.all([
    client.get("/admin/students"),
    client.get("/auth/admin/pending-registrations"),
    client.get("/attendance/report", { params: { date: today } })
  ]);

  return {
    totalStudents: studentsResponse.data.length,
    todayAttendanceCount: reportResponse.data.length,
    pendingApprovals: pendingResponse.data.length
  };
}

export async function getPendingRegistrations() {
  const response = await client.get("/auth/admin/pending-registrations");
  return response.data;
}

export async function approveRegistration(studentId) {
  const response = await client.post(`/auth/admin/pending-registrations/${studentId}/approve`);
  return response.data;
}

export async function rejectRegistration(studentId) {
  const response = await client.post(`/auth/admin/pending-registrations/${studentId}/reject`);
  return response.data;
}

export async function getAttendanceReport(filters) {
  const params = Object.fromEntries(
    Object.entries(filters).filter(([, value]) => value !== "" && value !== null && value !== undefined)
  );
  const response = await client.get("/attendance/report", { params });
  return response.data;
}
