import { Link } from "react-router-dom";
import { useLanguage } from "../i18n";

function GlobeIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="globe-icon">
      <circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" strokeWidth="1.8" />
      <path d="M3.8 12h16.4M12 3.5c2.3 2.4 3.3 5.1 3.3 8.5S14.3 18.1 12 20.5c-2.3-2.4-3.3-5.1-3.3-8.5S9.7 5.9 12 3.5Z" fill="none" stroke="currentColor" strokeWidth="1.4" />
    </svg>
  );
}

function Navbar() {
  const { language, setLanguage, t } = useLanguage();

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="logo" aria-label="KisanFinTech">
          <img className="logo-image" src="/assets/reference/logo.png" alt="KisanFinTech" />
        </Link>
        <div className="nav-actions">
          <Link to="/" className="home-nav-button">⌂ <span>{t("home")}</span></Link>
          <button
          className="language-button"
          onClick={() => setLanguage(language === "en" ? "hi" : "en")}
          aria-label={language === "en" ? t("languageHindi") : t("languageEnglish")}
        >
          <GlobeIcon />
          <span>{language === "en" ? "English" : "हिंदी"}</span>
          <span className="language-chevron">⌄</span>
          </button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
