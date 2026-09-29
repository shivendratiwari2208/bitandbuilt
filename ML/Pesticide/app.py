from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
import pickle
import os


app = Flask(__name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))


# ============================================================
# MODEL FILES
# ============================================================

LIKELY_PEST_MODEL_PATH = os.path.join(
    BASE_DIR,
    "DescisionClassifier.pkl"
)

PESTICIDE_MODEL_PATH = os.path.join(
    BASE_DIR,
    "Dtc_cure.pkl"
)

RISK_MODEL_PATH = os.path.join(
    BASE_DIR,
    "dtf.pkl"
)

LIKELY_PEST_ENCODER_PATH = os.path.join(
    BASE_DIR,
    "le_likely_pest"
)

BEST_PESTICIDE_ENCODER_PATH = os.path.join(
    BASE_DIR,
    "le_best_pesticide"
)


# ============================================================
# CROPS
# ============================================================

CROPS = [
    "Barley",
    "Cotton",
    "Ground Nuts",
    "Maize",
    "Millets",
    "Oil seeds",
    "Paddy",
    "Pulses",
    "Sugarcane",
    "Tobacco",
    "Wheat"
]


# ============================================================
# LOAD MODELS
# ============================================================

with open(RISK_MODEL_PATH, "rb") as f:
    risk_model = pickle.load(f)

with open(LIKELY_PEST_MODEL_PATH, "rb") as f:
    likely_pest_model = pickle.load(f)

with open(PESTICIDE_MODEL_PATH, "rb") as f:
    pesticide_model = pickle.load(f)

with open(LIKELY_PEST_ENCODER_PATH, "rb") as f:
    likely_pest_encoder = pickle.load(f)

with open(BEST_PESTICIDE_ENCODER_PATH, "rb") as f:
    best_pesticide_encoder = pickle.load(f)


print("======================================")
print("Pesticide ML models loaded")
print("======================================")

print("Risk model:", type(risk_model).__name__)
print("Likely pest model:", type(likely_pest_model).__name__)
print("Pesticide model:", type(pesticide_model).__name__)

print("\nRisk model features:")
print(list(risk_model.feature_names_in_))

print("\nLikely pest model features:")
print(list(likely_pest_model.feature_names_in_))

print("\nPesticide model features:")
print(list(pesticide_model.feature_names_in_))

print("======================================\n")


# ============================================================
# CREATE BASE FEATURES
# ============================================================

def create_base_features(
    temperature,
    humidity,
    rainfall,
    soil_moisture,
    crop
):
    """
    Creates the exact 15 features expected by
    dtf.pkl and DescisionClassifier.pkl.
    """

    data = {
        "Temperature_C": float(temperature),
        "Humidity_pct": float(humidity),
        "Rainfall_mm": float(rainfall),
        "Soil_Moisture_pct": float(soil_moisture)
    }

    # One-hot encode crop
    for crop_name in CROPS:
        data[crop_name] = (
            1 if crop_name == crop else 0
        )

    return pd.DataFrame([data])


# ============================================================
# CREATE PESTICIDE FEATURES
# ============================================================

def create_pesticide_features(
    base_features,
    pest_risk
):
    """
    Adds Pest_Risk_Pct to the 15 base features.

    Creates the exact 16 features expected by
    Dtc_cure.pkl.
    """

    features = base_features.copy()

    features["Pest_Risk_Pct"] = float(pest_risk)

    # Reorder exactly according to trained model
    expected_columns = list(
        pesticide_model.feature_names_in_
    )

    features = features[expected_columns]

    return features


# ============================================================
# RISK LEVEL
# ============================================================

def get_risk_level(pest_risk):

    if pest_risk < 30:
        return "Low"

    elif pest_risk < 60:
        return "Medium"

    else:
        return "High"


# ============================================================
# SAFE ENCODER DECODING
# ============================================================

def decode_label(
    encoder,
    prediction
):

    prediction = np.asarray(
        prediction
    ).reshape(-1)[0]

    try:

        # If model returns encoded integer
        return encoder.inverse_transform(
            [int(prediction)]
        )[0]

    except Exception:

        # If model already returns string
        return str(prediction)


# ============================================================
# PREDICT
# ============================================================

