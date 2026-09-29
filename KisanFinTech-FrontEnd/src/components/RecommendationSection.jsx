import { useLanguage } from "../i18n";
import {
  localizeCrop,
  localizeRisk,
  localizeMoney,
} from "../utils/localizeData";

function RecommendationSection({ data }) {
  const { t, language } = useLanguage();

  const recommendations = Array.isArray(data?.recommendations)
    ? data.recommendations.slice(0, 3)
    : [];

  const money = (value) => {
    if (
      value === null ||
      value === undefined ||
      value === "" ||
      Number.isNaN(Number(value))
    ) {
      return "—";
    }

    return localizeMoney(Number(value), language);
  };

  const numberValue = (value) => {
    if (
      value === null ||
      value === undefined ||
      value === ""
    ) {
      return "—";
    }

    return value;
  };

  return (
    <section className="dashboard-section">
      {/* =====================================================
          SECTION HEADER
      ===================================================== */}

      <div className="section-heading">
        <div>
          <span className="section-icon">🌱</span>

          <div>
            <h2>{t("cropRecommendations")}</h2>

            <p>{t("threeSuggested")}</p>
          </div>
        </div>
      </div>

      {/* =====================================================
          EMPTY STATE
      ===================================================== */}

      {!recommendations.length ? (
        <div className="empty-recommendation">
          <div className="empty-icon">🌾</div>

          <h3>{t("noRecommendation")}</h3>

          <p>{t("noRecommendationText")}</p>
        </div>
      ) : (
        <div className="recommendation-grid recommendation-grid-three">
          {recommendations.map((recommendation, index) => {
            /*
             * =================================================
             * BACKEND MARKET DATA
             *
             * The backend returns market information like:
             *
             * recommendation: {
             *   crop: "Oil seeds",
             *   probability: 0.40,
             *   confidence: 40,
             *
             *   market: {
             *     expectedPrice: ...,
             *     minimumPrice: ...,
             *     maximumPrice: ...,
             *     averageChange: ...,
             *     confidence: ...,
             *     risk: "Low",
             *     observations: ...
             *   }
             * }
             *
             * =================================================
             */

            const market = recommendation?.market || {};

            /*
             * Support both structures:
             *
             * 1. recommendation.market.expectedPrice
             * 2. recommendation.expectedPrice
             *
             * This keeps the frontend compatible with the
             * existing backend and older responses.
             */

            const expectedPrice =
              market.expectedPrice ??
              market.averageMarketPrice ??
              recommendation.expectedPrice ??
              recommendation.averageMarketPrice;

            const minimumPrice =
              market.minimumPrice ??
              market.minimumMarketPrice ??
              recommendation.minimumPrice ??
              recommendation.minimumMarketPrice;

            const maximumPrice =
              market.maximumPrice ??
              market.maximumMarketPrice ??
              recommendation.maximumPrice ??
              recommendation.maximumMarketPrice;

            const risk =
              market.risk ??
              recommendation.risk;

            const observations =
              market.observations ??
              market.priceObservations ??
              recommendation.priceObservations ??
              recommendation.observations;

            /*
             * Confidence should come from the ML prediction,
             * not the market dataset.
             */

            let confidence =
              recommendation.confidence;

            if (
              confidence === null ||
              confidence === undefined
            ) {
              if (
                recommendation.probabilityPercent !==
                undefined
              ) {
                confidence =
                  recommendation.probabilityPercent;
              } else if (
                recommendation.probability !==
                undefined
              ) {
                confidence =
                  Number(
                    recommendation.probability
                  ) * 100;
              }
            }

            /*
             * Avoid values such as:
             *
             * 40.0000001%
             *
             * in the UI.
             */

            if (
              confidence !== null &&
              confidence !== undefined &&
              Number.isFinite(Number(confidence))
            ) {
              confidence =
                Number(confidence).toFixed(0);
            }

            return (
              <article
                className="recommendation-card"
                key={
                  recommendation.crop ||
                  `crop-${index}`
                }
              >
                {/* ===========================================
                    CROP HEADER
                =========================================== */}

                <div className="recommendation-card-header">
                  <div className="crop-icon">
                    🌱
                  </div>

                  <div>
                    <span>
                      {index === 0
                        ? t("topRecommendation")
                        : t("alternativeCrop")}
                    </span>

                    <h3>
                      {localizeCrop(
                        recommendation.crop ||
                          "Crop",
                        language
                      )}
                    </h3>
                  </div>
                </div>

                {/* ===========================================
                    DETAILS
                =========================================== */}

                <div className="recommendation-details">

                  {/* EXPECTED PRICE */}

                  <div>
                    <span>
                      {t("expectedPrice")}
                    </span>

                    <strong>
                      {money(expectedPrice)}
                    </strong>
                  </div>

                  {/* CONFIDENCE */}

                  <div>
                    <span>
                      {t("confidence")}
                    </span>

                    <strong>
                      {confidence !== null &&
                      confidence !== undefined
                        ? `${confidence}%`
                        : "—"}
                    </strong>
                  </div>

                  {/* MINIMUM PRICE */}

                  <div>
                    <span>
                      {t("minimumPrice")}
                    </span>

                    <strong>
                      {money(minimumPrice)}
                    </strong>
                  </div>

                  {/* MAXIMUM PRICE */}

                  <div>
                    <span>
                      {t("maximumPrice")}
                    </span>

                    <strong>
                      {money(maximumPrice)}
                    </strong>
                  </div>

                  {/* RISK */}

                  <div>
                    <span>
                      {t("risk")}
                    </span>

                    <strong>
                      {risk
                        ? localizeRisk(
                            risk,
                            language
                          )
                        : "—"}
                    </strong>
                  </div>

                  {/* OBSERVATIONS */}

                  <div>
                    <span>
                      {t("observations")}
                    </span>

                    <strong>
                      {numberValue(observations)}
                    </strong>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}

export default RecommendationSection;