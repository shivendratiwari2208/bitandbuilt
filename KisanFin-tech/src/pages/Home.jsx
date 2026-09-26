import { useNavigate } from "react-router-dom";

function Home() {
  const navigate = useNavigate();

  return (
    <main className="home-page">

      <section className="hero">

        <div className="hero-content">

          <div className="hero-badge">
            🌱 Smart Agriculture Platform
          </div>

          <h1>
            Make Smarter
            <span> Farming Decisions</span>
          </h1>

          <p>
            Enter your farm location, pincode and sowing date to get
            data-driven crop recommendations and market insights.
          </p>

          <button
            className="primary-button hero-button"
            onClick={() => navigate("/recommendation")}
          >
            Get Crop Recommendation
            <span>→</span>
          </button>

        </div>

        <div className="hero-visual">

          <div className="farm-circle">

            <div className="sun">☀️</div>

            <div className="farm-field">
              <span>🌾</span>
              <span>🌱</span>
              <span>🌾</span>
              <span>🌱</span>
              <span>🌾</span>
            </div>

          </div>

        </div>

      </section>

      <section className="features">

        <div className="feature-card">
          <div className="feature-icon">📍</div>
          <h3>Location Based</h3>
          <p>
            Use your farm location and pincode to analyze
            local conditions.
          </p>
        </div>

        <div className="feature-card">
          <div className="feature-icon">🌦️</div>
          <h3>Weather Insights</h3>
          <p>
            Weather information is processed by the backend
            for your farm.
          </p>
        </div>

        <div className="feature-card">
          <div className="feature-icon">📊</div>
          <h3>Data Driven</h3>
          <p>
            Combines live environmental data, soil classification, ML
            prediction and historical crop prices.
          </p>
        </div>

      </section>

    </main>
  );
}

export default Home;