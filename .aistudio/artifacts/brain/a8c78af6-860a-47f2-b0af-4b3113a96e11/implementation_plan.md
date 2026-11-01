# Munna AI — GitHub Actions APK Build Fix & Direct Download Plan

## 1. समस्या का कारण (Root Cause Analysis from Your Screenshots)
आपके स्क्रीनशॉट (`github.com/aipartnerZone/Unique`) में साफ़ दिख रहा है कि **GitHub Actions** का `build-apk` जॉब **`Setup Android SDK`** स्टेप पर इस एरर के कारण रुक गया:
- **`Warning: Failed to find package 'tools'`** (`android-actions/setup-android@v3` पुराने `tools` पैकेज को ढूँढ रहा था जिसे Google ने Android SDK से हटा दिया है)।
- इसके अलावा, इस प्रोजेक्ट में **Gradle 9.3.1** और **AGP 9.1.1 (`compileSdk 36.1`)** का उपयोग हो रहा है, जिसके लिए GitHub Actions में सही Gradle वर्ज़न (`9.3.1`) और JDK (`21`) होना ज़रूरी है।

---

## 2. हम क्या सुधार करेंगे (Proposed Fixes)

### A. `.github/workflows/build-android-apk.yml` को 100% ठीक करना
1. **पुराने `android-actions/setup-android@v3` को हटाना:** इसकी जगह सीधे GitHub Ubuntu सर्वर के प्री-इंस्टॉल्ड `$ANDROID_HOME` SDK का उपयोग करेंगे और कमांड लाइन से सभी लाइसेंस (`sdkmanager --licenses`) व `platforms;android-36.1` और `build-tools;36.0.0` इंस्टॉल करेंगे—जिससे `Failed to find package 'tools'` एरर कभी नहीं आएगा।
2. **Gradle 9.3.1 और JDK 21 सेटअप:** प्रोजेक्ट के `gradle-wrapper.properties` के अनुसार **Gradle `9.3.1`** और **JDK `21`** सेट करेंगे ताकि `gradle :app:assembleDebug` बिना किसी रुकावट के पूरा हो।
3. **Automatic Release (`Munna-AI.apk`):** बिल्ड पूरा होते ही `Munna-AI.apk` सीधे आपके रिपॉजिटरी (`aipartnerZone/Unique`) के **Releases (`latest`)** और **Actions Artifacts** में अपलोड हो जाएगा।

### B. Vercel पोर्टल (`index.html` और `public/index.html`) में आपका GitHub Repo पहले से सेट करना
- आपके स्क्रीनशॉट से आपका रिपॉजिटरी नाम **`aipartnerZone/Unique`** मिल गया है।
- हम `index.html` और `public/index.html` में **`aipartnerZone/Unique`** को पहले से डिफ़ॉल्ट रूप में सेट कर देंगे, ताकि आपको वेबसाइट पर बार-बार रिपॉजिटरी का नाम टाइप न करना पड़े और एक क्लिक में **`Munna-AI.apk`** डाउनलोड हो सके।
