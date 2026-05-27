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
  const inputClassName =
    "mt-2 w-full rounded-2xl border border-slate-300 bg-white/90 px-4 py-3.5 text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-200";
  const labelClassName = "block text-sm font-semibold text-slate-700";

  const countries = [
    { value: "+91", label: "India (+91)" },
    { value: "+1", label: "USA (+1)" },
    { value: "+44", label: "UK (+44)" },
    { value: "+971", label: "UAE (+971)" }
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
    <div className="grid min-h-screen grid-cols-1 bg-[radial-gradient(circle_at_top_left,rgba(255,204,153,0.45),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(116,180,200,0.25),transparent_28%),linear-gradient(135deg,#f5efe2_0%,#edf4fb_48%,#fdf7f2_100%)] lg:grid-cols-[1.1fr_0.9fr]">
      <section className="flex flex-col justify-center bg-[linear-gradient(160deg,#7f4f24,#bc6c25)] px-8 py-12 text-white md:px-16 lg:px-20">
        <p className="text-xs uppercase tracking-[0.18em] text-orange-100/85">Student onboarding</p>
        <h1 className="mt-3 text-4xl font-extrabold leading-tight sm:text-5xl">
          Submit your registration request and wait for admin approval.
        </h1>
        <p className="mt-8 max-w-2xl text-lg leading-9 text-orange-50 md:text-xl md:leading-10">
          Share your admission details and photo. After approval, the system creates your username in the format
          <strong> firstname_admissionId</strong> and emails your temporary password.
        </p>
      </section>

      <form
        className="mx-auto my-auto grid w-full max-w-[680px] gap-4 rounded-[24px] border border-slate-200 bg-[rgba(255,255,255,0.9)] p-6 shadow-2xl backdrop-blur-[14px] md:p-7"
        onSubmit={handleSubmit}
      >
        <h2 className="text-center text-3xl font-bold text-slate-800">Register</h2>
        <div className="grid gap-4 md:grid-cols-2">
          <label className={labelClassName}>
            First Name
            <input className={inputClassName} value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required />
          </label>
          <label className={labelClassName}>
            Middle Name
            <input className={inputClassName} value={form.middleName} onChange={(e) => setForm({ ...form, middleName: e.target.value })} />
          </label>
          <label className={labelClassName}>
            Last Name
            <input className={inputClassName} value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required />
          </label>
          <label className={labelClassName}>
            Mobile
            <div className="mt-2 flex w-full items-center gap-2.5">
              <Select
                unstyled
                options={countries}
                value={countries.find((c) => c.value === form.countryCode)}
                onChange={(selected) =>
                  setForm({
                    ...form,
                    countryCode: selected?.value || "+91"
                  })
                }
                formatOptionLabel={(option, { context }) =>
                  context === "value" ? option.value : option.label
                }
                classNames={{
                  control: () =>
                    "flex min-h-[54px] w-[170px] min-w-[170px] items-center rounded-2xl border border-slate-300 bg-white/90 px-3 text-slate-900 shadow-none outline-none ring-0",
                  valueContainer: () => "p-0",
                  singleValue: () => "text-sm font-medium text-slate-900",
                  input: () => "m-0 p-0 text-sm",
                  indicatorsContainer: () => "gap-1",
                  indicatorSeparator: () => "hidden",
                  dropdownIndicator: () => "px-1 text-slate-500",
                  menu: () => "mt-2 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl",
                  menuList: () => "p-1",
                  option: ({ isFocused, isSelected }) =>
                    `cursor-pointer rounded-xl px-3 py-2 text-sm ${
                      isSelected ? "bg-slate-800 text-white" : isFocused ? "bg-slate-100 text-slate-900" : "text-slate-700"
                    }`
                }}
              />

              <input
                type="tel"
                inputMode="numeric"
                placeholder="Enter mobile number"
                className={`${inputClassName} mt-0 flex-1`}
                value={form.mobile}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, "");
                  setForm({ ...form, mobile: value });
                }}
                required
              />
            </div>
          </label>
          <label className={labelClassName}>
            Email
            <input className={inputClassName} type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          </label>
          <label className={labelClassName}>
            Admission ID
            <input className={inputClassName} type="number" min={0} max={200} value={form.admissionId} onChange={(e) => setForm({ ...form, admissionId: e.target.value })} required />
          </label>
          <label className={labelClassName}>
            Standard
            <select className={inputClassName} value={form.standard} onChange={(e) => setForm({ ...form, standard: e.target.value })}>
              <option value="CLASS_9">9th</option>
              <option value="CLASS_10">10th</option>
            </select>
          </label>
          <label className={labelClassName}>
            Photo
            <input className={inputClassName} type="file" accept="image/*" onChange={(e) => setForm({ ...form, photo: e.target.files?.[0] || null })} required />
          </label>
        </div>
        {error && <p className="text-sm font-medium text-red-700">{error}</p>}
        {success && <p className="text-sm text-slate-600">{success} You will be redirected in 5 seconds.</p>}
        <button
          type="submit"
          className="rounded-2xl bg-slate-800 px-5 py-4 text-base font-semibold text-white transition hover:-translate-y-px hover:bg-slate-700"
        >
          Submit Request
        </button>
        <p className="text-sm text-slate-600">
          Already registered?{" "}
          <Link to="/login" className="font-semibold text-blue-700 hover:underline">
            Back to login
          </Link>
        </p>
      </form>
    </div>
  );
}
