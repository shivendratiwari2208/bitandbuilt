function LoadingState() {
  return (
    <main className="loading-page">
      <div className="loading-card">
        <div className="loading-logo">🌾</div>
        <h1>Analyzing your farm...</h1>
        <p>We are combining location, live conditions, soil information and the crop model.</p>
        <div className="loading-steps">
          <div className="loading-step completed"><span>✓</span><span>Resolving farm location</span></div>
          <div className="loading-step completed"><span>✓</span><span>Fetching weather conditions</span></div>
          <div className="loading-step active"><span className="spinner"></span><span>Processing soil information</span></div>
          <div className="loading-step"><span>○</span><span>Running crop prediction</span></div>
          <div className="loading-step"><span>○</span><span>Preparing market insights</span></div>
        </div>
      </div>
    </main>
  );
}
export default LoadingState;
