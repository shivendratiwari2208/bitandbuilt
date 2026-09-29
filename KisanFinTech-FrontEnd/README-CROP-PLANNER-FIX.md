# KisanFinTech frontend wiring — crop planner fix

## What was fixed

- Crop Planner sends location, pincode, farm area, sowing date and harvest date to `POST /api/sowing-planner/plan`.
- Planner result reads the actual backend fields: `summary`, `planningDates`, `soil`, `recommendedCrops`, `allCrops`, and `economics`.
- If `recommendedCrops` is empty, the UI explicitly shows the full returned crop catalog instead of pretending there are recommendations.
- The `NaN days` badge is removed. Missing/non-numeric day values now show `Seasonal`, `Check locally`, `Window passed`, or `—` as appropriate.
- Per-hectare economics are displayed with units.
- Farm-area estimates are calculated from the backend per-hectare benchmark values using 1 acre = 0.404686 hectare.
- Soil moisture, temperature, soil type and crop-count summary cards are wired to the backend response.
- `localizeNumber` now safely handles non-numeric values instead of rendering `NaN`.

## Backend requirement

The backend Feature 4 DTO must accept `pincode` and `farmAreaAcres` if you want those fields to be validated and used by the planner. Ready-to-copy backend files are in `../backend-patch/` in the delivery package.

## Run

```powershell
npm install
npm run dev
```

Use:

```text
VITE_API_BASE_URL=http://localhost:8080
```
