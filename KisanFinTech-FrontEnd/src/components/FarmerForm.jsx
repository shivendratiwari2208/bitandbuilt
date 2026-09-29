import { useState } from "react";
import { useLanguage } from "../i18n";
function FarmerForm({ onSubmit }) {
  const { t } = useLanguage();
  const [formData, setFormData] = useState({ location:"", pincode:"", area:"", sowingDate:"" });
  const [errors, setErrors] = useState({});
  const handleChange = (event) => { const {name,value}=event.target; setFormData(p=>({...p,[name]:value})); setErrors(p=>({...p,[name]:""})); };
  const validate = () => { const e={}; if(!formData.location.trim())e.location=t("requiredLocation"); if(!formData.pincode.trim())e.pincode=t("requiredPincode"); else if(!/^\d{6}$/.test(formData.pincode))e.pincode=t("validPincode"); if(!formData.area||Number(formData.area)<=0)e.area=t("requiredArea"); if(!formData.sowingDate)e.sowingDate=t("requiredSowing"); return e; };
  const handleSubmit=(event)=>{event.preventDefault();const e=validate();if(Object.keys(e).length){setErrors(e);return;}onSubmit(formData);};
  return <form className="farmer-form" onSubmit={handleSubmit}>
    <div className="form-section"><div className="form-section-title"><span>📍</span><div><h2>{t("farmLocation")}</h2><p>{t("farmLocationLead")}</p></div></div><div className="form-grid">
      <div className="form-group"><label htmlFor="location">{t("location")}<span className="required">*</span></label><input id="location" name="location" value={formData.location} onChange={handleChange} placeholder={t("locationPlaceholder")} />{errors.location&&<span className="field-error">{errors.location}</span>}</div>
      <div className="form-group"><label htmlFor="area">{t("area")}<span className="required">*</span></label><input id="area" name="area" type="number" min="0.1" step="0.1" value={formData.area} onChange={handleChange} placeholder={t("areaPlaceholder")} />{errors.area&&<span className="field-error">{errors.area}</span>}</div>
      <div className="form-group"><label htmlFor="pincode">{t("pincode")}<span className="required">*</span></label><input id="pincode" name="pincode" inputMode="numeric" maxLength="6" value={formData.pincode} onChange={e=>handleChange({target:{name:"pincode",value:e.target.value.replace(/\D/g,"")}})} placeholder={t("pincodePlaceholder")} />{errors.pincode&&<span className="field-error">{errors.pincode}</span>}</div>
    </div></div>
    <div className="form-section"><div className="form-section-title"><span>🌱</span><div><h2>{t("cropDetails")}</h2><p>{t("cropDetailsLead")}</p></div></div><div className="form-group"><label htmlFor="sowingDate">{t("sowingDate")}<span className="required">*</span></label><input id="sowingDate" name="sowingDate" type="date" value={formData.sowingDate} onChange={handleChange} />{errors.sowingDate&&<span className="field-error">{errors.sowingDate}</span>}</div></div>
    <button type="submit" className="primary-button submit-button">{t("getRecommendation")} <span>→</span></button><p className="form-note">{t("analyzedBy")}</p>
  </form>;
}
export default FarmerForm;
