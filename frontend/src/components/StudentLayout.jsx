import { useState } from "react";
import { NavLink } from "react-router-dom";
import { clearStoredAuth, getStoredAuth } from "../api/session";

export default function Layout({ title, subtitle, children }) {
  const auth = getStoredAuth();
  const [collapsed, setCollapsed] = useState(false);

  function handleLogout() {
    clearStoredAuth();
    window.location.href = "/login";
  }

  return (
    <div className="app-shell">
      <div className="main-area">
        <header className="topbar">
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
