import { useState } from "react";
import { NavLink } from "react-router-dom";
import { clearStoredAuth, getStoredAuth } from "../api/session";
import { frontendPath } from "../api/client";

export default function Layout({ title, subtitle, children }) {
  const auth = getStoredAuth();
  const [collapsed, setCollapsed] = useState(false);

  function handleLogout() {
    clearStoredAuth();
    window.location.href = frontendPath + "login";
  }

  return (
    <div className="app-shell">
      <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>

        <nav className="sidebar-nav">
          <NavLink to="/dashboard" title="Dashboard" aria-label="Dashboard">
            <span className="nav-badge">D</span>
            <span className="nav-label">Dashboard</span>
          </NavLink>
          <NavLink to="/registration" title="Registration" aria-label="Registration">
            <span className="nav-badge">R</span>
            <span className="nav-label">Registration</span>
          </NavLink>
          <NavLink to="/attendance" title="Attendance" aria-label="Attendance">
            <span className="nav-badge">A</span>
            <span className="nav-label">Attendance</span>
          </NavLink>
          <NavLink to="/reports" title="Reports" aria-label="Reports">
            <span className="nav-badge">P</span>
            <span className="nav-label">Reports</span>
          </NavLink>
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
