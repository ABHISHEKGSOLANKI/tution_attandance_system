import client from "./client";

export async function getDashboardSummary() {
  const today = new Date().toISOString().slice(0, 10);
  const [studentsResponse, pendingResponse, reportResponse] = await Promise.all([
    client.get("/admin/dashboard/students"),
    client.get("/admin/registration/pending-registrations"),
    client.get("/admin/attendance/report", { params: { date: today } })
  ]);

  return {
    totalStudents: studentsResponse.data.length,
    todayAttendanceCount: reportResponse.data.length,
    pendingApprovals: pendingResponse.data.length
  };
}

export async function getPendingRegistrations() {
  const response = await client.get("/admin/registration/pending-registrations");
  return response.data;
}

export async function approveRegistration(registrationId) {
  const response = await client.post(`/admin/registration/pending-registrations/${registrationId}/approve`);
  return response.data;
}

export async function rejectRegistration(registrationId) {
  const response = await client.post(`/admin/registration/pending-registrations/${registrationId}/reject`);
  return response.data;
}

export async function getAttendanceReport(filters) {
  const params = Object.fromEntries(
    Object.entries(filters).filter(([, value]) => value !== "" && value !== null && value !== undefined)
  );
  const response = await client.get("/admin/attendance/report", { params });
  return response.data;
}
