import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from "recharts";

function PriceChart({ data, recommendations }) {
  const chartData = Array.isArray(data) ? data : [];
  const chartLines = Array.isArray(recommendations) ? recommendations.slice(0, 3).map((item) => item.crop).filter(Boolean) : [];

  if (!chartData.length || !chartLines.length) return null;

  const formatter = (value) => `₹${Number(value).toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;

  return (
    <section className="price-chart-section">
      <div className="price-chart-header">
        <div>
          <div className="section-label">MARKET ANALYSIS</div>
          <h2>Crop Price Comparison</h2>
          <p>Monthly average modal prices from the supplied crop-price dataset.</p>
        </div>
      </div>
      <div className="price-chart-card">
        <ResponsiveContainer width="100%" height={420}>
          <LineChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="month" />
            <YAxis label={{ value: "Price (₹/quintal)", angle: -90, position: "insideLeft" }} />
            <Tooltip formatter={(value, name) => [formatter(value), name]} />
            <Legend />
            {chartLines.map((crop, index) => (
              <Line key={crop} type="monotone" dataKey={crop} name={crop} stroke={index === 0 ? "#2563eb" : index === 1 ? "#16a34a" : "#f59e0b"} strokeWidth={3} dot={{ r: 3 }} activeDot={{ r: 5 }} />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </section>
  );
}

export default PriceChart;
