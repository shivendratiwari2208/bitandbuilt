import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 60000,
});

// These are the ONLY crops supported by the backend ML model.
export const CROP_OPTIONS = [
  "Barley",
  "Cotton",
  "Ground Nuts",
  "Maize",
  "Millets",
  "Oil seeds",
  "Paddy",
  "Pulses",
  "Sugarcane",
  "Tobacco",
  "Wheat",
];

/**
 * Calls Spring Boot pesticide recommendation API.
 *
 * Backend endpoint:
 * POST /api/pesticide/recommend
 *
 * Request:
 * {
 *   crop,
 *   location,
 *   pincode
 * }
 */
export async function getPestAdvice({
  crop,
  location,
  pincode,
}) {
  const payload = {
    crop: String(crop || "").trim(),
    location: String(location || "").trim(),
    pincode: String(pincode || "").trim(),
  };

  if (!payload.crop) {
    throw new Error("Please select a crop.");
  }

  if (!payload.location) {
    throw new Error("Please enter a location.");
  }

  if (!/^\d{6}$/.test(payload.pincode)) {
    throw new Error("Please enter a valid 6-digit pincode.");
  }

  const response = await api.post(
    "/api/pesticide/recommend",
    payload
  );

  return response.data;
}