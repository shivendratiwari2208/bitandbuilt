import { useState } from "react";

function FarmerForm({ onSubmit }) {
  const [formData, setFormData] = useState({
    location: "",
    pincode: "",
    sowingDate: ""
  });

  const [errors, setErrors] = useState({});

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((previous) => ({ ...previous, [name]: value }));
    setErrors((previous) => ({ ...previous, [name]: "" }));
  };

  const validate = () => {
    const next = {};
    if (!formData.location.trim()) next.location = "Location is required";
    if (!/^\d{6}$/.test(formData.pincode)) {
      next.pincode = "Enter a valid 6-digit Indian pincode";
    }
    if (!formData.sowingDate) next.sowingDate = "Sowing date is required";
    return next;
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length) {
      setErrors(validationErrors);
      return;
    }
    onSubmit(formData);
  };

  return (
    <form className="farmer-form" onSubmit={handleSubmit}>
      <div className="form-section">
        <div className="form-section-title">
          <span>📍</span>
          <div>
            <h2>Farm Location</h2>
            <p>Where is your farm located?</p>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="location">Location / Address <span className="required">*</span></label>
          <input id="location" name="location" value={formData.location} onChange={handleChange} placeholder="Enter your village, city or area" />
          {errors.location && <span className="field-error">{errors.location}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="pincode">Pincode <span className="required">*</span></label>
          <input
            id="pincode"
            name="pincode"
            type="text"
            inputMode="numeric"
            maxLength="6"
            value={formData.pincode}
            onChange={(event) => handleChange({ target: { name: "pincode", value: event.target.value.replace(/\D/g, "") } })}
            placeholder="Enter 6-digit pincode"
          />
          {errors.pincode && <span className="field-error">{errors.pincode}</span>}
        </div>
      </div>

      <div className="form-section">
        <div className="form-section-title">
          <span>🌱</span>
          <div>
            <h2>Crop Details</h2>
            <p>Tell us when you plan to sow</p>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="sowingDate">Sowing Date <span className="required">*</span></label>
          <input id="sowingDate" name="sowingDate" type="date" value={formData.sowingDate} onChange={handleChange} />
          {errors.sowingDate && <span className="field-error">{errors.sowingDate}</span>}
        </div>
      </div>

      <button type="submit" className="primary-button submit-button">
        Get Crop Recommendation <span>→</span>
      </button>

      <p className="form-note">Only location, pincode and sowing date are required from the farmer.</p>
    </form>
  );
}

export default FarmerForm;
