# KisanFinTech Backend

## Flow
Farmer -> Spring Boot -> Nominatim -> Open-Meteo + Esri -> 5 ML features -> Flask KNN -> Spring Boot market enrichment -> React.

## Input
POST `/api/recommendations`
```json
{
  "location": "Bhopal",
  "pincode": "462003",
  "sowingDate": "2026-09-26"
}
```

## ML bridge
Run the Python API on port 8000. It accepts `temperature`, `humidity`, `moisture`, `soilType`, and `phosphorous`.

## Market data
`src/main/resources/data/crop_price_dataset.csv` is loaded once at startup. The backend sends only the processed market data needed by the dashboard: top-3 crop metrics and 12 monthly price points for those crops. Raw CSV rows are never sent to React.

## Important
The current Black-soil phosphorus value is 10.0 because that is the value used by the verified local integration. Other soil classes intentionally fail fast until their actual training-data averages are configured in `application.properties`; this avoids silently feeding invented values to the KNN model.
