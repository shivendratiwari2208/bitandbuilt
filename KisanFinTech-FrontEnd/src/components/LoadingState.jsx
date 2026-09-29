import { useLanguage } from "../i18n";
function LoadingState(){const {t}=useLanguage();return <main className="loading-page"><div className="loading-card"><div className="loading-logo">🌾</div><h1>{t("loadingTitle")}</h1><p>{t("loadingLead")}</p><div className="loading-steps"><div className="loading-step completed">✓ {t("loadingFarm")}</div><div className="loading-step active"><span className="spinner"/> {t("loadingWeather")}</div><div className="loading-step"><span>○</span> {t("loadingCrops")}</div></div></div></main>}
export default LoadingState;
