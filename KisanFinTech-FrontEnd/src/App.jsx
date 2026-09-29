import { Routes, Route, Navigate } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Recommendation from "./pages/Recommendation";
import CropPlanner from "./pages/CropPlanner";
import PestAdvisor from "./pages/PestAdvisor";

function App() {
  return (
    <div className="app">
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/recommendation" element={<Recommendation />} />
        <Route path="/crop-planner" element={<CropPlanner />} />
        <Route path="/pest-advisor" element={<PestAdvisor />} />
        <Route path="/disease-detector" element={<Navigate to="/" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}

export default App;
