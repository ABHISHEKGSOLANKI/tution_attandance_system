import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { changePassword } from "../api/auth";
import { getStoredAuth } from "../api/session";

export default function ChangePasswordPage() {
  const navigate = useNavigate();
  const auth = getStoredAuth();
  const [form, setForm] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (form.newPassword !== form.confirmPassword) {
      setError("New password and confirm password must match");
      return;
    }

    try {
      await changePassword({ currentPassword: form.currentPassword, newPassword: form.newPassword });
      setMessage("Password updated successfully");
      navigate(auth?.user?.role === "ADMIN" ? "/admin" : "/student");
    } catch (err) {
      setError(err.response?.data?.error || "Password change failed");
    }
  }

  return (
    <div className="auth-shell">
      <section className="hero-panel">
        <p className="eyebrow">First Login Security</p>
        <h1>Change your temporary password before continuing.</h1>
        <p>This is required the first time you sign in with the credentials sent by the admin.</p>
      </section>

      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Update Password</h2>
        <label>
          Temporary Password
          <input
            type="password"
            value={form.currentPassword}
            onChange={(e) => setForm({ ...form, currentPassword: e.target.value })}
            required
          />
        </label>
        <label>
          New Password
          <input
            type="password"
            minLength="6"
            value={form.newPassword}
            onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
            required
          />
        </label>
        <label>
          Confirm New Password
          <input
            type="password"
            minLength="6"
            value={form.confirmPassword}
            onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
            required
          />
        </label>
        {error && <p className="error-text">{error}</p>}
        {message && <p className="helper-text">{message}</p>}
        <button type="submit">Save Password</button>
      </form>
    </div>
  );
}
