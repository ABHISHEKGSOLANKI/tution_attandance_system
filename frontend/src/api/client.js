import axios from "axios";
import { clearStoredAuth, getStoredAuth } from "./session";

export const appContext = import.meta.env.VITE_CONTEXT_PATH?.trim().replace(/\/+$/, "") || "";
export const backendBase =  import.meta.env.VITE_BACKEND_API_BASE_URL?.trim().replace(/\/+$/, "") || "";
export const backendUrl = `${backendBase}${appContext}`;
export const loginPath = `${backendUrl}/login`;

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
    if (error.response?.status === 401) {
      clearStoredAuth();
      window.location.assign(loginPath);
    }
    return Promise.reject(error);
  }
);

export default client;
