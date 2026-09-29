import { useState } from "react";
import {
  CROP_OPTIONS,
  getPestAdvice,
} from "../services/pestAdvisorApi";
import { useLanguage } from "../i18n";


const CROP_HI = {
  Barley: "जौ",
  Cotton: "कपास",
  "Ground Nuts": "मूंगफली",
  Maize: "मक्का",
  Millets: "बाजरा",
  "Oil seeds": "तिलहन",
  Paddy: "धान",
  Pulses: "दलहन",
  Sugarcane: "गन्ना",
  Tobacco: "तंबाकू",
  Wheat: "गेहूँ",
};

const PEST_HI = {
  Aphids: "एफिड्स",
  "American bollworm": "अमेरिकन बॉलवर्म",
  "Groundnut leaf miner": "मूंगफली लीफ माइनर",
  "Fall armyworm": "फॉल आर्मीवर्म",
  "Shoot fly": "शूट फ्लाई",
  "Brown planthopper": "ब्राउन प्लांट हॉपर",
  "Gram pod borer": "चना फली छेदक",
  "Early shoot borer": "अर्ली शूट बोरर",
  "Tobacco caterpillar": "तंबाकू कैटरपिलर",
  Termite: "दीमक",
};


function LeafBugIcon() {
  return (
    <svg
      viewBox="0 0 48 48"
      className="pest-title-icon"
      aria-hidden="true"
    >
      <path
        d="M29.5 7C19 8.2 11.8 14.1 12 23.1c.1 6.8 5 11.2 10.8 10.1 8.1-1.5 10.6-11.2 6.7-26.2Z"
        fill="none"
        stroke="currentColor"
        strokeWidth="2.4"
        strokeLinecap="round"
      />

      <path
        d="M13.5 36c4.2-8.4 9.5-13.6 17.7-19.2"
        fill="none"
        stroke="currentColor"
        strokeWidth="2.4"
        strokeLinecap="round"
      />

      <path
        d="M30 28c-3.5 0-6.3 2.8-6.3 6.3v2.2c0 3.5 2.8 6.3 6.3 6.3s6.3-2.8 6.3-6.3v-2.2c0-3.5-2.8-6.3-6.3-6.3Z"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      />

      <path
        d="M30 24.5v4M23.7 31.5l-4-2.4M36.3 31.5l4-2.4M23.7 37l-4 2.2M36.3 37l4 2.2"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  );
}


function LocationIcon() {
  return (
    <span
      className="pest-field-icon"
      aria-hidden="true"
    >
      ⌖
    </span>
  );
}


/* ============================================================
   RISK GAUGE
   Backend returns percentage: 0 - 100
   ============================================================ */

