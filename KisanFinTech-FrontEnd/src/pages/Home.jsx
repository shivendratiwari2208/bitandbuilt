import { Link } from "react-router-dom";
import { useLanguage } from "../i18n";

function LeafIcon({ color = "#238447" }) {
  return (
    <svg viewBox="0 0 32 32" aria-hidden="true" className="icon-svg">
      <path d="M24.8 4.8C15.3 5.3 8.5 9.3 7.3 17.1c-.7 4.7 2.4 8.7 7.2 8.7 7.9 0 11.5-8.2 10.3-21Z" fill={color} />
      <path d="M8.4 26.2c3.4-7.2 7.3-11.2 13.8-14.9" fill="none" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

function BugIcon() {
  return (
    <svg viewBox="0 0 32 32" aria-hidden="true" className="icon-svg">
      <path d="M11 13.5h10v7a5 5 0 0 1-10 0v-7Z" fill="none" stroke="#a95c2c" strokeWidth="2" />
      <path d="M16 8v5M9 16H5.5M26.5 16H23M10 11 7.5 8.5M22 11l2.5-2.5M12 24l-2 3M20 24l2 3" fill="none" stroke="#a95c2c" strokeWidth="2" strokeLinecap="round" />
      <circle cx="13.5" cy="17" r="1" fill="#a95c2c" /><circle cx="18.5" cy="17" r="1" fill="#a95c2c" />
    </svg>
  );
}

function CalendarIcon() {
  return (
    <svg viewBox="0 0 32 32" aria-hidden="true" className="icon-svg">
      <rect x="5" y="7" width="22" height="20" rx="3" fill="none" stroke="#5b4ab4" strokeWidth="2.2" />
      <path d="M10 4.5v6M22 4.5v6M5 13h22" stroke="#5b4ab4" strokeWidth="2.2" strokeLinecap="round" />
      <path d="M10 18h3M16 18h3M10 22h3M16 22h3" stroke="#5b4ab4" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

const tools = [
  {
    to: "/recommendation",
    key: "cropRecommendation",
    textKey: "cropText",
    image: "crop",
    icon: LeafIcon,
  },
  {
    to: "/crop-planner",
    key: "cropPlanner",
    textKey: "plannerText",
    image: "planner",
    icon: CalendarIcon,
  },
  {
    to: "/pest-advisor",
    key: "pestAdvisor",
    textKey: "pestAdvisorText",
    image: "pest",
    icon: BugIcon,
  },
];

function Home() {
  const { t } = useLanguage();

  return (
    <main className="reference-home">
      <section className="reference-hero">
        <div className="reference-hero-image" aria-hidden="true" />
        <div className="reference-hero-overlay" aria-hidden="true" />
        <div className="reference-hero-copy">
          <div className="section-label">SMARTER FARMING</div>
          <h1>Better Decisions<br />for a Prosperous Harvest</h1>
          <p>AI-powered insights, real-time data and personalized guidance for every farmer.</p>
          <div className="hero-tool-pills">
            {tools.map((tool) => {
              const Icon = tool.icon;
              return (
                <Link to={tool.to} className="hero-tool-pill" key={tool.key}>
                  <span className={`hero-pill-icon hero-pill-${tool.image}`}><Icon /></span>
                  <strong>{t(tool.key)}</strong>
                </Link>
              );
            })}
          </div>
        </div>
      </section>

      <section className="reference-feature-grid" aria-label="Farm tools">
        {tools.map((tool) => {
          const Icon = tool.icon;
          return (
            <Link to={tool.to} className={`reference-feature-card reference-${tool.image}`} key={tool.key}>
              <div className="reference-feature-copy">
                <div className={`reference-feature-icon feature-icon-${tool.image}`}><Icon /></div>
                <div className="reference-feature-text">
                  <h2>{t(tool.key)}</h2>
                  <p>{t(tool.textKey)}</p>
                  <span className="reference-arrow" aria-hidden="true">→</span>
                </div>
              </div>
              <div className="reference-feature-art" aria-hidden="true">
                <img
                  src={tool.image === "pest" ? "/pest-advisor-banner.png" : `/assets/reference/${tool.image === "crop" ? "crop-card" : "planner-card"}.png`}
                  alt=""
                />
              </div>
            </Link>
          );
        })}
      </section>

      <section className="reference-footer-banner">
        <div className="reference-footer-image" aria-hidden="true" />
        <div className="reference-footer-overlay" aria-hidden="true" />
        <div className="reference-footer-content">
          <div className="reference-footer-mark"><LeafIcon /></div>
          <div>
            <div className="reference-footer-kicker">
              {t("healthySoil")} <span>•</span> {t("healthyCrops")} <span>•</span> {t("sustainableFuture")}
            </div>
            <h2>{t("greenerTomorrow")}</h2>
          </div>
        </div>
      </section>
    </main>
  );
}

export default Home;
