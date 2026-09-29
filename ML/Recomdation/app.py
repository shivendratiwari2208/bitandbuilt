from flask import Flask, request, jsonify
from flask_cors import CORS

import pickle
import pandas as pd


app = Flask(__name__)
CORS(app)


# =========================================================
# CROP RECOMMENDATION MODEL
# =========================================================

knn = pickle.load(
    open("KNN.pkl", "rb")
)

crop_label_encoder = pickle.load(
    open("le1.pkl", "rb")
)

soil_label_encoder = pickle.load(
    open("le2.pkl", "rb")
)


# =========================================================
# FERTILIZER RECOMMENDATION MODEL
# =========================================================

fertilizer_model = pickle.load(
    open("bernouli_model.pkl", "rb")
)

crop_onehot_encoder = pickle.load(
    open("onh(crops).pkl", "rb")
)

soil_onehot_encoder = pickle.load(
    open("onh(soil).pkl", "rb")
)


# =========================================================
# MARKET DATA
# =========================================================

market_data = pd.read_csv(
    "bhopal_crop_profit_data.csv"
)


# Average Bhopal mandi price for every crop
market_summary = (
    market_data
    .groupby("Crop")
    .agg(
        averageMarketPrice=(
            "Bhopal_Mandi_Price_Rs_q",
            "mean"
        ),
        minimumMarketPrice=(
            "Bhopal_Mandi_Price_Rs_q",
            "min"
        ),
        maximumMarketPrice=(
            "Bhopal_Mandi_Price_Rs_q",
            "max"
        ),
        averageMSP=(
            "MSP_Rs_q",
            "mean"
        ),
        priceObservations=(
            "Bhopal_Mandi_Price_Rs_q",
            "count"
        )
    )
    .reset_index()
)


# Convert dataframe to dictionary
market_lookup = {}

for _, row in market_summary.iterrows():

    crop = row["Crop"]

    market_lookup[crop] = {

        "averageMarketPrice": round(
            float(row["averageMarketPrice"]),
            2
        ),

        "minimumMarketPrice": round(
            float(row["minimumMarketPrice"]),
            2
        ),

        "maximumMarketPrice": round(
            float(row["maximumMarketPrice"]),
            2
        ),

        "averageMSP": round(
            float(row["averageMSP"]),
            2
        ),

        "priceObservations": int(
            row["priceObservations"]
        )
    }


# =========================================================
# MARKET HELPER
# =========================================================

def get_market_data(crop):

    if crop in market_lookup:

        return market_lookup[crop]

    return {

        "averageMarketPrice": None,

        "minimumMarketPrice": None,

        "maximumMarketPrice": None,

        "averageMSP": None,

        "priceObservations": 0
    }


# =========================================================
# FERTILIZER INPUT BUILDER
# =========================================================

