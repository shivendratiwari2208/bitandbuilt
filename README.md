# bitandbuilt
AI-Based Crop Recommendation & Price Prediction
1. The Problem

Farmers often have to decide which crop to grow based on many factors such as soil condition, weather, location, available land and expected market price.

Making this decision manually can be difficult because:

Different locations have different soil and weather conditions.
A crop that performs well in one area may not be suitable for another.
Farmers may not know which crop can provide better returns.
Crop prices can vary significantly around the harvest period.
Looking at soil, weather and price information separately makes the decision more complicated.

We identified the need for a system that can bring these factors together and give farmers a simple, data-driven comparison of suitable crops.

2. Our Solution

We built a real working AI/ML-based crop recommendation and price prediction system.

The user provides:

Pincode
Location
Area of land
Current date

The system sends this information to the backend. The backend collects relevant weather, soil and geolocation information and processes it before sending it to our machine learning model.

The model recommends suitable crops and provides their estimated probabilities. We then combine these recommendations with expected crop prices to calculate and compare the potential return per quintal.

Finally, the frontend displays the top 3 crops with the best expected return per quintal using an easy-to-understand graph.

3. How It Works
User Input
   ↓
Pincode + Location + Land Area + Current Date
   ↓
Backend
   ↓
Weather + Soil + Geolocation Data
   ↓
Data Processing
   ↓
Label Encoding
   ↓
KNN Machine Learning Model
   ↓
Crop Recommendations + Probability
   ↓
Price Prediction
   ↓
Minimum + Maximum + Average Expected Price
   ↓
Return per Quintal
   ↓
Top 3 Crops
   ↓
Frontend Graph

5. Machine Learning Model

We use a K-Nearest Neighbors (KNN) classifier for crop recommendation.

The model uses the following inputs:

Temperature
Humidity
Soil moisture
Soil type
Phosphorous

Since some inputs such as soil type are categorical, we use Label Encoding to convert them into numerical values before passing them to the KNN model.

Our current model achieves approximately 82% accuracy on the available dataset.

The model returns suitable crop recommendations along with their probability.

5. Price Prediction

After identifying suitable crops, the system also provides estimated:

Minimum price at harvest
Maximum price at harvest
Average price at harvest

These values are used to compare the expected return of different crops.

The system then identifies the top 3 crops based on expected return per quintal and displays them graphically on the frontend.

6. Why This Approach?

Instead of giving only a crop recommendation, our system combines:

Location + Weather + Soil + ML Recommendation + Expected Price

This gives the user a more complete picture when comparing different crop options.

The goal is to make the output simple enough that a user does not need to understand machine learning or complex agricultural datasets to use the system.

7. Key Features
Location-based crop recommendation
Weather-based analysis
Soil-based analysis
Geolocation information
Soil type and phosphorous consideration
KNN machine learning model
Approximately 82% model accuracy
Crop probability estimation
Harvest-time price estimation
Minimum, maximum and average price information
Return per quintal comparison
Top 3 crop recommendations
Graphical visualization
Real working ML model
Designed specifically using Madhya Pradesh data

8. Technology Used
Python
Machine Learning
K-Nearest Neighbors (KNN)
Label Encoding
Weather API
Soil API
Geolocation services
Backend API
Frontend web application
Data visualization

9. Project Structure. 
bitandbuilt/
│
├── ml-model/
│   ├── KNN.pkl
│   ├── le1.pkl
│   ├── le2.pkl
│   ├── Soil.ipynb
│   └── README.md
│
└── README.md
10. Data Scope

The current machine learning model is trained on Madhya Pradesh data.

Therefore, the system is currently intended to provide recommendations for Madhya Pradesh locations only.

The model should not be considered reliable for locations outside Madhya Pradesh until it is trained and validated using data from those regions.

11. Impact

Our goal is to help farmers move from simply asking:

"Which crop can I grow?"

to a more useful question:

"Which suitable crop can potentially give me better returns under my current conditions?"

By combining environmental conditions with crop price information, the system can provide a simple way to compare different crop options.

12. Ownership & Development

This project was built by Team The Optimizer for BIT N BUILD 2026.

The team understands the overall workflow, including the frontend, backend, data processing, machine learning model, prediction pipeline and output generation.

AI tools were used as development assistance where required, but the team is responsible for understanding and explaining the implementation and architecture of the project.

13. Important Note

The crop recommendations and prices generated by this system are data-driven estimates and should not be treated as guaranteed agricultural or market outcomes.

Actual crop prices and agricultural results can vary depending on market conditions, weather, soil conditions and other real-world factors.

Built for BIT N BUILD 2026 — Team The Optimizer


