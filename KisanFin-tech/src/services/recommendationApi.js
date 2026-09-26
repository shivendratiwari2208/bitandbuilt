import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json"
  },
  timeout: 30000
});

export const getRecommendation = async ({ location, pincode, sowingDate }) => {
  const response = await api.post("/api/recommendations", {
    location: location.trim(),
    pincode: pincode.trim(),
    sowingDate
  });

  return response.data;
};