def build_fertilizer_input(
    temperature,
    humidity,
    moisture,
    phosphorous,
    recommended_crop,
    soil_type
):

    # -----------------------------------------------------
    # Encode crop
    # -----------------------------------------------------

    crop_df = pd.DataFrame({
        "Crop Type": [recommended_crop]
    })

    crop_encoded = crop_onehot_encoder.transform(
        crop_df
    )


    # -----------------------------------------------------
    # Encode soil
    # -----------------------------------------------------

    soil_df = pd.DataFrame({
        "Soil Type": [soil_type]
    })

    soil_encoded = soil_onehot_encoder.transform(
        soil_df
    )


    # -----------------------------------------------------
    # Get exact features used during model training
    # -----------------------------------------------------

    expected_features = (
        fertilizer_model.feature_names_in_
    )


    # Create an input containing exactly
    # the features expected by the model
    fertilizer_input = pd.DataFrame(
        0.0,
        index=[0],
        columns=expected_features
    )


    # -----------------------------------------------------
    # Numeric features
    # -----------------------------------------------------

    numeric_values = {

        "Temparature": temperature,

        "Temperature": temperature,

        "Humidity": humidity,

        "Moisture": moisture,

        "Phosphorous": phosphorous
    }


    for feature, value in numeric_values.items():

        if feature in fertilizer_input.columns:

            fertilizer_input.loc[
                0,
                feature
            ] = value


    # -----------------------------------------------------
    # Crop one-hot features
    # -----------------------------------------------------

    crop_feature_names = (
        crop_onehot_encoder
        .get_feature_names_out()
    )


    if hasattr(crop_encoded, "toarray"):

        crop_values = (
            crop_encoded
            .toarray()[0]
        )

    else:

        crop_values = crop_encoded[0]


    for feature_name, value in zip(
        crop_feature_names,
        crop_values
    ):

        clean_name = feature_name


        # Example:
        # Crop Type_Barley
        # becomes:
        # Barley

        if clean_name.startswith(
            "Crop Type_"
        ):

            clean_name = clean_name.replace(
                "Crop Type_",
                "",
                1
            )


        if clean_name in fertilizer_input.columns:

            fertilizer_input.loc[
                0,
                clean_name
            ] = float(value)


    # -----------------------------------------------------
    # Soil one-hot features
    # -----------------------------------------------------

    soil_feature_names = (
        soil_onehot_encoder
        .get_feature_names_out()
    )


    if hasattr(
        soil_encoded,
        "toarray"
    ):

        soil_values = (
            soil_encoded
            .toarray()[0]
        )

    else:

        soil_values = soil_encoded[0]


    for feature_name, value in zip(
        soil_feature_names,
        soil_values
    ):

        clean_name = feature_name


        # Example:
        # Soil Type_Black
        # becomes:
        # Black

        if clean_name.startswith(
            "Soil Type_"
        ):

            clean_name = clean_name.replace(
                "Soil Type_",
                "",
                1
            )


        if clean_name in fertilizer_input.columns:

            fertilizer_input.loc[
                0,
                clean_name
            ] = float(value)


    return fertilizer_input


# =========================================================
# MAIN PREDICTION API
# =========================================================

