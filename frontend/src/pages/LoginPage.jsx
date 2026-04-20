import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/auth";

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    try {
      const data = await login(form);
      navigate(data.user.role === "ADMIN" ? "/admin" : "/student");
    } catch (err) {
      setError(err.response?.data?.error || "Login failed");
    }
  }

  return (
    <div className="auth-shell">
      <section className="hero-panel">
        <h1 className="logo">Gangadhar Tutions</h1>
        <p className="eyebrow">Fingerprint-first attendance</p>
        <h1>Track tuition attendance with secure student and admin access.</h1>
        <p>
          Students can view their own attendance percentage and records, while the tuition teacher can
          monitor daily and monthly trends from one dashboard.
        </p>
        <p>Students receive their temporary password only after the admin approves their registration request.</p>
      </section>

      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Login</h2>
        <label>
          Email
          <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
        </label>
        <label>
          Password
          <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
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
