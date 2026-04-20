import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import StudentDashboard from "./pages/StudentDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import ChangePasswordPage from "./pages/ChangePasswordPage";
import { getStoredAuth } from "./api/session";

function ProtectedRoute({ children, role }) {
  const auth = getStoredAuth();
  if (!auth?.token) {
    return <Navigate to="/login" replace />;
  }
  if (auth.user?.passwordChangeRequired) {
    return <Navigate to="/change-password" replace />;
  }
  if (role && auth.user?.role !== role) {
    return <Navigate to={auth.user?.role === "ADMIN" ? "/admin" : "/student"} replace />;
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
        path="/admin"
        element={
          <ProtectedRoute role="ADMIN">
            <AdminDashboard />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}
