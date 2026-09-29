import { useLanguage } from "../i18n";
import {
  localizeDate,
  localizeNumber,
} from "../utils/localizeData";

function FarmOverview({ data }) {
  const { t, language } = useLanguage();

  const formatDate = (value) =>
    value
      ? localizeDate(value, language)
      : t("notAvailable");


  /*
   * Support both possible response structures:
   *
   * 1. {
   *      coordinates: {
   *        latitude: ...,
   *        longitude: ...
   *      }
   *    }
   *
   * 2. {
   *      latitude: ...,
   *      longitude: ...
   *    }
   *
   * This makes the frontend safe while we verify
   * the exact Spring Boot response.
   */

  const latitude =
    data?.coordinates?.latitude ??
    data?.latitude;

  const longitude =
    data?.coordinates?.longitude ??
    data?.longitude;


  const hasCoordinates =
    typeof latitude === "number" &&
    typeof longitude === "number";


  const cards = [
    {
      icon: "📍",
      label: t("location"),
      value:
        data?.displayName ||
        data?.location ||
        t("notAvailable"),
    },

    {
      icon: "🔢",
      label: t("pincode"),
      value:
        data?.pincode ||
        t("notAvailable"),
    },

    {
      icon: "📅",
      label: t("sowingDate"),
      value:
        formatDate(data?.sowingDate),
    },

    {
      icon: "🌐",
      label: t("coordinates"),
      value: hasCoordinates
        ? `${localizeNumber(
            latitude,
            language
          )}, ${localizeNumber(
            longitude,
            language
          )}`
        : t("notAvailable"),
    },
  ];


  return (
    <section className="dashboard-section">

      <div className="section-heading">

        <div>

          <span className="section-icon">
            🌾
          </span>

          <div>

            <h2>
              {t("farmOverview")}
            </h2>

            <p>
              {t("submittedFarm")}
            </p>

          </div>

        </div>

      </div>


      <div className="overview-grid overview-grid-three">

        {cards.map((card) => (

          <div
            className="overview-card"
            key={card.label}
          >

            <span className="overview-icon">
              {card.icon}
            </span>

            <div>

              <span className="overview-label">
                {card.label}
              </span>

              <strong>
                {card.value}
              </strong>

            </div>

          </div>

        ))}

      </div>

    </section>
  );
}

export default FarmOverview;