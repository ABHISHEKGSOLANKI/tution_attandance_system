import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/auth";
import Select from "react-select";

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: "",
    middleName: "",
    lastName: "",
    mobile: "",
    countryCode: "+91",
    email: "",
    admissionId: "",
    standard: "CLASS_9",
    photo: null
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

const countries = [
  { value: "+91", label: "India (+91)" },
  { value: "+1", label: "USA (+1)" },
  { value: "+44", label: "UK (+44)" },
  { value: "+971", label: "UAE (+971)" },
];

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");
    try {
      const response = await register(form);
      setSuccess(response.message || "Registration submitted successfully. Redirecting to login...");
      window.setTimeout(() => {
        navigate("/login");
      }, 5000);
    } catch (err) {
      if (err.response?.data?.errors) {
        const validationErrors = Object.entries(
          err.response.data.errors
        )
          .map(([field, message]) => `${field}: ${message}`)
          .join(", ");

        setError(validationErrors);
      } else {
        setError(
          err.response?.data?.detail ||
          "An unexpected error occurred. Please try again later."
        );
      }
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

      <form className="auth-card register-card" onSubmit={handleSubmit}>
        <h2>Register</h2>
        <div className="register-grid">
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
            <div className="mobile-input">
              <Select
                options={countries}
                value={countries.find(
                  (c) => c.value === form.countryCode
                )}
                onChange={(selected) =>
                  setForm({
                    ...form,
                    countryCode: selected.value,
                  })
                }
                formatOptionLabel={(option, { context }) =>
                  context === "value"
                    ? option.value
                    : option.label
                }
              />

              <input
                type="tel"
                inputMode="numeric"
                placeholder="Enter mobile number"
                value={form.mobile}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, "");
                  setForm({ ...form, mobile: value });
                }}
                required
              />
            </div>
          </label>
          <label>
            Email
            <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          </label>
          <label>
            Admission ID
            <input type="number" min={0} max={200} value={form.admissionId} onChange={(e) => setForm({ ...form, admissionId: e.target.value })} required />
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
        </div>
        {error && <p className="error-text">{error}</p>}
        {success && <p className="helper-text">{success} You will be redirected in 5 seconds.</p>}
        <button type="submit">Submit Request</button>
        <p className="helper-text">
          Already registered? <Link to="/login">Back to login</Link>
        </p>
      </form>
    </div>
  );
}
