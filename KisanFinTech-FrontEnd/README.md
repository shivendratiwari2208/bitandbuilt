# KisanFinTech Frontend - Backend Wired Final

## Run

Create `.env`:

VITE_API_BASE_URL=http://localhost:8080

Then:

npm install
npm run dev

## Important

This version uses the real Spring Boot APIs. It does not use mock recommendation or crop-planner data.

Recommendation:
POST /api/recommendations

Seasonal planner:
POST /api/sowing-planner/plan

Disease is currently disabled from the UI.

The market chart uses the monthly `marketPrices` returned by Spring Boot. It does not invent fallback prices.

For actual fertilizer output, apply the included `backend-required-change/RecommendationService.java` because the current Spring Boot service otherwise drops the fertilizer fields returned by Flask.
