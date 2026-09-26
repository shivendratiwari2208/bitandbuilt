function WeatherCard({ weather }) {
  const cards = [
    { icon: "🌡️", label: "Temperature", value: weather?.temperature != null ? `${weather.temperature} °C` : "—" },
    { icon: "💧", label: "Humidity", value: weather?.humidity != null ? `${weather.humidity} %` : "—" },
    { icon: "🌧️", label: "Rainfall", value: weather?.rainfall != null ? `${weather.rainfall} mm` : "—" },
    { icon: "🌊", label: "Soil Moisture", value: weather?.soilMoisture != null ? `${weather.soilMoisture} %` : "—" },
    { icon: "🌱", label: "Soil Type", value: weather?.soilType || "Not available" }
  ];

  return (
    <section className="dashboard-section">
      <div className="section-heading">
        <div>
          <span className="section-icon">🌦️</span>
          <div><h2>Weather & Soil Conditions</h2><p>Current environmental information for your farm</p></div>
        </div>
      </div>
      <div className="weather-grid weather-grid-five">
        {cards.map((card) => (
          <div className="weather-card" key={card.label}>
            <div className="weather-icon">{card.icon}</div>
            <div><span>{card.label}</span><strong>{card.value}</strong></div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default WeatherCard;
