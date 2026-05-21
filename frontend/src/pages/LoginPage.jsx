import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/auth";

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    try {
      const data = await login(form);
      navigate(data.user.role === "ADMIN" ? "/dashboard" : "/student");
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.detail || "Login failed");
    }
  }

  return (
    <div className="auth-shell">
      <section className="hero-panel">
        <h1 className="logo">Attendance System</h1>
        <p className="eyebrow">Attendance and approval workflow</p>
        <h1>Track attendance with secure student and admin access.</h1>
        <p>
          Students first submit their registration request with photo and admission details. After admin approval,
          they receive a generated username and temporary password by email.
        </p>
      </section>

      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Login</h2>
        <label>
          Username
          <input
            type="text"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
            required
          />
        </label>
        <label>
          Password
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />
        </label>
        {error && <p className="error-text">{error}</p>}
        <button type="submit">Sign In</button>
        <p className="helper-text">
          New student? <Link to="/register">Create an account</Link>
        </p>
      </form>
    </div>
  );
}
