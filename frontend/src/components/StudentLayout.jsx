import { clearStoredAuth, getStoredAuth } from "../api/session";
import { frontendPath } from "../api/client";

export default function Layout({ title, subtitle, children }) {
  const auth = getStoredAuth();

  function handleLogout() {
    clearStoredAuth();
    window.location.href = frontendPath + "login";
  }

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,rgba(255,204,153,0.45),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(116,180,200,0.25),transparent_28%),linear-gradient(135deg,#f5efe2_0%,#edf4fb_48%,#fdf7f2_100%)] p-4 md:p-6">
      <div className="mx-auto max-w-7xl">
        <header className="mb-5 flex flex-col gap-4 rounded-[26px] border border-[rgba(201,214,225,0.8)] bg-[rgba(255,255,255,0.74)] p-5 backdrop-blur-[14px] lg:flex-row lg:items-center lg:justify-between">
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