@app.route("/predict", methods=["POST"])
def predict():

    try:

        data = request.get_json()

        if not data:

            return jsonify({
                "error": "Request body is required"
            }), 400


        # ----------------------------------------------------
        # INPUT
        # ----------------------------------------------------

        crop = data.get("crop")

        temperature = data.get("temperature")

        humidity = data.get("humidity")

        rainfall = data.get("rainfall")

        soil_moisture = data.get("soilMoisture")


        # ----------------------------------------------------
        # VALIDATION
        # ----------------------------------------------------

        if not crop:

            return jsonify({
                "error": "Crop is required"
            }), 400


        if crop not in CROPS:

            return jsonify({
                "error": "Invalid crop",
                "allowedCrops": CROPS
            }), 400


        if temperature is None:
            return jsonify({
                "error": "Temperature is required"
            }), 400


        if humidity is None:
            return jsonify({
                "error": "Humidity is required"
            }), 400


        if rainfall is None:
            return jsonify({
                "error": "Rainfall is required"
            }), 400


        if soil_moisture is None:
            return jsonify({
                "error": "Soil moisture is required"
            }), 400


        # ----------------------------------------------------
        # CONVERT NUMBERS
        # ----------------------------------------------------

        temperature = float(temperature)

        humidity = float(humidity)

        rainfall = float(rainfall)

        soil_moisture = float(soil_moisture)


        # ----------------------------------------------------
        # BASE FEATURES
        # ----------------------------------------------------

        base_features = create_base_features(
            temperature,
            humidity,
            rainfall,
            soil_moisture,
            crop
        )


        # Ensure exact order for risk model
        risk_features = base_features[
            list(risk_model.feature_names_in_)
        ]


        print("\n======================================")
        print("PESTICIDE PREDICTION")
        print("======================================")

        print("Crop:", crop)
        print("Temperature:", temperature)
        print("Humidity:", humidity)
        print("Rainfall:", rainfall)
        print("Soil Moisture:", soil_moisture)

        print("\nRisk model features:")
        print(risk_features)


        # ====================================================
        # 1. PEST RISK
        # ====================================================

        risk_prediction = risk_model.predict(
            risk_features
        )

        pest_risk = float(
            np.asarray(
                risk_prediction
            ).reshape(-1)[0]
        )


        # Dataset contains Pest_Risk_Pct directly
        # in percentage form, so keep it as percentage.

        pest_risk = max(
            0.0,
            min(100.0, pest_risk)
        )


        print(
            "\nPest Risk:",
            pest_risk
        )


        # ====================================================
        # 2. RISK LEVEL
        # ====================================================

        risk_level = get_risk_level(
            pest_risk
        )


        print(
            "Risk Level:",
            risk_level
        )


        # ====================================================
        # 3. LIKELY PEST
        # ====================================================

        likely_pest_features = base_features[
            list(
                likely_pest_model.feature_names_in_
            )
        ]


        pest_prediction = (
            likely_pest_model.predict(
                likely_pest_features
            )
        )


        likely_pest = decode_label(
            likely_pest_encoder,
            pest_prediction
        )


        print(
            "Likely Pest:",
            likely_pest
        )


        # ====================================================
        # 4. BEST PESTICIDE
        # ====================================================

        pesticide_features = (
            create_pesticide_features(
                base_features,
                pest_risk
            )
        )


        print(
            "\nPesticide model features:"
        )

        print(
            pesticide_features
        )


        pesticide_prediction = (
            pesticide_model.predict(
                pesticide_features
            )
        )


        best_pesticide = decode_label(
            best_pesticide_encoder,
            pesticide_prediction
        )


        print(
            "Best Pesticide:",
            best_pesticide
        )


        # ====================================================
        # RESPONSE
        # ====================================================

        result = {

            "crop": crop,

            "Temperature_C": temperature,

            "Humidity_pct": humidity,

            "Rainfall_mm": rainfall,

            "Soil_Moisture_pct": soil_moisture,

            "Pest_Risk_Pct": round(
                pest_risk,
                2
            ),

            "Risk_Level": risk_level,

            "Likely_Pest": likely_pest,

            "Best_Pesticide": best_pesticide
        }


        print(
            "\nFinal result:"
        )

        print(
            result
        )

        print(
            "======================================\n"
        )


        return jsonify(result)


    except Exception as e:

        print(
            "\n======================================"
        )

        print(
            "PREDICTION ERROR"
        )

        print(
            "======================================"
        )

        print(
            repr(e)
        )

        print(
            "======================================\n"
        )


        return jsonify({

            "error": "Prediction failed",

            "message": str(e)

        }), 500


# ============================================================
# HEALTH CHECK
# ============================================================

@app.route("/", methods=["GET"])
def home():

    return jsonify({

        "service": "KisanFinTech Pesticide ML",

        "status": "running",

        "port": 8001,

        "crops": CROPS

    })


# ============================================================
# RUN
# ============================================================

if __name__ == "__main__":

    app.run(
        host="0.0.0.0",
        port=8001,
        debug=True
    )