function RiskGauge({ value }) {
  const max = 100;

  const numericValue = Number(value);

  const safeValue = Number.isFinite(numericValue)
    ? Math.max(0, Math.min(max, numericValue))
    : 0;

  const angle =
    -90 + (safeValue / max) * 180;

  return (
    <div
      className="risk-gauge-wrap"
      aria-label={`Pest risk ${safeValue}%`}
    >
      <svg
        className="risk-gauge"
        viewBox="0 0 300 210"
        role="img"
      >
        <defs>

          <linearGradient
            id="riskArc"
            x1="0%"
            y1="100%"
            x2="100%"
            y2="0%"
          >
            <stop
              offset="0%"
              stopColor="#31e7a1"
            />

            <stop
              offset="45%"
              stopColor="#8cf56d"
            />

            <stop
              offset="68%"
              stopColor="#ffd34e"
            />

            <stop
              offset="100%"
              stopColor="#ff4f5e"
            />
          </linearGradient>

          <filter
            id="gaugeGlow"
            x="-40%"
            y="-40%"
            width="180%"
            height="180%"
          >
            <feGaussianBlur
              stdDeviation="5"
              result="blur"
            />

            <feMerge>
              <feMergeNode in="blur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>

        </defs>


        {/* Background arc */}

        <path
          d="M48 158 A102 102 0 0 1 252 158"
          fill="none"
          stroke="#17383b"
          strokeWidth="22"
          strokeLinecap="round"
        />


        {/* Colored arc */}

        <path
          d="M48 158 A102 102 0 0 1 252 158"
          fill="none"
          stroke="url(#riskArc)"
          strokeWidth="13"
          strokeLinecap="round"
          filter="url(#gaugeGlow)"
        />


        {/* Tick labels */}

        <g className="gauge-ticks">
          <text x="42" y="163">
            0
          </text>

          <text x="76" y="92">
            25
          </text>

          <text x="150" y="63">
            50
          </text>

          <text x="224" y="92">
            75
          </text>

          <text x="258" y="163">
            100
          </text>
        </g>


        {/* Needle */}

        <g
          transform={`rotate(${angle} 150 158)`}
          className="gauge-needle"
        >
          <line
            x1="150"
            y1="158"
            x2="150"
            y2="77"
          />

          <circle
            cx="150"
            cy="158"
            r="13"
          />

          <circle
            cx="150"
            cy="158"
            r="5"
          />
        </g>


        {/* Score */}

        <text
          x="150"
          y="202"
          textAnchor="middle"
          className="gauge-score"
        >
          {safeValue.toFixed(1)}%
        </text>

      </svg>
    </div>
  );
}


/* ============================================================
   PEST ADVISOR PAGE
   ============================================================ */

