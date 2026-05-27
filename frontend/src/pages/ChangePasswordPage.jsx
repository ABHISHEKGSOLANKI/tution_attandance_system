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
  const inputClassName =
    "mt-2 w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-4 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200";

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
    <div className="grid min-h-screen grid-cols-1 bg-[radial-gradient(circle_at_top_left,rgba(255,204,153,0.45),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(116,180,200,0.25),transparent_28%),linear-gradient(135deg,#f5efe2_0%,#edf4fb_48%,#fdf7f2_100%)] lg:grid-cols-[1.1fr_0.9fr]">
      <section className="flex flex-col justify-center bg-[linear-gradient(160deg,#173b63,#2c6e74)] px-8 py-12 text-white md:px-16 lg:px-20">
        <p className="text-xs uppercase tracking-[0.18em] text-slate-200/90">First Login Security</p>
        <h1 className="mt-3 text-4xl font-extrabold leading-tight sm:text-5xl">
          Change your temporary password before continuing.
        </h1>
        <p className="mt-8 max-w-2xl text-lg leading-9 text-slate-100 md:text-xl md:leading-10">
          This is required the first time you sign in with the credentials sent by the admin.
        </p>
      </section>

      <form
        className="mx-auto my-auto grid w-full max-w-md gap-4 rounded-[24px] border border-slate-200 bg-[rgba(255,255,255,0.9)] p-8 shadow-2xl backdrop-blur-[14px]"
        onSubmit={handleSubmit}
      >
        <h2 className="text-center text-3xl font-bold text-slate-800">Update Password</h2>
        <label className="block text-sm font-semibold text-slate-700">
          Temporary Password
          <input
            className={inputClassName}
            type="password"
            value={form.currentPassword}
            onChange={(e) => setForm({ ...form, currentPassword: e.target.value })}
            required
          />
        </label>
        <label className="block text-sm font-semibold text-slate-700">
          New Password
          <input
            className={inputClassName}
            type="password"
            minLength="6"
            value={form.newPassword}
            onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
            required
          />
        </label>
        <label className="block text-sm font-semibold text-slate-700">
          Confirm New Password
          <input
            className={inputClassName}
            type="password"
            minLength="6"
            value={form.confirmPassword}
            onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
            required
          />
        </label>
        {error && <p className="text-sm font-medium text-red-700">{error}</p>}
        {message && <p className="text-sm text-slate-600">{message}</p>}
        <button
          type="submit"
          className="rounded-2xl bg-slate-800 px-5 py-4 text-base font-semibold text-white transition hover:-translate-y-px hover:bg-slate-700"
        >
          Save Password
        </button>
      </form>
    </div>
  );
}
