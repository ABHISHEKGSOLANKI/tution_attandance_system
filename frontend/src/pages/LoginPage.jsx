import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/auth";

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const inputClassName =
    "mt-2 w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-4 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200";

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const data = await login(form);
      navigate(data.user.role === "ADMIN" ? "/dashboard" : "/student");
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.detail || "Login failed");
      setLoading(false);
    }
  }

  return (
    <div className="grid min-h-screen grid-cols-1 bg-[radial-gradient(circle_at_top_left,rgba(255,204,153,0.45),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(116,180,200,0.25),transparent_28%),linear-gradient(135deg,#f5efe2_0%,#edf4fb_48%,#fdf7f2_100%)] lg:grid-cols-[1.1fr_0.9fr]">
      <section className="flex flex-col justify-center bg-[linear-gradient(160deg,#173b63,#2c6e74)] px-8 py-12 text-white md:px-16 lg:px-20">
        <h1 className="text-4xl font-bold">Attendance System</h1>
        <p className="mt-6 text-xl text-slate-200">Attendance and approval workflow</p>
        <h2 className="mt-16 max-w-2xl text-4xl font-extrabold leading-tight sm:text-5xl">
          Track attendance with secure student and admin access.
        </h2>
        <p className="mt-10 max-w-2xl text-lg leading-9 text-slate-100 md:text-xl md:leading-10">
          Students first submit their registration request with photo and admission details. After admin approval,
          they receive a generated username and temporary password by email.
        </p>
      </section>

      <section className="flex items-center justify-center px-6 py-10 md:px-8">
        <form
          onSubmit={handleSubmit}
          className="w-full max-w-md rounded-3xl border border-slate-200 bg-[rgba(255,255,255,0.9)] p-8 shadow-2xl backdrop-blur-[14px]"
        >
          <h2 className="text-center text-4xl font-bold text-slate-800">Login</h2>

          <div className="mt-10 space-y-6">
            <label className="block">
              <span className="text-sm font-semibold text-slate-700">Username</span>
              <input
                type="text"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                className={inputClassName}
                required
              />
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-slate-700">Password</span>
              <input
                type="password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                className={inputClassName}
                required
              />
            </label>

            {error && <p className="text-center text-sm font-medium text-red-700">{error}</p>}

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-2xl bg-slate-800 py-4 text-lg font-semibold text-white transition duration-200 hover:bg-slate-700 disabled:opacity-60"
            >
              {loading ? "Logging in..." : "Login"}
            </button>

            <p className="text-center text-sm text-slate-600">
              New student?{" "}
              <Link to="/register" className="font-semibold text-blue-700 hover:underline">
                Create an account
              </Link>
            </p>
          </div>
        </form>
      </section>
    </div>
  );
}
