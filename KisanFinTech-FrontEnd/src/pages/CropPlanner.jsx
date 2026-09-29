import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { getCropPlan } from "../services/farmApi";
import { useLanguage } from "../i18n";
import {
  localizeCrop,
  localizeMoney,
  localizeNumber,
  localizeText,
  localizeDate,
} from "../utils/localizeData";

const initial = {
  location: "",
  pincode: "",
  area: "",
  sowingDate: "",
  harvestingDate: "",
};

function safeNumber(value) {
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function displayNumber(value, language) {
  const number = safeNumber(value);
  return number === null ? "—" : localizeNumber(number, language);
}

function displayMoney(value, language) {
  const number = safeNumber(value);
  return number === null ? "—" : localizeMoney(number, language);
}

/*
 * Backend statuses:
 *
 * RECOMMENDED
 * DATE_MISMATCH
 * OUTSIDE_SOWING_WINDOW
 *
 * The backend now uses the month/season as the primary planning signal.
 */
function statusLabel(crop, language) {
  const status = String(
    crop?.status || crop?.sowingStatus || ""
  ).toUpperCase();

  const days = safeNumber(crop?.daysUntilNextWindow);

  if (status === "RECOMMENDED") {
    return language === "hi"
      ? "अभी/इस अवधि के लिए उपयुक्त"
      : "Recommended for this period";
  }

  if (status === "DATE_MISMATCH") {
    return language === "hi"
      ? "कटाई अवधि जाँचें"
      : "Check harvest timing";
  }

  if (status === "OUTSIDE_SOWING_WINDOW") {
    if (days !== null && days >= 0) {
      return language === "hi"
        ? `${displayNumber(days, language)} दिन में अगली विंडो`
        : `Next window in ${displayNumber(days, language)} days`;
    }

    return language === "hi"
      ? "अगली बुवाई विंडो"
      : "Upcoming sowing window";
  }

  if (status === "COMPATIBLE") {
    return language === "hi"
      ? "मिट्टी अनुकूल"
      : "Soil compatible";
  }

  if (status === "UNKNOWN") {
    return language === "hi"
      ? "जाँच आवश्यक"
      : "Check locally";
  }

  if (status === "WINDOW_PASSED") {
    return language === "hi"
      ? "विंडो निकल गई"
      : "Window passed";
  }

  return language === "hi"
    ? "मौसमी"
    : "Seasonal";
}

function categoryLabel(crop, language) {
  const category =
    crop?.category ||
    crop?.marketGroup ||
    "Crop";

  return localizeText(category, language);
}

function CropCard({
  crop,
  language,
  t,
  farmAreaAcres,
}) {
  const economics = crop?.economics || {};

  const status = statusLabel(
    crop,
    language
  );

  const investmentPerHa =
    economics.averageInvestmentPerHectare;

  const revenuePerHa =
    economics.estimatedRevenueAveragePerHectare;

  const profitPerHa =
    economics.estimatedProfitAveragePerHectare;

  const pricePerQ =
    economics.averageMarketPricePerQuintal;

  const yieldPerHa =
    economics.averageYieldQuintalPerHectare;

  const farmArea =
    safeNumber(farmAreaAcres);

  const hectares =
    farmArea === null
      ? null
      : farmArea * 0.404686;

  const farmInvestment =
    hectares !== null &&
    safeNumber(investmentPerHa) !== null
      ? safeNumber(investmentPerHa) * hectares
      : null;

  const farmRevenue =
    hectares !== null &&
    safeNumber(revenuePerHa) !== null
      ? safeNumber(revenuePerHa) * hectares
      : null;

  const farmProfit =
    hectares !== null &&
    safeNumber(profitPerHa) !== null
      ? safeNumber(profitPerHa) * hectares
      : null;

  const soilCompatibility =
    crop?.soilCompatibility || "—";

  const weatherCompatibility =
    crop?.currentWeatherCompatibility ||
    "—";

  const nextSowingWindow =
    crop?.nextSowingWindow;

  const daysUntilNextWindow =
    safeNumber(crop?.daysUntilNextWindow);

  return (
    <article className="planner-crop">

      {/* HEADER */}
      <div className="crop-card-top">

        <span aria-hidden="true">
          🌱
        </span>

        <div className="planner-crop-title">

          <p>
            {t("suitableCrop")}
          </p>

          <h2>
            {localizeCrop(
              crop?.crop || "Crop",
              language
            )}
          </h2>

        </div>

        <b className="planner-status-pill">
          {status}
        </b>

      </div>

      {/* TAGS */}
      <div className="planner-tags">

        <span>
          {categoryLabel(
            crop,
            language
          )}
        </span>

        {crop?.season && (
          <span>
            {localizeText(
              crop.season,
              language
            )}
          </span>
        )}

        {crop?.statewideMajor && (
          <span>
            {language === "hi"
              ? "प्रमुख फसल"
              : "Major crop"}
          </span>
        )}

      </div>

      {/* REASON */}
      <p className="crop-reason">

        {localizeText(
          crop?.reason,
          language
        ) ||
          (language === "hi"
            ? "यह योजना फसल कैलेंडर, मिट्टी, मौसम और बाजार बेंचमार्क को ध्यान में रखकर तैयार की गई है।"
            : "This plan considers the crop calendar, soil, current weather and market benchmarks.")}

      </p>

      {/* MONTH / SOWING INFORMATION */}
      <div className="planner-extra">

        <span>
          <strong>
            {language === "hi"
              ? "बुवाई विंडो"
              : "Sowing window"}
          :
          </strong>{" "}
          {crop?.sowingWindow || "—"}
        </span>

        {nextSowingWindow && (
          <span>
            <strong>
              {language === "hi"
                ? "अगली विंडो"
                : "Next window"}
              :
            </strong>{" "}
            {nextSowingWindow}

            {daysUntilNextWindow !== null && (
              <>
                {" "}
                (
                {displayNumber(
                  daysUntilNextWindow,
                  language
                )}{" "}
                {language === "hi"
                  ? "दिन"
                  : "days"}
                )
              </>
            )}
          </span>
        )}

      </div>

      {/* ECONOMICS */}
      <div className="planner-metrics">

        <div>
          <span>
            {t("investment")}
          </span>

          <strong>
            {displayMoney(
              investmentPerHa,
              language
            )}
          </strong>

          <small>
            / ha
          </small>
        </div>

        <div>
          <span>
            {t("expectedRevenue")}
          </span>

          <strong>
            {displayMoney(
              revenuePerHa,
              language
            )}
          </strong>

          <small>
            / ha
          </small>
        </div>

        <div>
          <span>
            {t("expectedProfit")}
          </span>

          <strong className="profit-text">
            {displayMoney(
              profitPerHa,
              language
            )}
          </strong>

          <small>
            / ha
          </small>
        </div>

        <div>
          <span>
            {t("expectedPrice")}
          </span>

          <strong>
            {displayMoney(
              pricePerQ,
              language
            )}
          </strong>

          <small>
            / q
          </small>
        </div>

        <div>
          <span>
            {t("averageYield")}
          </span>

          <strong>
            {displayNumber(
              yieldPerHa,
              language
            )}
          </strong>

          <small>
            q / ha
          </small>
        </div>

        <div>
          <span>
            {t("soilType")}
          </span>

          <strong>
            {soilCompatibility}
          </strong>
        </div>

      </div>

      {/* FARM ECONOMICS */}
      {farmArea !== null && (
        <div className="planner-farm-economics">

          <div>
            <span>
              {language === "hi"
                ? "आपके खेत के लिए अनुमानित निवेश"
                : "Estimated investment for your farm"}
            </span>

            <strong>
              {displayMoney(
                farmInvestment,
                language
              )}
            </strong>
          </div>

          <div>
            <span>
              {language === "hi"
                ? "आपके खेत के लिए अनुमानित राजस्व"
                : "Estimated revenue for your farm"}
            </span>

            <strong>
              {displayMoney(
                farmRevenue,
                language
              )}
            </strong>
          </div>

          <div>
            <span>
              {language === "hi"
                ? "आपके खेत के लिए अनुमानित लाभ"
                : "Estimated profit for your farm"}
            </span>

            <strong>
              {displayMoney(
                farmProfit,
                language
              )}
            </strong>
          </div>

        </div>
      )}

      {/* SOIL / WEATHER */}
      <div className="planner-extra">

        <span>
          {t("soilCompatibility")}:
          {" "}
          {soilCompatibility}
        </span>

        <span>
          {language === "hi"
            ? "मौसम"
            : "Current weather"}:
          {" "}
          {weatherCompatibility}
        </span>

      </div>

      {/* HARVEST */}
      <div className="planner-extra">

        <span>
          {language === "hi"
            ? "अपेक्षित कटाई"
            : "Expected harvest"}:
          {" "}
          {crop?.expectedHarvestFrom ||
            "—"}
          {" → "}
          {crop?.expectedHarvestTo ||
            "—"}
        </span>

        <span>
          {language === "hi"
            ? "आपकी नियोजित कटाई"
            : "Your planned harvest"}:
          {" "}
          {crop?.plannedHarvestDate ||
            "—"}
        </span>

      </div>

    </article>
  );
}

function CropPlanner() {
  const { t, language } =
    useLanguage();

  const [form, setForm] =
    useState(initial);

  const [result, setResult] =
    useState(null);

  const [error, setError] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  const update = (event) => {

    const {
      name,
      value,
    } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const submit = async (event) => {

    event.preventDefault();

    setError("");

    if (!form.location.trim()) {
      return setError(
        t("requiredLocation")
      );
    }

    if (!/^\d{6}$/.test(form.pincode)) {
      return setError(
        t("validPincode")
      );
    }

    if (
      !form.area ||
      Number(form.area) <= 0
    ) {
      return setError(
        t("requiredArea")
      );
    }

    if (
      !form.sowingDate ||
      !form.harvestingDate
    ) {
      return setError(
        t("completeDetails")
      );
    }

    if (
      form.harvestingDate <=
      form.sowingDate
    ) {
      return setError(
        t("harvestAfterSowing")
      );
    }

    setLoading(true);

    try {

      const plan =
        await getCropPlan(form);

      setResult(plan);

    } catch (err) {

      console.error(
        "Crop planner request failed:",
        err
      );

      setError(
        err?.response?.data?.message ||
          err?.response?.data?.error ||
          err?.response?.data?.details ||
          t("backendError")
      );

    } finally {

      setLoading(false);

    }
  };

  /*
   * Recommended crops first.
   *
   * Then add the complete catalog.
   *
   * This is important because the backend may say:
   *
   * - Recommended
   * - DATE_MISMATCH
   * - OUTSIDE_SOWING_WINDOW
   *
   * We still want the farmer to see all considered crops.
   */
  const allCrops = useMemo(() => {

    if (!result) {
      return [];
    }

    const recommended =
      Array.isArray(
        result.recommendedCrops
      )
        ? result.recommendedCrops
        : [];

    const catalog =
      Array.isArray(
        result.allCrops
      )
        ? result.allCrops
        : [];

    const combined = [
      ...recommended,
      ...catalog,
    ];

    const seen =
      new Set();

    return combined.filter(
      (crop) => {

        const key =
          String(
            crop?.crop || ""
          )
            .trim()
            .toLowerCase();

        if (
          !key ||
          seen.has(key)
        ) {
          return false;
        }

        seen.add(key);

        return true;
      }
    );

  }, [result]);

  /* =====================================================
     LOADING
     ===================================================== */

  if (loading) {

    return (
      <main className="tool-page planner-loading-page">

        <div className="planner-loading-mark">
          🌱
        </div>

        <div className="section-label">
          CROP PLANNER
        </div>

        <h1>
          {language === "hi"
            ? "आपकी मौसमी योजना तैयार हो रही है"
            : "Building your seasonal plan"}
        </h1>

        <p>
          {language === "hi"
            ? "स्थान, मिट्टी, मौसम और फसल कैलेंडर का विश्लेषण किया जा रहा है।"
            : "Resolving your location and combining soil, weather, crop-calendar and market data."}
        </p>

      </main>
    );
  }

  /* =====================================================
     RESULT
     ===================================================== */

  if (result) {

    const summary =
      result.summary || {};

    const dates =
      result.planningDates || {};

    const recommended =
      Array.isArray(
        result.recommendedCrops
      )
        ? result.recommendedCrops
        : [];

    const recommendedCount =
      safeNumber(
        summary.recommendedCount
      ) ?? recommended.length;

    const temperature =
      summary.currentTemperatureC;

    const moisture =
      result.soil?.soilMoisturePercent ??
      summary.soilMoisturePercent;

    const soilType =
      result.soil?.soilType ||
      summary.detectedSoilType ||
      "Unknown";

    const plannedDays =
      dates.plannedDurationDays ??
      summary.plannedDurationDays;

    const farmArea =
      safeNumber(form.area);

    const planningPeriod =
      result.planningPeriod || {};

    return (
      <main className="tool-page planner-result-page">

        {/* HEADER */}
        <div className="dashboard-header planner-result-header">

          <div>

            <div className="section-label">
              CROP PLANNER
            </div>

            <h1>
              {language === "hi"
                ? "आपकी मौसमी फसल योजना"
                : "Your seasonal crop plan"}
            </h1>

            <p>

              {result.location ||
                form.location}

              {" · "}

              {localizeDate(
                dates.sowingDate,
                language
              )}

              {" → "}

              {localizeDate(
                dates.harvestDate,
                language
              )}

              {farmArea !== null
                ? ` · ${displayNumber(
                    farmArea,
                    language
                  )} ${
                    language === "hi"
                      ? "एकड़"
                      : "acres"
                  }`
                : ""}

            </p>

          </div>

          <button
            className="secondary-button"
            onClick={() =>
              setResult(null)
            }
          >
            ← {t("changeDates")}
          </button>

        </div>

        {/* PLANNING PERIOD */}
        <section className="planner-summary">

          <span aria-hidden="true">
            📅
          </span>

          <div>

            <strong>

              {planningPeriod.label ||
                (language === "hi"
                  ? "मौसमी योजना"
                  : "Seasonal planning")}

            </strong>

            <p>

              {language === "hi"
                ? `${recommendedCount} उपयुक्त विकल्प मिले।`
                : `${recommendedCount} suitable options returned for the selected planning period.`}

            </p>

          </div>

        </section>

        {/* SUMMARY */}
        <section
          className={`planner-summary ${
            recommendedCount > 0
              ? "has-recommendations"
              : "no-recommendations"
          }`}
        >

          <span aria-hidden="true">
            🌾
          </span>

          <div>

            <strong>

              {recommendedCount}

              {" "}

              {language === "hi"
                ? "अनुशंसित विकल्प"
                : "recommended options"}

            </strong>

            <p>

              {recommendedCount > 0

                ? language === "hi"
                  ? "चयनित बुवाई अवधि, मिट्टी और उपलब्ध मौसम डेटा के आधार पर उपयुक्त फसलें।"

                  : "Crops that fit the selected planning period and available agricultural data."

                : language === "hi"
                  ? "इन तिथियों के लिए कोई पूर्ण अनुशंसा नहीं मिली। नीचे पूरा कैटलॉग दिखाया गया है।"

                  : "No complete recommendation was returned for these dates. The complete crop catalog is shown below."}

            </p>

          </div>

        </section>

        {/* ENVIRONMENT */}
        <section className="planner-info-grid planner-info-grid-four">

          <div className="planner-info-card">

            <span>🌡️</span>

            <div>

              <small>
                {t("temperature")}
              </small>

              <strong>

                {displayNumber(
                  temperature,
                  language
                )}

                {safeNumber(
                  temperature
                ) !== null
                  ? " °C"
                  : ""}

              </strong>

            </div>

          </div>

          <div className="planner-info-card">

            <span>💧</span>

            <div>

              <small>

                {language === "hi"
                  ? "मिट्टी की नमी"
                  : "Soil moisture"}

              </small>

              <strong>

                {displayNumber(
                  moisture,
                  language
                )}

                {safeNumber(
                  moisture
                ) !== null
                  ? " %"
                  : ""}

              </strong>

            </div>

          </div>

          <div className="planner-info-card">

            <span>🌱</span>

            <div>

              <small>
                {t("soilType")}
              </small>

              <strong>
                {soilType}
              </strong>

            </div>

          </div>

          <div className="planner-info-card">

            <span>📊</span>

            <div>

              <small>

                {language === "hi"
                  ? "विचार की गई फसलें"
                  : "Crops considered"}

              </small>

              <strong>

                {displayNumber(
                  summary.totalCropsConsidered,
                  language
                )}

              </strong>

            </div>

          </div>

        </section>

        {/* CROP LIST */}
        <section className="planner-section-block">

          <div className="section-heading">

            <div>

              <span className="section-icon">
                🌱
              </span>

              <div>

                <h2>

                  {recommendedCount > 0

                    ? language === "hi"
                      ? "अनुशंसित फसलें"
                      : "Recommended crops"

                    : language === "hi"
                      ? "फसल कैटलॉग"
                      : "Crop catalog considered"}

                </h2>

                <p>

                  {safeNumber(
                    plannedDays
                  ) !== null

                    ? `${displayNumber(
                        plannedDays,
                        language
                      )} ${t(
                        "daysAvailable"
                      )}`

                    : ""}

                </p>

              </div>

            </div>

          </div>

          <div className="planner-grid">

            {allCrops.length ? (

              allCrops.map(
                (crop) => (

                  <CropCard
                    key={`${crop?.crop}-${crop?.sowingWindow || crop?.nextSowingWindow || "crop"}`}
                    crop={crop}
                    language={language}
                    t={t}
                    farmAreaAcres={
                      farmArea
                    }
                  />

                )
              )

            ) : (

              <div className="empty-state">

                <strong>

                  {language === "hi"
                    ? "कोई फसल डेटा नहीं मिला"
                    : "No crop data returned"}

                </strong>

                <p>

                  {language === "hi"
                    ? "बैकएंड ने कोई crop catalog entry नहीं लौटाई।"
                    : "The planner backend did not return any crop catalog entries."}

                </p>

              </div>

            )}

          </div>

        </section>

        {/* METHODOLOGY */}
        <section className="planner-methodology">

          <strong>

            {language === "hi"
              ? "योजना पद्धति"
              : "Planning methodology"}

          </strong>

          <p>

            {language === "hi"
              ? "फसल की बुवाई अवधि को प्राथमिकता दी जाती है। इसके बाद मिट्टी, वर्तमान तापमान और बाजार/आर्थिक डेटा का उपयोग योजना को समझने के लिए किया जाता है।"
              : "The sowing period is the primary planning signal. Soil, current temperature and market/economic data are then used to provide practical planning information."}

          </p>

          {result.economicsMethod?.warning && (
            <p>
              {result.economicsMethod.warning}
            </p>
          )}

          {result.catalogCoverage?.note && (
            <p>
              {result.catalogCoverage.note}
            </p>
          )}

        </section>

        <div className="flow-links">

          <Link to="/recommendation">
            {t("detailedAdvice")}
          </Link>

        </div>

      </main>
    );
  }

  /* =====================================================
     FORM
     ===================================================== */

  return (
    <main className="tool-page">

      <Link
        className="back-button"
        to="/"
      >
        ← {t("back")}{" "}
        {t("home").toLowerCase()}
      </Link>

      <div className="page-kicker">
        {t("cropPlannerKicker")}
      </div>

      <h1>
        {t("growNext")}
      </h1>

      <p className="page-lead">
        {t("growLead")}
      </p>

      <form
        className="planner-form"
        onSubmit={submit}
      >

        <div className="form-grid">

          <label>

            {t("location")}

            <input
              name="location"
              value={form.location}
              onChange={update}
              placeholder={
                t("villagePlaceholder")
              }
              autoComplete="address-level2"
            />

          </label>

          <label>

            {t("pincode")}

            <input
              name="pincode"
              maxLength="6"
              inputMode="numeric"
              value={form.pincode}
              onChange={(event) =>
                setForm(
                  (current) => ({
                    ...current,
                    pincode:
                      event.target.value
                        .replace(/\D/g, "")
                        .slice(0, 6),
                  })
                )
              }
              placeholder={
                t("pincodePlaceholder")
              }
              autoComplete="postal-code"
            />

          </label>

          <label>

            {t("area")}

            <input
              name="area"
              type="number"
              min="0.1"
              step="0.1"
              value={form.area}
              onChange={update}
              placeholder={
                t("areaPlaceholder")
              }
            />

          </label>

          <label>

            {t("sowingDate")}

            <input
              name="sowingDate"
              type="date"
              value={
                form.sowingDate
              }
              onChange={update}
            />

          </label>

          <label>

            {t("harvestingDate")}

            <input
              name="harvestingDate"
              type="date"
              value={
                form.harvestingDate
              }
              onChange={update}
            />

          </label>

        </div>

        {error && (
          <p className="form-error">
            {error}
          </p>
        )}

        <button
          className="primary-button"
          type="submit"
          disabled={loading}
        >

          {loading
            ? t("buildingPlan")
            : t("findSuitable")}

          {" →"}

        </button>

      </form>

    </main>
  );
}

export default CropPlanner;