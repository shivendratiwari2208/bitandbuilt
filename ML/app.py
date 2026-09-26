from flask import Flask, request, jsonify
from flask_cors import CORS
import pickle
import pandas as pd

app = Flask(__name__)
CORS(app)

knn = pickle.load(open("KNN.pkl", "rb"))
le1 = pickle.load(open("le1.pkl", "rb"))
le2 = pickle.load(open("le2.pkl", "rb"))


@app.route("/predict", methods=["POST"])
def predict():

    data = request.get_json()

    temperature = data["temperature"]
    humidity = data["humidity"]
    moisture = data["moisture"]
    soil_type = data["soilType"]
    phosphorous = data["phosphorous"]

    soil_encoded = le2.transform([soil_type])[0]

    input_data = pd.DataFrame(
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

    prediction = knn.predict(input_data)[0]

    crop_name = le1.inverse_transform([prediction])[0]

    probabilities = knn.predict_proba(input_data)[0]
    crops = le1.classes_

    predictions = []

    for crop, probability in zip(crops, probabilities):
        predictions.append({
            "crop": crop,
            "probability": float(probability)
        })

    predictions.sort(
        key=lambda x: x["probability"],
        reverse=True
    )

    return jsonify({
        "recommendedCrop": crop_name,
        "predictions": predictions
    })


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=8000,
        debug=True
    )