function formatPrice(value) {
  if (value === undefined || value === null || value === "") return "—";
  return `₹${Number(value).toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;
}

function RecommendationSection({ data }) {
  const recommendations = Array.isArray(data?.recommendations) ? data.recommendations.slice(0, 3) : [];

  return (
    <section className="dashboard-section">
      <div className="section-heading">
        <div>
          <span className="section-icon">🌱</span>
          <div><h2>Crop Recommendations</h2><p>Top crops selected from the ML prediction and market dataset</p></div>
        </div>
      </div>

      {recommendations.length === 0 ? (
        <div className="empty-recommendation"><div className="empty-icon">🌾</div><h3>No recommendation data returned</h3><p>The backend did not return any crop predictions for this analysis.</p></div>
      ) : (
        <div className="recommendation-grid recommendation-grid-three">
          {recommendations.map((item, index) => (
            <div className={`recommendation-card ${index === 0 ? "top-recommendation" : ""}`} key={item.crop || index}>
              <div className="recommendation-card-header">
                <div className="crop-icon">🌱</div>
                <div><span>{index === 0 ? "Top Recommended Crop" : "Recommended Crop"}</span><h3>{item.crop}</h3></div>
              </div>
              <div className="recommendation-details">
                <div><span>Expected Price</span><strong>{formatPrice(item.expectedPrice)}</strong></div>
                <div><span>Model Confidence</span><strong>{item.confidence != null ? `${item.confidence}%` : "—"}</strong></div>
                <div><span>Average Minimum</span><strong>{formatPrice(item.minimumPrice)}</strong></div>
                <div><span>Average Maximum</span><strong>{formatPrice(item.maximumPrice)}</strong></div>
                <div><span>Avg Price Change</span><strong>{item.averageChange != null ? `₹${Number(item.averageChange).toLocaleString("en-IN", { maximumFractionDigits: 0 })}` : "—"}</strong></div>
                <div><span>Price Risk</span><strong>{item.risk || "—"}</strong></div>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

export default RecommendationSection;
