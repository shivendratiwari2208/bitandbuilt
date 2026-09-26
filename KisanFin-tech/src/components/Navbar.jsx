import { Link } from "react-router-dom";

function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar-container">

        <Link to="/" className="logo">
          <span className="logo-icon">🌾</span>

          <div>
            <div className="logo-name">KisanFinTech</div>
            <div className="logo-subtitle">
              Smart Farming Decisions
            </div>
          </div>
        </Link>

        <div className="nav-links">
          <Link to="/">Home</Link>
          <Link to="/recommendation">Recommendation</Link>
        </div>

      </div>
    </nav>
  );
}

export default Navbar;