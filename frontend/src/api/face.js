import axios from "axios";
import { backendUrl } from "./client";

const faceClient = axios.create({
  baseURL: import.meta.env.VITE_FACE_API_BASE_URL?.trim() || `${backendUrl}/faceapi`
});

export async function registerFaceSamples(payload) {
  const response = await faceClient.post("/register/browser", payload);
  return response.data;
}

export async function captureAttendanceFrame(payload) {
  const response = await faceClient.post("/capture/browser", payload);
  return response.data;
}
