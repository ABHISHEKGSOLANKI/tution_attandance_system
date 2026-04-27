import { Navigate, Route, Routes } from "react-router-dom";
import { getStoredAuth } from "./api/session";
import Attendance from "./pages/Attendance";
import AdminDashboard from "./pages/AdminDashboard";
import ChangePasswordPage from "./pages/ChangePasswordPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import Registration from "./pages/Registration";
import Reports from "./pages/Reports";
import StudentDashboard from "./pages/StudentDashboard";

function ProtectedRoute({ children, role }) {
  const auth = getStoredAuth();
  if (!auth?.token) {
    return <Navigate to="/login" replace />;
  }
  if (auth.user?.passwordChangeRequired) {
    return <Navigate to="/change-password" replace />;
  }
  if (role && auth.user?.role !== role) {
    return <Navigate to={auth.user?.role === "ADMIN" ? "/dashboard" : "/student"} replace />;
  }
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/change-password" element={<ChangePasswordPage />} />
      <Route
        path="/student"
        element={
          <ProtectedRoute role="STUDENT">
            <StudentDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute role="ADMIN">
            <AdminDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin"
        element={
          <ProtectedRoute role="ADMIN">
            <Navigate to="/dashboard" replace />
          </ProtectedRoute>
        }
      />
      <Route
        path="/registration"
        element={
          <ProtectedRoute role="ADMIN">
            <Registration />
          </ProtectedRoute>
        }
      />
      <Route
        path="/attendance"
        element={
          <ProtectedRoute role="ADMIN">
            <Attendance />
          </ProtectedRoute>
        }
      />
      <Route
        path="/reports"
        element={
          <ProtectedRoute role="ADMIN">
            <Reports />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}
