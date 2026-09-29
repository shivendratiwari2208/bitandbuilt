import { useId, useState } from "react";
import { useLanguage } from "../i18n";
import {
  localizeCrop,
  localizeMonth,
  localizeMoney,
} from "../utils/localizeData";

const dimensions = {
  width: 980,
  height: 390,
  left: 82,
  right: 28,
  top: 30,
  bottom: 62,
};

const palette = ["#2563eb", "#16a34a", "#f59e0b"];

const formatAxisValue = (value) => {
  const n = Number(value);

  if (!Number.isFinite(n)) return "—";

  if (Math.abs(n) >= 100000) {
    return `₹${(n / 100000).toFixed(1)}L`;
  }

  if (Math.abs(n) >= 1000) {
    return `₹${Math.round(n / 1000)}k`;
  }

  return `₹${Math.round(n)}`;
};

function PriceChart({ data, recommendations }) {
  const { t, language } = useLanguage();
  const clipId = useId().replace(/:/g, "");
  const [activePoint, setActivePoint] = useState(null);
  const [hiddenCrops, setHiddenCrops] = useState(() => new Set());

  /*
   * Backend structure:
   *
   * recommendations: [
   *   {
   *     crop: "Oil seeds",
   *     market: {
   *       monthlyPrices: [
   *         { month: "March", price: 29354.55 }
   *       ]
   *     }
   *   }
   * ]
   */

  const cropRecommendations = Array.isArray(recommendations)
    ? recommendations.slice(0, 3).filter(Boolean)
    : [];

  /*
   * Convert backend nested structure into chart structure:
   *
   * [
   *   {
   *     month: "January",
   *     "Oil seeds": 25671,
   *     "Barley": 1240,
   *     "Ground Nuts": 5457
   *   }
   * ]
   */

  const monthMap = new Map();

  cropRecommendations.forEach((recommendation) => {
    const crop = recommendation?.crop;
    const monthlyPrices = recommendation?.market?.monthlyPrices;

    if (!crop || !Array.isArray(monthlyPrices)) {
      return;
    }

    monthlyPrices.forEach((item) => {
      if (!item?.month) return;

      const month = item.month;
      const price = Number(item.price);

      if (!Number.isFinite(price)) return;

      if (!monthMap.has(month)) {
        monthMap.set(month, {
          month,
        });
      }

      monthMap.get(month)[crop] = price;
    });
  });

  const rawData = Array.from(monthMap.values());

  const cropNames = cropRecommendations
    .map((item) => item?.crop)
    .filter(Boolean);

  if (!rawData.length || !cropNames.length) {
    return (
      <section className="price-chart-section">
        <div className="price-chart-header">
          <div>
            <div className="section-label">MARKET ANALYSIS</div>

            <h2>{t("marketTrend")}</h2>

            <p>{t("marketTrendLead")}</p>
          </div>
        </div>

        <div className="price-chart-card chart-empty">
          <span>📈</span>

          <strong>{t("marketTrendUnavailable")}</strong>

          <p>{t("marketTrendUnavailableLead")}</p>
        </div>
      </section>
    );
  }

  const lines = cropNames.map((crop, index) => ({
    key: crop,
    crop,
    label: localizeCrop(crop, language),
    color: palette[index % palette.length],
  }));

  const toggleCrop = (crop) => {
    setHiddenCrops((current) => {
      const next = new Set(current);
      if (next.has(crop)) next.delete(crop);
      else if (next.size < lines.length - 1) next.add(crop);
      return next;
    });
  };

  const chartData = rawData.map((row, index) => ({
    ...row,
    month: localizeMonth(
      row.month || `Month ${index + 1}`,
      language
    ),
  }));

  const allValues = chartData
    .flatMap((row) =>
      lines.map((line) => Number(row[line.key]))
    )
    .filter(Number.isFinite);

  if (!allValues.length) {
    return (
      <section className="price-chart-section">
        <div className="price-chart-header">
          <div>
            <div className="section-label">MARKET ANALYSIS</div>

            <h2>{t("marketTrend")}</h2>

            <p>{t("marketTrendLead")}</p>
          </div>
        </div>

        <div className="price-chart-card chart-empty">
          <span>📈</span>

          <strong>{t("marketTrendUnavailable")}</strong>

          <p>{t("marketTrendUnavailableLead")}</p>
        </div>
      </section>
    );
  }

  const minimum = Math.min(...allValues);
  const maximum = Math.max(...allValues);

  const spread =
    maximum - minimum ||
    Math.max(maximum * 0.1, 100);

  const padding = Math.max(spread * 0.14, 100);

  const yMin = Math.max(0, minimum - padding);
  const yMax = maximum + padding;

  const plotWidth =
    dimensions.width -
    dimensions.left -
    dimensions.right;

  const plotHeight =
    dimensions.height -
    dimensions.top -
    dimensions.bottom;

  const xAt = (index) =>
    dimensions.left +
    (chartData.length > 1
      ? index / (chartData.length - 1)
      : 0.5) *
      plotWidth;

  const yAt = (value) =>
    dimensions.top +
    (1 -
      (value - yMin) /
        (yMax - yMin || 1)) *
      plotHeight;

  const yTicks = Array.from(
    { length: 5 },
    (_, index) =>
      yMin +
      ((yMax - yMin) * index) / 4
  );

  return (
    <section
      className="price-chart-section"
      aria-labelledby="market-trend-title"
    >
      <div className="price-chart-header">
        <div>
          <div className="section-label">
            MARKET ANALYSIS
          </div>

          <h2 id="market-trend-title">
            {t("marketTrend")}
          </h2>

          <p>{t("marketTrendLead")}</p>
        </div>
      </div>

      <div className="price-chart-card">
        <div
          className="chart-legend"
          aria-label="Market price legend"
        >
          {lines.map((line) => {
            const hidden = hiddenCrops.has(line.key);
            return (
              <button
                key={line.key}
                type="button"
                className={`chart-legend-toggle ${hidden ? "is-hidden" : ""}`}
                onClick={() => toggleCrop(line.key)}
                aria-pressed={!hidden}
              >
                <i style={{ backgroundColor: line.color }} />
                {line.label}
              </button>
            );
          })}
        </div>

        {activePoint && (
          <div className="chart-hover-card" role="status">
            <strong>{activePoint.label}</strong>
            <span>{activePoint.month}</span>
            <b>{localizeMoney(activePoint.value, language)}</b>
          </div>
        )}

        <svg
          className="profit-chart"
          viewBox={`0 0 ${dimensions.width} ${dimensions.height}`}
          role="img"
          aria-label={t("marketTrend")}
          preserveAspectRatio="none"
          onMouseLeave={() => setActivePoint(null)}
        >
          <title>{t("marketTrend")}</title>

          <defs>
            <clipPath id={clipId}>
              <rect
                x={dimensions.left}
                y={dimensions.top}
                width={plotWidth}
                height={plotHeight}
              />
            </clipPath>
          </defs>

          {yTicks.map((value, index) => {
            const y = yAt(value);

            return (
              <g key={index}>
                <line
                  className="chart-grid-line"
                  x1={dimensions.left}
                  x2={
                    dimensions.width -
                    dimensions.right
                  }
                  y1={y}
                  y2={y}
                />

                <text
                  className="chart-y-label"
                  x={dimensions.left - 12}
                  y={y + 4}
                  textAnchor="end"
                >
                  {formatAxisValue(value)}
                </text>
              </g>
            );
          })}

          <line
            className="chart-axis"
            x1={dimensions.left}
            x2={dimensions.left}
            y1={dimensions.top}
            y2={
              dimensions.height -
              dimensions.bottom
            }
          />

          <line
            className="chart-axis"
            x1={dimensions.left}
            x2={
              dimensions.width -
              dimensions.right
            }
            y1={
              dimensions.height -
              dimensions.bottom
            }
            y2={
              dimensions.height -
              dimensions.bottom
            }
          />

          <text
            className="chart-axis-title"
            transform={`translate(22 ${
              dimensions.top +
              plotHeight / 2
            }) rotate(-90)`}
            textAnchor="middle"
          >
            {t("marketPriceAxis")}
          </text>

          {chartData.map((row, index) => {
            const x = xAt(index);

            return (
              <g key={`${row.month}-${index}`}>
                <line
                  className="chart-x-tick"
                  x1={x}
                  x2={x}
                  y1={
                    dimensions.height -
                    dimensions.bottom
                  }
                  y2={
                    dimensions.height -
                    dimensions.bottom +
                    5
                  }
                />

                <text
                  className="chart-x-label"
                  x={x}
                  y={
                    dimensions.height -
                    dimensions.bottom +
                    22
                  }
                  textAnchor="middle"
                >
                  {row.month}
                </text>
              </g>
            );
          })}

          <g clipPath={`url(#${clipId})`}>
            {lines.filter((line) => !hiddenCrops.has(line.key)).map((line) => {
              const points = chartData
                .map((row, index) => {
                  const value = Number(
                    row[line.key]
                  );

                  return Number.isFinite(value)
                    ? `${xAt(index)},${yAt(value)}`
                    : null;
                })
                .filter(Boolean)
                .join(" ");

              return (
                <g key={line.key}>
                  <polyline
                    className="chart-line"
                    points={points}
                    stroke={line.color}
                  />

                  {chartData.map(
                    (row, index) => {
                      const value = Number(
                        row[line.key]
                      );

                      if (
                        !Number.isFinite(value)
                      ) {
                        return null;
                      }

                      return (
                        <circle
                          key={`${line.key}-${index}`}
                          className="chart-point"
                          cx={xAt(index)}
                          cy={yAt(value)}
                          r={activePoint?.lineKey === line.key && activePoint?.index === index ? 7 : 4}
                          fill={line.color}
                          onMouseEnter={() => setActivePoint({ lineKey: line.key, index, month: row.month, value, label: line.label, color: line.color })}
                          onMouseLeave={() => setActivePoint(null)}
                          onFocus={() => setActivePoint({ lineKey: line.key, index, month: row.month, value, label: line.label, color: line.color })}
                          tabIndex={0}
                        >
                          <title>
                            {`${line.label}: ${localizeMoney(
                              value,
                              language
                            )}`}
                          </title>
                        </circle>
                      );
                    }
                  )}
                </g>
              );
            })}
          </g>
        </svg>
      </div>
    </section>
  );
}

export default PriceChart;