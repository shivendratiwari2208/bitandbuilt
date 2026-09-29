import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 60000,
});

export async function getRecommendation(formData) {
  const payload = {
    location: String(formData.location || "").trim(),
    pincode: String(formData.pincode || "").trim(),
    area: Number(formData.area),
    sowingDate: formData.sowingDate,
  };

  const response = await api.post("/api/recommendations", payload);
  return response.data;
}
