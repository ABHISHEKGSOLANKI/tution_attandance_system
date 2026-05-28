import { useState } from "react";
import { NavLink } from "react-router-dom";
import { clearStoredAuth, getStoredAuth } from "../api/session";
import { frontendPath } from "../api/client";

export default function Layout({ title, subtitle, children }) {
  const auth = getStoredAuth();
  const [collapsed, setCollapsed] = useState(false);
  const navItems = [
    { to: "/dashboard", label: "Dashboard", badge: "D" },
    { to: "/registration", label: "Registration", badge: "R" },
    { to: "/attendance", label: "Attendance", badge: "A" },
    { to: "/reports", label: "Reports", badge: "P" }
  ];

  function handleLogout() {
    clearStoredAuth();
    window.location.href = frontendPath + "login";
  }

  return (
<>
  {/* TOP HEADER */}
  <header className="border-b border-slate-200 bg-white/80 p-4 backdrop-blur-md">
    <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      
      <div className="min-w-0">
        <h1 className="text-2xl font-bold text-slate-900">
          {title}
        </h1>

        <p className="mt-1 text-sm text-slate-600">
          {subtitle}
        </p>
      </div>

      <div className="flex items-center justify-between gap-4">

        {/* MOBILE MENU BUTTON */}
        <button
          type="button"
          onClick={() => setCollapsed(!collapsed)}
          className="rounded-xl border border-slate-300 bg-white px-4 py-2 text-sm font-medium lg:hidden"
        >
          Menu
        </button>

        {auth?.user && (
          <div className="flex flex-col lg:items-end">
            <strong className="text-sm font-semibold text-slate-900">
              {auth.user.name}
            </strong>

            <button
              type="button"
              onClick={handleLogout}
              className="mt-1 rounded-xl border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100"
            >
              Logout
            </button>
          </div>
        )}
      </div>
    </div>
  </header>

  {/* MAIN LAYOUT */}
  <div className="flex min-h-screen bg-[radial-gradient(circle_at_top_left,rgba(255,204,153,0.45),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(116,180,200,0.25),transparent_28%),linear-gradient(135deg,#f5efe2_0%,#edf4fb_48%,#fdf7f2_100%)]">

    {/* SIDEBAR */}
    <aside
      className={`
        fixed top-0 left-0 z-50 h-full bg-linear-to-b from-[#163f69] to-[#0f2742] text-white transition-all duration-300

        ${collapsed ? "-translate-y-full lg:translate-y-0" : "translate-y-0"}

        w-full p-6
        lg:static
        lg:h-auto
        lg:translate-y-0

        ${collapsed ? "lg:w-20 lg:p-3" : "lg:w-64"}
      `}
    >

      {/* DESKTOP TOGGLE */}
      <button
        type="button"
        onClick={() => setCollapsed(!collapsed)}
        className="hidden lg:block rounded-xl border border-white px-4 py-2 text-sm"
      >
        Menu
      </button>

      <nav className="mt-6 grid gap-3">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `
              flex items-center gap-3 rounded-2xl px-4 py-4 font-semibold transition

              ${
                isActive
                  ? "bg-white/20 text-white"
                  : "text-white/80 hover:bg-white/10"
              }

              ${collapsed ? "lg:justify-center lg:px-2" : ""}
              `
            }
          >
            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-white/10 text-sm font-bold">
              {item.badge}
            </span>

            <span
              className={
                collapsed
                  ? "hidden lg:hidden"
                  : "block truncate"
              }
            >
              {item.label}
            </span>
          </NavLink>
        ))}
      </nav>
    </aside>

    {/* CONTENT */}
    <div className="flex-1 p-4 md:p-6 lg:ml-0">
      <main className="grid gap-5">
        {children}
      </main>
    </div>
  </div>
</>
  );
}
