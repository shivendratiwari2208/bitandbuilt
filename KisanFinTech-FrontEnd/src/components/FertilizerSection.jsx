import { useLanguage } from "../i18n";

function FertilizerSection({ data }) {
  const { t } = useLanguage();
  const recommended = data?.recommendedFertilizer;
  const predictions = Array.isArray(data?.fertilizerPredictions)
    ? data.fertilizerPredictions.slice(0, 4)
    : [];

  if (!recommended && !predictions.length) {
    return (
      <section className="fertilizer-section fertilizer-unavailable">
        <div className="section-heading">
          <div>
            <span className="section-icon">🌿</span>
            <div>
              <h2>{t("recommendedFertilizers")}</h2>
              <p>{t("fertilizerUnavailable")}</p>
            </div>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="fertilizer-section">
      <div className="section-heading">
        <div>
          <span className="section-icon">🌿</span>
          <div>
            <h2>{t("recommendedFertilizers")}</h2>
            <p>{t("fertilizerBasedOnModel")}</p>
          </div>
        </div>
      </div>

      {recommended && (
        <div className="fertilizer-featured">
          <div>
            <span>{t("recommendedFertilizer")}</span>
            <strong>{recommended}</strong>
          </div>
        </div>
      )}

      {predictions.length > 0 && (
        <div className="fertilizer-list">
          {predictions.map((item) => (
            <div className="fertilizer-item" key={item.fertilizer}>
              <span>🌱</span>
              <div>
                <strong>{item.fertilizer}</strong>
                <small>
                  {item.probabilityPercent !== undefined
                    ? `${item.probabilityPercent}%`
                    : "—"}
                </small>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

export default FertilizerSection;
