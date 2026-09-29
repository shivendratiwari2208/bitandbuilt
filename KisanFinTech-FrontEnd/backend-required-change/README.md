# Backend change required for real fertilizer display

The Flask ML service already returns `recommendedFertilizer` and `fertilizerPredictions`, but the current Spring Boot `RecommendationService` does not forward those fields to the frontend.

Replace the existing `RecommendationService.java` with the included file, then restart Spring Boot.

The frontend will then display the real fertilizer model output.
