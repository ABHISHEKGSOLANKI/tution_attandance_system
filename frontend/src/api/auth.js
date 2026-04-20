import client from "./client";
import { getStoredAuth, setStoredAuth } from "./session";

export async function login(data) {
  const response = await client.post("/auth/login", data);
  setStoredAuth(response.data);
  return response.data;
}

export async function register(data) {
  const response = await client.post("/auth/register", data);
  return response.data;
}

export async function changePassword(data) {
  const response = await client.post("/auth/change-password", data);
  const auth = getStoredAuth();
  const authResponse = { token: auth.token, user: response.data };
  setStoredAuth(authResponse);
  return response.data;
}
