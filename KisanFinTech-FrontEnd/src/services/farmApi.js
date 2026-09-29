import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  "http://localhost:8080";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 60000,
});

export async function getCropPlan(form) {
  const payload = {
    location: String(form.location || "").trim(),
    pincode: String(form.pincode || "").trim(),
    farmAreaAcres: Number(form.area),
    sowingDate: form.sowingDate,
    harvestDate: form.harvestingDate,
  };

  const response =
    await api.post(
      "/api/sowing-planner/plan",
      payload
    );

  return response.data;
}

export async function detectDisease() {
  throw new Error(
    "Disease detection is currently disabled."
  );
}