const dictionaries = {
  crop: {
    soybean: "सोयाबीन", maize: "मक्का", corn: "मक्का", wheat: "गेहूँ", chickpea: "चना", gram: "चना",
    "green gram": "मूंग", moong: "मूंग", mustard: "सरसों", coriander: "धनिया", rice: "धान", cotton: "कपास",
    potato: "आलू", tomato: "टमाटर", onion: "प्याज", groundnut: "मूंगफली", sugarcane: "गन्ना"
  },
  risk: { low: "कम", medium: "मध्यम", high: "उच्च" },
  soil: {
    "black soil": "काली मिट्टी", "red soil": "लाल मिट्टी", "alluvial soil": "जलोढ़ मिट्टी", "loamy soil": "दोमट मिट्टी",
    "sandy soil": "बलुई मिट्टी", "clay soil": "चिकनी मिट्टी", "sandy loam": "बलुई दोमट मिट्टी"
  },
  disease: {
    "early blight (likely)": "अर्ली ब्लाइट (संभावित)", "late blight": "लेट ब्लाइट", "early blight": "अर्ली ब्लाइट",
    "powdery mildew": "पाउडरी मिल्ड्यू", "leaf spot": "पत्ती धब्बा रोग", "healthy": "स्वस्थ फसल"
  },
  month: {
    jan: "जनवरी", feb: "फ़रवरी", mar: "मार्च", apr: "अप्रैल", may: "मई", jun: "जून",
    jul: "जुलाई", aug: "अगस्त", sep: "सितंबर", oct: "अक्टूबर", nov: "नवंबर", dec: "दिसंबर",
    "month 1": "महीना 1", "month 2": "महीना 2", "month 3": "महीना 3", "month 4": "महीना 4", harvest: "कटाई"
  }
};
const lookup = (group, value) => typeof value === "string" ? (dictionaries[group]?.[value.trim().toLowerCase()] || value) : value;
export function localizeCrop(value, language) { return language === "hi" ? lookup("crop", value) : value; }
export function localizeRisk(value, language) { return language === "hi" ? lookup("risk", value) : value; }
export function localizeSoil(value, language) { return language === "hi" ? lookup("soil", value) : value; }
export function localizeDisease(value, language) { return language === "hi" ? lookup("disease", value) : value; }
export function localizeMonth(value, language) { return language === "hi" ? lookup("month", value) : value; }
export function localizeDate(value, language) {
  if (!value) return value;
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString(language === "hi" ? "hi-IN" : "en-IN", { day: "numeric", month: "long", year: "numeric" });
}
export function localizeNumber(value, language) {
  if (value === undefined || value === null || value === "") return "—";
  const number = Number(value);
  if (!Number.isFinite(number)) return "—";
  return number.toLocaleString(language === "hi" ? "hi-IN" : "en-IN");
}
export function localizeMoney(value, language) {
  if (value === undefined || value === null || value === "") return "—";
  return `₹${localizeNumber(value, language)}`;
}
const phraseTranslations = {
  "Short-duration crop that works well in a smaller sowing window.": "कम अवधि की फसल, जो छोटे बुवाई समय के लिए उपयुक्त है।",
  "Good choice for a cool, longer season and local market demand.": "ठंडे और लंबे मौसम तथा स्थानीय बाजार की मांग के लिए अच्छा विकल्प।",
  "A resilient option with moderate investment and steady returns.": "मध्यम निवेश और स्थिर लाभ वाला टिकाऊ विकल्प।",
  "Fits a full winter growing window with predictable harvesting.": "पूरे सर्दी के मौसम के लिए उपयुक्त और अनुमानित कटाई वाली फसल।",
  "Quick harvest crop for a short free-land period.": "कम खाली समय वाली जमीन के लिए जल्दी तैयार होने वाली फसल।",
  "Brown circular spots with ring-like patterns": "गोलाकार भूरे धब्बे, जिनमें छल्लेनुमा पैटर्न दिखाई दे सकते हैं।",
  "Yellowing around the leaf spots": "पत्ती के धब्बों के आसपास पीलापन।",
  "Older leaves affected first": "सबसे पहले पुरानी पत्तियाँ प्रभावित होना।",
  "Early blight is a common fungal leaf disease. It is easier to manage when treated early.": "अर्ली ब्लाइट एक सामान्य फफूंदजनित पत्ती रोग है। शुरुआती उपचार से इसे नियंत्रित करना आसान होता है।",
  "Remove heavily affected leaves, avoid overhead watering, and keep enough space between plants for airflow.": "बहुत प्रभावित पत्तियाँ हटा दें, ऊपर से पानी देने से बचें और हवा के आवागमन के लिए पौधों के बीच पर्याप्त दूरी रखें।"
};
export function localizeText(value, language) { return language === "hi" && typeof value === "string" ? (phraseTranslations[value] || value) : value; }
