# KisanFinTech Frontend

React + Vite dashboard for KisanFinTech.

## Run

```bash
npm install
npm run dev
```

Frontend: http://localhost:5173
Backend: http://localhost:8080
ML API: http://localhost:8000

## API contract

The frontend sends only:

```json
{
  "location": "Bhopal",
  "pincode": "462003",
  "sowingDate": "2026-09-26"
}
```

to `POST /api/recommendations`.

The backend is responsible for geocoding, weather, soil classification, ML inference and market-data aggregation. React receives only processed dashboard data; it never reads the CSV and never calls the external weather/soil services directly.
