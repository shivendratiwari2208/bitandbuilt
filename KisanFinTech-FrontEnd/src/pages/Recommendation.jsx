import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import FarmerForm from "../components/FarmerForm";
import LoadingState from "../components/LoadingState";
import FarmOverview from "../components/FarmOverview";
import WeatherCard from "../components/WeatherCard";
import RecommendationSection from "../components/RecommendationSection";
import PriceChart from "../components/PriceChart";
import FertilizerSection from "../components/FertilizerSection";
import { getRecommendation } from "../services/recommendationApi";
import { useLanguage } from "../i18n";

function getErrorMessage(error, t) {
  const backendMessage =
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.response?.data?.details;

  if (backendMessage) return backendMessage;

  if (error?.response?.status === 400) return t("invalidInput");
  if (error?.response?.status === 404) return t("locationNotFound");
  if (error?.response?.status >= 500) return t("serverError");
  return t("backendError");
}

function Recommendation() {
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const handleSubmit = async (formData) => {
    setLoading(true);
    setError("");
    setResult(null);

    try {
      const response = await getRecommendation(formData);
      setResult(response);
    } catch (err) {
      console.error("Recommendation request failed:", err);
      setError(getErrorMessage(err, t));
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingState />;

  if (result) {
    return (
      <main className="dashboard-page">
        <div className="dashboard-header">
          <div>
            <div className="section-label">{t("farmAnalysis")}</div>
            <h1>{t("dashboardTitle")}</h1>
            <p>{t("dashboardLead")}</p>
          </div>

          <button
            className="secondary-button"
            onClick={() => {
              setResult(null);
              setError("");
            }}
          >
            {t("newAnalysis")}
          </button>
        </div>

        <FarmOverview data={result} />
        <WeatherCard data={result} />
        <RecommendationSection data={result} />
     <PriceChart
  recommendations={result.recommendations}
/>
        <FertilizerSection data={result} />

        <div className="flow-links">
          <Link to="/crop-planner">{t("planNext")}</Link>
        </div>
      </main>
    );
  }

  return (
    <main className="recommendation-page">
      <div className="form-header">
        <button className="back-button" onClick={() => navigate("/")}>
          {t("back")}
        </button>
        <div className="section-label">{t("farmAnalysis")}</div>
        <h1>{t("tellFarm")}</h1>
        <p>{t("tellFarmLead")}</p>
      </div>

      {error && (
        <div className="error-box">
          <span className="error-icon">!</span>
          <div>
            <strong>{t("unable")}</strong>
            <p>{error}</p>
          </div>
        </div>
      )}

      <FarmerForm onSubmit={handleSubmit} />
    </main>
  );
}

export default Recommendation;
