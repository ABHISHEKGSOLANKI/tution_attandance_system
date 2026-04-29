import client from "./client";
import { getStoredAuth, setStoredAuth } from "./session";

export async function login(data) {
  const response = await client.post("/auth/login", data);
  setStoredAuth(response.data);
  return response.data;
}

export async function register(data) {
  const formData = new FormData();
  Object.entries(data).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      formData.append(key, value);
    }
  });

  const response = await client.post("/auth/register", formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
  return response.data;
}

export async function changePassword(data) {
  const response = await client.post("/auth/change-password", data);
  const auth = getStoredAuth();
  const authResponse = { token: auth.token, user: response.data };
  setStoredAuth(authResponse);
  return response.data;
}
