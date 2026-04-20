import { clearStoredAuth, getStoredAuth } from "../api/session";

export default function Layout({ title, subtitle, children }) {
  const auth = getStoredAuth();

  function handleLogout() {
    clearStoredAuth();
    window.location.href = "/login";
  }

  return (
    <div className="page-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Tuition Attendance System</p>
          <h1>{title}</h1>
          <p className="subtitle">{subtitle}</p>
        </div>
        {auth?.user && (
          <div className="topbar-actions">
            <div className="user-chip">
              <strong>{auth.user.name}</strong>
              <span>{auth.user.role}</span>
            </div>
            <button className="ghost-button" onClick={handleLogout}>Logout</button>
          </div>
        )}
      </header>
      <main>{children}</main>
    </div>
  );
}
