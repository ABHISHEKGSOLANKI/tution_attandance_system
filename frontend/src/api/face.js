import axios from "axios";

const faceClient = axios.create({
  baseURL: import.meta.env.VITE_FACE_API_BASE_URL || "http://localhost:8095"
});

export async function registerFaceSamples(payload) {
  const response = await faceClient.post("/register/browser", payload);
  return response.data;
}

export async function captureAttendanceFrame(payload) {
  const response = await faceClient.post("/capture/browser", payload);
  return response.data;
}
