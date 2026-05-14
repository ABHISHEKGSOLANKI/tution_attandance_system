import { useState } from "react";
import { NavLink } from "react-router-dom";
import { clearStoredAuth, getStoredAuth } from "../api/session";
import { appContext } from "../api/client";

export default function Layout({ title, subtitle, children }) {
  const auth = getStoredAuth();
  const [collapsed, setCollapsed] = useState(false);

  function handleLogout() {
    clearStoredAuth();
    window.location.href = appContext + "/login";
  }

  return (
    <div className="app-shell">
      <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
        <div className="brand-block">
          <p className="brand-kicker">Tuition Center</p>
          <h2 className="logo">{collapsed ? "GT" : "Gangadhar Tutions"}</h2>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/registration">Registration</NavLink>
          <NavLink to="/attendance">Attendance</NavLink>
          <NavLink to="/reports">Reports</NavLink>
        </nav>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <button className="menu-btn" type="button" onClick={() => setCollapsed(!collapsed)}>
            Menu
          </button>

          <div className="topbar-copy">
            <h1>{title}</h1>
            <p>{subtitle}</p>
          </div>

          {auth?.user && (
            <div className="user-box">
              <strong>{auth.user.name}</strong>
              <span>{auth.user.email}</span>
              <button type="button" onClick={handleLogout}>
                Logout
              </button>
            </div>
          )}
        </header>

        <main className="content">{children}</main>
      </div>
    </div>
  );
}
