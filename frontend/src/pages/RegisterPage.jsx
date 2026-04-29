import { useState } from "react";
import { Link } from "react-router-dom";
import { register } from "../api/auth";

export default function RegisterPage() {
  const [form, setForm] = useState({
    firstName: "",
    middleName: "",
    lastName: "",
    mobile: "",
    email: "",
    admissionId: "",
    standard: "CLASS_9",
    photo: null
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
      setForm({
        firstName: "",
        middleName: "",
        lastName: "",
        mobile: "",
        email: "",
        admissionId: "",
        standard: "CLASS_9",
        photo: null
      });
      event.target.reset();
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.detail || "Registration failed");
    }
  }

  return (
    <div className="auth-shell">
      <section className="hero-panel accent">
        <p className="eyebrow">Student onboarding</p>
        <h1>Submit your registration request and wait for admin approval.</h1>
        <p>
          Share your admission details and photo. After approval, the system creates your username in the format
          <strong> firstname_admissionId</strong> and emails your temporary password.
        </p>
      </section>

      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Register</h2>
        <label>
          First Name
          <input value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required />
        </label>
        <label>
          Middle Name
          <input value={form.middleName} onChange={(e) => setForm({ ...form, middleName: e.target.value })} />
        </label>
        <label>
          Last Name
          <input value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required />
        </label>
        <label>
          Mobile
          <input value={form.mobile} onChange={(e) => setForm({ ...form, mobile: e.target.value })} required />
        </label>
        <label>
          Email
          <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
        </label>
        <label>
          Admission ID
          <input value={form.admissionId} onChange={(e) => setForm({ ...form, admissionId: e.target.value })} required />
        </label>
        <label>
          Standard
          <select value={form.standard} onChange={(e) => setForm({ ...form, standard: e.target.value })}>
            <option value="CLASS_9">9th</option>
            <option value="CLASS_10">10th</option>
          </select>
        </label>
        <label>
          Photo
          <input type="file" accept="image/*" onChange={(e) => setForm({ ...form, photo: e.target.files?.[0] || null })} required />
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
