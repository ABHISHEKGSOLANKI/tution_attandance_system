const STORAGE_KEY = "attendance-auth";

export function getStoredAuth() {
  const raw = window.localStorage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function setStoredAuth(payload) {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
}

export function clearStoredAuth() {
  window.localStorage.removeItem(STORAGE_KEY);
}