@app.route(
    "/predict",
    methods=["POST"]
)
def predict():

    data = request.get_json()


    # =====================================================
    # VALIDATE REQUEST
    # =====================================================

    if not data:

        return jsonify({

            "error":
                "Request body is required"

        }), 400


    # =====================================================
    # INPUT
    # =====================================================

    try:

        temperature = float(
            data["temperature"]
        )

        humidity = float(
            data["humidity"]
        )

        moisture = float(
            data["moisture"]
        )

        soil_type = data["soilType"]

        phosphorous = float(
            data["phosphorous"]
        )

    except (
        KeyError,
        TypeError,
        ValueError
    ) as e:

        return jsonify({

            "error":
                "Invalid or missing input",

            "details":
                str(e)

        }), 400


    # =====================================================
    # 1. CROP RECOMMENDATION
    # =====================================================

    try:

        soil_encoded = (
            soil_label_encoder
            .transform(
                [soil_type]
            )[0]
        )

    except Exception:

        return jsonify({

            "error":
                "Unknown soil type",

            "soilType":
                soil_type

        }), 400


    # -----------------------------------------------------
    # Create crop model input
    # -----------------------------------------------------

    crop_input = pd.DataFrame(

        [[
            temperature,
            humidity,
            moisture,
            soil_encoded,
            phosphorous
        ]],

        columns=[

            "Temparature",

            "Humidity",

            "Moisture",

            "Soil Type",

            "Phosphorous"
        ]
    )


    # -----------------------------------------------------
    # Crop prediction
    # -----------------------------------------------------

    try:

        crop_prediction = (
            knn.predict(
                crop_input
            )[0]
        )

    except Exception as e:

        return jsonify({

            "error":
                "Crop prediction failed",

            "details":
                str(e)

        }), 500


    recommended_crop = (
        crop_label_encoder
        .inverse_transform(
            [crop_prediction]
        )[0]
    )


    # =====================================================
    # CROP PROBABILITIES
    # =====================================================

    try:

        probabilities = (
            knn.predict_proba(
                crop_input
            )[0]
        )

    except Exception as e:

        return jsonify({

            "error":
                "Crop probability calculation failed",

            "details":
                str(e)

        }), 500


    crops = (
        crop_label_encoder.classes_
    )


    crop_predictions = []


    for crop, probability in zip(
        crops,
        probabilities
    ):

        crop_predictions.append({

            "crop":
                crop,

            "probability":
                round(
                    float(probability),
                    4
                ),

            "probabilityPercent":
                round(
                    float(probability) * 100,
                    2
                ),

            "market":
                get_market_data(
                    crop
                )
        })


    crop_predictions.sort(

        key=lambda x:
            x["probability"],

        reverse=True
    )


    # =====================================================
    # 2. FERTILIZER RECOMMENDATION
    # =====================================================

    try:

        fertilizer_input = (
            build_fertilizer_input(

                temperature=temperature,

                humidity=humidity,

                moisture=moisture,

                phosphorous=phosphorous,

                recommended_crop=recommended_crop,

                soil_type=soil_type
            )
        )

    except Exception as e:

        return jsonify({

            "error":
                "Fertilizer encoding failed",

            "details":
                str(e)

        }), 500


    # =====================================================
    # FERTILIZER PREDICTION
    # =====================================================

    try:

        fertilizer_prediction = (
            fertilizer_model.predict(
                fertilizer_input
            )[0]
        )


        fertilizer_probabilities = (
            fertilizer_model.predict_proba(
                fertilizer_input
            )[0]
        )


    except Exception as e:

        return jsonify({

            "error":
                "Fertilizer prediction failed",

            "details":
                str(e),

            "modelFeatures":
                list(
                    fertilizer_model
                    .feature_names_in_
                ),

            "inputFeatures":
                list(
                    fertilizer_input.columns
                )

        }), 500


    fertilizer_classes = (
        fertilizer_model.classes_
    )


    fertilizer_predictions = []


    for fertilizer, probability in zip(
        fertilizer_classes,
        fertilizer_probabilities
    ):

        fertilizer_predictions.append({

            "fertilizer":
                fertilizer,

            "probability":
                round(
                    float(probability),
                    4
                ),

            "probabilityPercent":
                round(
                    float(probability) * 100,
                    2
                )
        })


    fertilizer_predictions.sort(

        key=lambda x:
            x["probability"],

        reverse=True
    )


    # =====================================================
    # MARKET DATA FOR RECOMMENDED CROP
    # =====================================================

    recommended_market = (
        get_market_data(
            recommended_crop
        )
    )


    # =====================================================
    # FINAL RESPONSE
    # =====================================================

    return jsonify({

        "recommendedCrop":
            recommended_crop,


        "cropPredictions":
            crop_predictions,


        # Backward compatibility
        # with existing frontend/backend
        "predictions":
            crop_predictions,


        "recommendedFertilizer":
            fertilizer_prediction,


        "fertilizerPredictions":
            fertilizer_predictions,


        "recommendedCropMarket":
            recommended_market,


        "marketDataSource":
            "Bhopal crop profit dataset",


        "input": {

            "temperature":
                temperature,

            "humidity":
                humidity,

            "moisture":
                moisture,

            "soilType":
                soil_type,

            "phosphorous":
                phosphorous
        }

    })


# =========================================================
# HEALTH CHECK
# =========================================================

@app.route(
    "/health",
    methods=["GET"]
)
def health():

    return jsonify({

        "status":
            "UP",

        "service":
            "KisanFinTech ML",

        "models": {

            "cropRecommendation":
                True,

            "fertilizerRecommendation":
                True,

            "marketData":
                True
        }

    })


# =========================================================
# RUN
# =========================================================

if __name__ == "__main__":

    app.run(

        host="0.0.0.0",

        port=8000,

        debug=True
    )