function PestAdvisor() {

  const {
    language,
    t,
  } = useLanguage();


  const [form, setForm] = useState({
    crop: "",
    location: "",
    pincode: "",
  });


  const [result, setResult] = useState(null);

  const [loading, setLoading] = useState(false);

  const [error, setError] = useState("");


  const update = (
    key,
    value
  ) => {

    setForm((current) => ({
      ...current,
      [key]: value,
    }));

  };


  /* ==========================================================
     SUBMIT
     ========================================================== */

  const handleSubmit = async (
    event
  ) => {

    event.preventDefault();

    setError("");
    setResult(null);


    if (!form.crop) {

      setError(
        t("pestRequiredCropLocation")
      );

      return;
    }


    if (!form.location.trim()) {

      setError(
        t("pestRequiredCropLocation")
      );

      return;
    }


    if (
      !/^\d{6}$/.test(
        form.pincode.trim()
      )
    ) {

      setError(
        t("validPincode")
      );

      return;
    }


    setLoading(true);


    try {

      const response =
        await getPestAdvice(form);

      setResult(response);

    } catch (err) {

      console.error(
        "Pesticide recommendation failed:",
        err
      );


      const backendMessage =
        err?.response?.data?.message ||
        err?.response?.data?.error;


      if (backendMessage) {

        setError(
          backendMessage
        );

      } else if (
        err?.response?.status === 400
      ) {

        setError(
          t("invalidInput")
        );

      } else if (
        err?.response?.status >= 500
      ) {

        setError(
          t("serverError")
        );

      } else {

        setError(
          err?.message ||
          t("backendError")
        );

      }

    } finally {

      setLoading(false);

    }
  };


  const cropLabel = (
    crop
  ) => {

    return language === "hi"
      ? CROP_HI[crop] || crop
      : crop;

  };


  const pestLabel = (
    pest
  ) => {

    return language === "hi"
      ? PEST_HI[pest] || pest
      : pest;

  };


  const riskLevelLabel = (
    level
  ) => {

    if (language !== "hi") {
      return level;
    }

    const translations = {
      Low: "कम",
      Medium: "मध्यम",
      High: "उच्च",
    };

    return (
      translations[level] ||
      level
    );

  };


  /* ==========================================================
     ENVIRONMENT
     ========================================================== */

  const environment =
    result?.environment || {};


  const temperature =
    environment.temperature_C;


  const humidity =
    environment.humidity_pct;


  const rainfall =
    environment.rainfall_mm;


  const soilMoisture =
    environment.soil_moisture_pct;


  const pestRisk =
    Number(
      result?.pestRiskPct ?? 0
    );


  const riskLevel =
    result?.riskLevel || "Low";


  const likelyPest =
    result?.likelyPest ||
    "Not available";


  const bestPesticide =
    result?.bestPesticide ||
    "Not available";


  /* ==========================================================
     UI
     ========================================================== */

  return (

    <main className="pest-advisor-page">

      {/* ======================================================
          HERO
          ====================================================== */}

      <section className="pest-hero">

        <div className="pest-hero-copy">

          <div className="pest-kicker">
            {t("pestKicker")}
          </div>

          <h1>
            {t("pestTitle")}{" "}
            <span>
              {t("advisorTitle")}
            </span>
          </h1>

          <p>
            {t("pestHeroText")}
          </p>

        </div>


        <div
          className="pest-hero-art"
          aria-hidden="true"
        />

      </section>


      {/* ======================================================
          WORKSPACE
          ====================================================== */}

      <section className="pest-workspace">


        {/* ====================================================
            INPUT CARD
            ==================================================== */}

        <div className="pest-input-card">

          <div className="pest-card-heading">

            <div className="pest-icon-box">
              <LeafBugIcon />
            </div>

            <div>

              <h2>
                {t("pestGetAdvice")}
              </h2>

              <p>
                {t("pestEnterDetails")}
              </p>

            </div>

          </div>


          <form
            onSubmit={handleSubmit}
            className="pest-advisor-form"
          >


            {/* CROP */}

            <div className="pest-form-field">

              <label htmlFor="pest-crop">
                {t("cropType")}
              </label>

              <div className="pest-input-wrap">

                <span>
                  🌱
                </span>

                <select
                  id="pest-crop"
                  value={form.crop}
                  onChange={(event) =>
                    update(
                      "crop",
                      event.target.value
                    )
                  }
                >

                  <option value="">
                    {t("selectCrop")}
                  </option>

                  {CROP_OPTIONS.map(
                    (crop) => (

                      <option
                        key={crop}
                        value={crop}
                      >
                        {cropLabel(crop)}
                      </option>

                    )
                  )}

                </select>

                <b>
                  ⌄
                </b>

              </div>

            </div>


            {/* LOCATION */}

            <div className="pest-form-field">

              <label htmlFor="pest-location">
                {t("location")}
              </label>

              <div className="pest-input-wrap">

                <LocationIcon />

                <input
                  id="pest-location"
                  value={form.location}
                  onChange={(event) =>
                    update(
                      "location",
                      event.target.value
                    )
                  }
                  placeholder={
                    t("locationPlaceholder")
                  }
                />

              </div>

            </div>


            {/* PINCODE */}

            <div className="pest-form-field">

              <label htmlFor="pest-pincode">
                {t("pincode")}
              </label>

              <div className="pest-input-wrap">

                <span>
                  ✉
                </span>

                <input
                  id="pest-pincode"
                  inputMode="numeric"
                  maxLength="6"
                  value={form.pincode}
                  onChange={(event) =>
                    update(
                      "pincode",
                      event.target.value
                        .replace(/\D/g, "")
                        .slice(0, 6)
                    )
                  }
                  placeholder={
                    t("pincodePlaceholder")
                  }
                />

              </div>

              <small>
                {t("pestPincodeHint")}
              </small>

            </div>


            {/* ERROR */}

            {error && (

              <div className="pest-error">
                {error}
              </div>

            )}


            {/* BUTTON */}

            <button
              className="pest-advice-button"
              type="submit"
              disabled={loading}
            >

              <span>
                {loading ? "…" : "⌕"}
              </span>

              {loading
                ? "Getting prediction..."
                : t("getAdvice")}

            </button>

          </form>

        </div>


        {/* ====================================================
            OUTPUT CARD
            ==================================================== */}

        <div className="pest-output-card">


          {/* EMPTY */}

          {!result && !loading && (

            <div className="pest-empty-state">

              <div className="pest-empty-glyph">
                🐛
              </div>

              <h2>
                {t("pestRecommendation")}
              </h2>

              <p>
                {t("pestEmptyText")}
              </p>

            </div>

          )}


          {/* LOADING */}

          {loading && (

            <div className="pest-empty-state">

              <div className="pest-empty-glyph">
                ⏳
              </div>

              <h2>
                Analyzing farm conditions...
              </h2>

              <p>
                Fetching weather, soil moisture
                and ML-based pest prediction.
              </p>

            </div>

          )}


          {/* RESULT */}

          {result && !loading && (

            <>

              {/* RESULT TITLE */}

              <div className="pest-output-title">

                <LeafBugIcon />

                <div>

                  <h2>
                    {t("pestRecommendation")}
                  </h2>

                  <p>
                    {cropLabel(result.crop)}
                    {" · "}
                    {result.location}
                    {" · "}
                    {result.pincode}
                  </p>

                </div>

              </div>


              {/* SUMMARY */}

              <div className="pest-summary-strip">

                <span>
                  🌱
                  <b>
                    {t("cropType")}
                  </b>
                  {" "}
                  {cropLabel(result.crop)}
                </span>


                <span>
                  ⌖
                  <b>
                    {t("location")}
                  </b>
                  {" "}
                  {result.location}
                </span>


                <span>
                  ✉
                  <b>
                    {t("pincode")}
                  </b>
                  {" "}
                  {result.pincode}
                </span>

              </div>


              {/* ENVIRONMENT */}

              <div className="pest-summary-strip">

                <span>
                  🌡️
                  <b>
                    Temperature
                  </b>
                  {" "}
                  {temperature ?? "—"}°C
                </span>


                <span>
                  💧
                  <b>
                    Humidity
                  </b>
                  {" "}
                  {humidity ?? "—"}%
                </span>


                <span>
                  🌧️
                  <b>
                    Rainfall
                  </b>
                  {" "}
                  {rainfall ?? "—"} mm
                </span>


                <span>
                  🌱
                  <b>
                    Soil Moisture
                  </b>
                  {" "}
                  {soilMoisture ?? "—"}%
                </span>

              </div>


              {/* OUTPUT */}

              <div className="pest-output-grid">


                {/* LEFT */}

                <div className="pest-detail-column">


                  {/* LIKELY PEST */}

                  <article className="pest-detail-block pest-danger">

                    <div className="detail-symbol">
                      🐞
                    </div>

                    <div>

                      <span className="detail-label">
                        Likely Pest
                      </span>

                      <h3>
                        {pestLabel(
                          likelyPest
                        )}
                      </h3>

                    </div>

                  </article>


                  {/* PESTICIDE */}

                  <article className="pest-detail-block pest-safe">

                    <div className="detail-symbol">
                      🧴
                    </div>

                    <div>

                      <span className="detail-label">
                        Best Pesticide
                      </span>

                      <h3>
                        {bestPesticide}
                      </h3>

                      <p>
                        Follow the registered
                        product label and local
                        agricultural guidance
                        before application.
                      </p>

                    </div>

                  </article>


                  {/* RESOLVED LOCATION */}

                  {result.resolvedLocation && (

                    <article className="pest-detail-block">

                      <div className="detail-symbol">
                        📍
                      </div>

                      <div>

                        <span className="detail-label">
                          Resolved Location
                        </span>

                        <h3>
                          {result.resolvedLocation}
                        </h3>

                      </div>

                    </article>

                  )}

                </div>


                {/* RIGHT — GAUGE */}

                <div>

                  <RiskGauge
                    value={pestRisk}
                  />

                  <div
                    className={`risk-level risk-${riskLevel.toLowerCase()}`}
                  >

                    <span>
                      {t("riskLevel")}
                    </span>

                    <strong>
                      {riskLevelLabel(
                        riskLevel
                      )}
                    </strong>

                  </div>

                </div>

              </div>

            </>

          )}

        </div>

      </section>

    </main>

  );
}


export default PestAdvisor;