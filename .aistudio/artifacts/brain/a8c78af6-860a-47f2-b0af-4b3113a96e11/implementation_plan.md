# Munna AI — Vercel 404 Fix (Live Web App + APK Portal) & GitHub Automatic APK Builder

यह योजना आपके `unique-public.vercel.app` पर आने वाले **404 (यह पेज मौजूद नहीं है)** एरर को जड़ से ठीक करती है और साथ ही **GitHub पर अपने-आप Android APK फ़ाइल बनाने और डाउनलोड करने** की सुविधा जोड़ती है।

## User Review & Critical Decisions

> [!IMPORTANT]
> आपके द्वारा चुने गए दोनों विकल्प इस योजना में शामिल हैं। नीचे **Proceed** पर क्लिक करते ही हम इसे लागू कर देंगे, जिसके बाद GitHub पर Push करते ही आपकी Vercel वेबसाइट भी चलेगी और फ़ोन के लिए APK भी बन जाएगी।

- **Confirmed Decision 1 (Vercel 404 Fix — Live Munna AI Web App + APK Download Page)**:
  - प्रोजेक्ट में वेब एंट्री और Vercel रूटिंग जोड़ी जाएगी ताकि `unique-public.vercel.app` खोलने पर `404` के बजाय **पूरा Munna AI वेब ऐप** (माइक, हिंदी/हिंग्लिश वॉइस कमांड्स, बोलती हुई आवाज़, शॉर्टकट्स, इतिहास, सेटिंग्स) और **"Download Android APK"** बटन दिखे।
- **Confirmed Decision 2 (GitHub Actions Automatic APK Builder)**:
  - जब भी आप कोड को GitHub पर भेजेंगे, GitHub अपने-आप Android प्रोजेक्ट को बिल्ड करके `Munna-AI.apk` तैयार करेगा (जिसे आप GitHub के **Actions** या **Releases** टैब से सीधे अपने फ़ोन में डाउनलोड कर सकेंगे)।

---

## 1. Overview & Core Concept

- **What It Does**: एक ही GitHub रिपॉजिटरी से दोनों काम पूरे होंगे:
  1. **Vercel** पर आपकी वेबसाइट (`unique-public.vercel.app`) बिना किसी 404 एरर के लाइव चलेगी।
  2. **Android फ़ोन** के लिए असली Native Android कोड भी सुरक्षित रहेगा और GitHub हर अपडेट पर अपने-आप इंस्टॉल करने योग्य `.apk` फ़ाइल बना देगा।
- **Target Audience / Persona**: मोबाइल से काम करने वाले डेवलपर्स और यूज़र्स जो अपनी ऐप को वेब लिंक (Vercel) और Android APK दोनों रूपों में चलाना और शेयर करना चाहते हैं।
- **Key Value**: न तो Vercel पर 404 आएगा, और न ही आपको APK बनाने के लिए कंप्यूटर या Android Studio की ज़रूरत पड़ेगी।

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Vercel वेबसाइट खोलने पर (`unique-public.vercel.app`)**:
     - यूज़र को गहरा नेवी ब्लू और स्काई-ब्लू **Munna AI (“Bolo, Munna Karega!”)** इंटरफ़ेस दिखेगा।
     - ऊपर **"📲 Android APK डाउनलोड करें"** का चमकता हुआ बटन होगा, और बीच में लाइव एनिमेटेड माइक होगा जो ब्राउज़र के Web Speech API से हिंदी/हिंग्लिश कमांड सुनेगा और बोलकर जवाब देगा।
  2. **GitHub से अपने फ़ोन में APK डाउनलोड करने पर**:
     - GitHub पर कोड पुश होते ही ऑटोमैटिक बिल्ड चलेगा और तैयार `Munna-AI.apk` फ़ाइल डाउनलोड के लिए उपलब्ध हो जाएगी।
  3. **ऐप के अंदर गाइड**:
     - Android ऐप के सेटअप हब में भी Vercel और GitHub से APK पाने के आसान हिंदी स्टेप्स दिखेंगे।

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: आधुनिक, प्रीमियम डार्क नेवी और सियान ग्लो इंटरफ़ेस (मोबाइल-फ़र्स्ट रिस्पॉन्सिव लेआउट)।
  - *Color Palette & Mood*:
    - `--background`: Deep Navy Blue (`#071124`)
    - `--surface`: Lighter Navy Card (`#102240`)
    - `--primary`: Sky Blue (`#38BDF8`)
    - `--accent`: Soft Cyan Glow (`#22D3EE`)
  - *Typography & Hierarchy*: साफ़ हिंदी देवनागरी और अंग्रेज़ी फ़ॉन्ट्स, बड़े टच बटन और बिना हॉरिजॉन्टल स्क्रॉल वाला मोबाइल लेआउट।

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: एक ही प्रोजेक्ट में Native Android + Static Web Entry का सह-अस्तित्व**
  - *Chosen Approach*: Android के बिल्ड सिस्टम (Gradle) को छेड़े बिना प्रोजेक्ट के रूट और पब्लिक आउटपुट में वेब असिस्टेंट और Vercel कॉन्फ़िगरेशन रखना।
  - *Why*: इससे AI Studio का Android Emulator भी 100% काम करता रहेगा और Vercel को भी तुरंत वेबसाइट फ़ाइल मिल जाएगी, जिससे 404 एरर तुरंत खत्म हो जाएगा।
- **Decision 2: GitHub Actions में Java 17 + Android SDK के साथ ऑटोमैटिक APK बिल्ड**
  - *Chosen Approach*: क्लाउड रनर पर Gradle के ज़रिए Debug APK बनाना और उसे Artifact व Release के रूप में अपलोड करना।
  - *Why*: यूज़र केवल अपने मोबाइल ब्राउज़र से GitHub खोलकर एक टैप में `.apk` डाउनलोड कर सकता है।

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Deployment Flow Diagram**:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     Single Unified GitHub Repository                    │
└───────────────────┬─────────────────────────────────┬───────────────────┘
                    │                                 │
       (1) Automatic Web Deploy          (2) Automatic Cloud APK Build
                    │                                 │
                    ▼                                 ▼
┌───────────────────────────────────────┐ ┌───────────────────────────────┐
│     Vercel (unique-public.vercel.app) │ │    GitHub Actions Workflow    │
│ • Zero-Config Static Web Routing      │ │ • Sets up JDK & Android SDK   │
│ • Live Munna AI Web Voice Assistant   │ │ • Runs Gradle assembleDebug   │
│ • Web Speech API + Hindi TTS          │ │ • Publishes Munna-AI.apk to   │
│ • Direct "Download Android APK" Card  │ │   Artifacts & GitHub Releases │
└───────────────────────────────────────┘ └───────────────────────────────┘
```

- **Interactive Component & State Mapping**:
  - **Web Voice & Command Engine**: ब्राउज़र में `webkitSpeechRecognition` / `SpeechRecognition` (`hi-IN`) और `speechSynthesis` का उपयोग करके YouTube, WhatsApp Web, Google, समय/तारीख, और चैट कमांड्स को तुरंत चलाता है, और यह स्पष्ट बताता है कि फ़ोन के इंस्टॉल्ड ऐप्स और फ़्लोटिंग माइक के लिए ऊपर दिए गए बटन से Native Android APK इंस्टॉल करें।
  - **Vercel Routing Configuration**: सभी वेब रिक्वेस्ट्स को मुख्य वेब ऐप पर रूट करता है ताकि किसी भी लिंक पर `404: NOT_FOUND` न आए।
