function FarmOverview({ data }) {
  const formatDate = (dateString) => {
    if (!dateString) return "Not available";

    const date = new Date(`${dateString}T00:00:00`);

    if (Number.isNaN(date.getTime())) return dateString;

    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "long",
      year: "numeric"
    });
  };

  const latitude = data?.latitude;
  const longitude = data?.longitude;

  return (
    <section className="dashboard-section">
      <div className="section-heading">
        <div>
          <span className="section-icon">🌾</span>
          <div>
            <h2>Farm Overview</h2>
            <p>Your submitted farm information</p>
          </div>
        </div>
      </div>

      <div className="overview-grid overview-grid-three">
        <div className="overview-card location-card">
          <span className="overview-icon">📍</span>
          <div>
            <span className="overview-label">Location</span>
            <strong>{data?.location || "Not available"}<br /><small>{data?.pincode || ""}</small></strong>
          </div>
        </div>

        <div className="overview-card">
          <span className="overview-icon">📅</span>
          <div>
            <span className="overview-label">Sowing Date</span>
            <strong>{formatDate(data?.sowingDate)}</strong>
          </div>
        </div>

        <div className="overview-card">
          <span className="overview-icon">🌐</span>
          <div>
            <span className="overview-label">Coordinates</span>
            <strong>
              {typeof latitude === "number" ? latitude.toFixed(4) : "—"}
              {typeof longitude === "number" ? `, ${longitude.toFixed(4)}` : ""}
            </strong>
          </div>
        </div>
      </div>
    </section>
  );
}

export default FarmOverview;
