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
    <div className="flex min-h-screen bg-[radial-gradient(circle_at_top_left,rgba(255,204,153,0.45),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(116,180,200,0.25),transparent_28%),linear-gradient(135deg,#f5efe2_0%,#edf4fb_48%,#fdf7f2_100%)]">
      <aside
        className={`overflow-hidden bg-gradient-to-b from-[#163f69] to-[#0f2742] text-white transition-[width,padding] duration-200 ${
          collapsed ? "w-[88px] px-3.5 py-5" : "w-[250px] p-6"
        }`}
      >
        <nav className="grid gap-3">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              title={item.label}
              aria-label={item.label}
              className={({ isActive }) =>
                `flex items-center gap-3 whitespace-nowrap rounded-2xl px-4 py-4 font-semibold text-[rgba(255,255,255,0.86)] transition ${
                  collapsed ? "justify-center px-2" : ""
                } ${isActive ? "bg-[rgba(255,255,255,0.16)] text-white" : "hover:bg-[rgba(255,255,255,0.08)]"}`
              }
            >
              <span className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[rgba(255,255,255,0.12)] text-[0.82rem] font-bold text-white">
                {item.badge}
              </span>
              <span className={collapsed ? "w-0 overflow-hidden opacity-0" : "truncate"}>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex-1 p-4 md:p-6">
        <header className="mb-5 flex flex-col gap-4 rounded-[26px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.74)] p-5 backdrop-blur-[14px] lg:flex-row lg:items-center lg:justify-between">
          <button
            className="rounded-xl border border-[rgba(22,63,105,0.18)] bg-transparent px-4 py-3 text-sm font-medium text-[#163f69] transition hover:-translate-y-px"
            type="button"
            onClick={() => setCollapsed(!collapsed)}
          >
            Menu
          </button>

          <div className="min-w-0">
            <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
            <p className="mt-1 text-sm text-slate-600">{subtitle}</p>
          </div>

          {auth?.user && (
            <div className="flex flex-col gap-1 lg:items-end">
              <strong className="text-sm font-semibold text-slate-900">{auth.user.name}</strong>
              <span className="text-sm text-slate-500">{auth.user.email}</span>
              <button
                type="button"
                className="mt-1 rounded-xl border border-[rgba(22,63,105,0.18)] bg-transparent px-4 py-2 text-sm font-medium text-[#163f69] transition hover:-translate-y-px"
                onClick={handleLogout}
              >
                Logout
              </button>
            </div>
          )}
        </header>

        <main className="grid gap-5">{children}</main>
      </div>
    </div>
  );
}
