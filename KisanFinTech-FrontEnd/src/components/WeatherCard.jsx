import { useLanguage } from "../i18n";
import { localizeSoil, localizeNumber } from "../utils/localizeData";

function WeatherCard({ data }) {
  const { t, language } = useLanguage();

  // Backend currently returns these values at the top level.
  // Keep fallbacks for compatibility if the structure changes later.
  const temperature =
    data?.temperature ??
    data?.weather?.temperature ??
    data?.weather?.temperatureC;

  const humidity =
    data?.humidity ??
    data?.weather?.humidity ??
    data?.weather?.humidityPercent;

  const rainfall =
    data?.rainfall ??
    data?.weather?.rainfall ??
    data?.weather?.rainfallMm;

  const soilMoisture =
    data?.soilMoisture ??
    data?.weather?.soilMoisture ??
    data?.weather?.soilMoisturePercent;

  const soilType =
    data?.soil?.soilType ??
    data?.weather?.soilType;

  const formatValue = (value, unit = "") => {
    if (value === null || value === undefined || value === "") {
      return "—";
    }

    const number = Number(value);

    if (!Number.isFinite(number)) {
      return `${value}${unit}`;
    }

    return `${localizeNumber(number, language)}${unit}`;
  };

  const cards = [
    {
      icon: "🌡️",
      label: t("temperature"),
      value: formatValue(temperature, " °C"),
    },
    {
      icon: "💧",
      label: t("humidity"),
      value: formatValue(humidity, " %"),
    },
    {
      icon: "🌧️",
      label: t("rainfall"),
      value: formatValue(rainfall, " mm"),
    },
    {
      icon: "🌊",
      label: t("soilMoisture"),
      value: formatValue(soilMoisture, " %"),
    },
    {
      icon: "🌱",
      label: t("soilType"),
      value: soilType
        ? localizeSoil(soilType, language)
        : t("notAvailable"),
    },
  ];

  return (
    <section className="dashboard-section">
      <div className="section-heading">
        <div>
          <span className="section-icon">🌦️</span>

          <div>
            <h2>{t("weatherSoil")}</h2>
            <p>{t("currentEnvironment")}</p>
          </div>
        </div>
      </div>

      <div className="weather-grid weather-grid-five">
        {cards.map((card) => (
          <div className="weather-card" key={card.label}>
            <div className="weather-icon">{card.icon}</div>

            <div>
              <span>{card.label}</span>
              <strong>{card.value}</strong>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default WeatherCard;