import { useState } from "react";
import { useNavigate } from "react-router-dom";
import FarmerForm from "../components/FarmerForm";
import LoadingState from "../components/LoadingState";
import FarmOverview from "../components/FarmOverview";
import WeatherCard from "../components/WeatherCard";
import RecommendationSection from "../components/RecommendationSection";
import PriceChart from "../components/PriceChart";
import { getRecommendation } from "../services/recommendationApi";

function Recommendation() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const handleSubmit = async (formData) => {
    setLoading(true); setError(""); setResult(null);
    try {
      setResult(await getRecommendation(formData));
    } catch (err) {
      const status = err.response?.status;
      if (status === 400) setError("Invalid farmer input. Please check the location, pincode and sowing date.");
      else if (status === 404) setError("Location or soil information was not found for this pincode.");
      else if (status === 502) setError("An external data service could not be reached. Please try again.");
      else if (status === 500) setError("The server could not generate the recommendation. Please try again.");
      else setError("Unable to connect to the backend. Make sure Spring Boot and the Python ML API are running.");
    } finally { setLoading(false); }
  };

  if (loading) return <LoadingState />;

  if (result) {
    return (
      <main className="dashboard-page">
        <div className="dashboard-header">
          <div><div className="section-label">FARM ANALYSIS</div><h1>Your Farm Dashboard</h1><p>Processed insights from your submitted farm information.</p></div>
          <button className="secondary-button" onClick={() => { setResult(null); setError(""); }}>← New Analysis</button>
        </div>
        <FarmOverview data={result} />
        <WeatherCard weather={result.weather} />
        <RecommendationSection data={result} />
        <PriceChart data={result.marketPrices} recommendations={result.recommendations} />
      </main>
    );
  }

  return (
    <main className="recommendation-page">
      <div className="form-header">
        <button className="back-button" onClick={() => navigate("/")}>← Back</button>
        <div className="section-label">FARM ANALYSIS</div>
        <h1>Tell us about your farm</h1>
        <p>Enter only the information you know. KisanFinTech fetches and processes the environmental data automatically.</p>
      </div>
      {error && <div className="error-box"><span className="error-icon">!</span><div><strong>Unable to generate recommendation</strong><p>{error}</p></div></div>}
      <FarmerForm onSubmit={handleSubmit} />
    </main>
  );
}
export default Recommendation;
