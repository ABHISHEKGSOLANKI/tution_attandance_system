import axios from "axios";
import { clearStoredAuth, getStoredAuth } from "./session";

export const appContext = import.meta.env.VITE_CONTEXT_PATH?.trim().replace(/\/+$/, "") || "";
export const backendBase =  import.meta.env.VITE_BACKEND_API_BASE_URL?.trim().replace(/\/+$/, "") || "";
export const backendUrl = `${backendBase}${appContext}`;
export const frontendPath = import.meta.env.VITE_FRONTEND_PATH;
export const loginPath = `${frontendPath}/login`;


const client = axios.create({
  baseURL: backendUrl,
});

client.interceptors.request.use((config) => {
  const auth = getStoredAuth();
  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const requestUrl = error.config?.url || "";

    // Ignore login API failures
    if (
      error.response?.status === 401 &&
      !requestUrl.includes("/auth/login")
    ) {
      clearStoredAuth();
      window.location.assign(frontendPath + "login");
    }

    return Promise.reject(error);
  }
);

export default client;
