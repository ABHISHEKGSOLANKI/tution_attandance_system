import { useState } from "react";
import { Link } from "react-router-dom";
import { register } from "../api/auth";

export default function RegisterPage() {
  const [form, setForm] = useState({
    name: "",
    studentClass: "CLASS_9",
    email: ""
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");
    try {
      const data = await register(form);
      setSuccess(data.message);
      setForm({ name: "", studentClass: "CLASS_9", email: "" });
    } catch (err) {
      setError(err.response?.data?.error || "Registration failed");
    }
  }

  return (
    <div className="auth-shell">
      <section className="hero-panel accent">
        <p className="eyebrow">Student onboarding</p>
        <h1>Submit your registration request and wait for admin approval.</h1>
        <p>The admin will review your request, send your first login credentials by email, and then you will set your own password.</p>
      </section>

      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Register</h2>
        <label>
          Full Name
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </label>
        <label>
          Class
          <select value={form.studentClass} onChange={(e) => setForm({ ...form, studentClass: e.target.value })}>
            <option value="CLASS_9">9th</option>
            <option value="CLASS_10">10th</option>
          </select>
        </label>
        <label>
          Email
          <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
        </label>
        {error && <p className="error-text">{error}</p>}
        {success && <p className="helper-text">{success}</p>}
        <button type="submit">Submit Request</button>
        <p className="helper-text">
          Already registered? <Link to="/login">Back to login</Link>
        </p>
      </form>
    </div>
  );
}
