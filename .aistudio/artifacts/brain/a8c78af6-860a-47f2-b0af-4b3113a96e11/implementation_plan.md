# Munna AI — फ़ोन सेटअप हब, एक्सटेंडेड ऐप लॉन्चर और हिंदी ट्रबलशूटिंग गाइड

यह योजना **Munna AI (“Bolo, Munna Karega!”)** को आपके असली Android फ़ोन पर और अधिक शक्तिशाली और आसान बनाने के लिए तीन नए फीचर्स जोड़ती है: **(1) सभी परमिशन्स को एक ही जगह से जांचने और चालू करने का 'फ़ोन सेटअप हब'**, **(2) आवाज़ से फ़ोन के अन्य ऐप्स (Camera, Settings, Dialer, Maps, Calculator, Clock, Play Store आदि) खोलने की सुविधा**, और **(3) हिंदी में APK इंस्टॉल, माइक/TTS आवाज़ और बैटरी ट्रबलशूटिंग गाइड**।

## User Review & Critical Decisions

> [!IMPORTANT]
> आपके द्वारा चुने गए तीनों फीचर्स को इस प्लान में शामिल किया गया है। नीचे **"Proceed"** बटन दबाते ही हम इन्हें ऐप में जोड़ देंगे।

- **Confirmed Decision 1 (One-Stop Permissions & Setup Hub)**: होम स्क्रीन और सेटिंग्स में एक आसान **"फ़ोन रेडीनेस और परमिशन कार्ड"** जोड़ा जाएगा जहाँ से माइक, फ़्लोटिंग ओवरले, नोटिफिकेशन एक्सेस और हिंदी TTS आवाज़ को एक टैप में चेक और चालू किया जा सकेगा।
- **Confirmed Decision 2 (Custom & System App Launching)**: YouTube, WhatsApp, Chrome और Files के साथ-साथ अब आप बोलकर **Camera, Settings, Phone Dialer, Calculator, Google Maps, Clock/Alarm, Gmail, Photos/Gallery और Play Store** जैसे ऐप्स भी खोल सकेंगे।
- **Confirmed Decision 3 (Hindi Battery, Voice & APK Guide)**: ऐप के अंदर एक इंटरैक्टिव **"फ़ोन में चलाने और समस्या समाधान (Troubleshooting) गाइड"** उपलब्ध होगी जो APK इंस्टॉल करने, हिंदी आवाज़ डाउनलोड करने और फ़्लोटिंग माइक को बैटरी सेवर से बचाने के सटीक स्टेप्स बताएगी।

---

## 1. Overview & Core Concept

- **What It Does**: यह अपडेट शुरुआती (Beginner) Android यूज़र्स को अपने फ़ोन में Munna AI इंस्टॉल करने के तुरंत बाद सभी ज़रूरी सेटिंग्स 1 मिनट में पूरी करने, ज़्यादा फ़ोन ऐप्स को हिंदी/हिंग्लिश आवाज़ से खोलने, और माइक या बैटरी से जुड़ी किसी भी समस्या को खुद हल करने में सक्षम बनाता है।
- **Target Audience / Persona**: वे भारतीय Android स्मार्टफोन यूज़र्स जो हिंदी या हिंग्लिश में बोलकर अपना फ़ोन आसानी से कंट्रोल करना चाहते हैं।
- **Key Value**: बार-बार अलग-अलग Android सेटिंग्स ढूँढने की झंझट खत्म होती है और वॉइस कमांड से खुलने वाले ऐप्स की संख्या दोगुनी से अधिक हो जाती है।

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **फ़ोन सेटअप और परमिशन चेकलिस्ट (One-Tap Readiness Hub)**:
     - होम स्क्रीन के ऊपरी हिस्से में एक कॉम्पैक्ट **"फ़ोन सेटअप स्थिति (Phone Setup Status)"** बैनर दिखेगा।
     - इस पर टैप करने पर चारों मुख्य क्षमताओं (Microphone, Floating Overlay, Notification Reader, और Hindi TTS Voice) की लाइव स्थिति दिखेगी और हर एक के सामने **"अभी चालू करें (Allow / Setup)"** बटन होगा।
  2. **विस्तारित ऐप लॉन्चिंग (Extended Voice App Launcher)**:
     - यूज़र बोल सकता है: *"Munna, Camera kholo"*, *"Settings kholo"*, *"Calculator chalu karo"*, *"Maps kholo"*, *"Phone dialer kholo"*, या किसी भी अन्य ऐप का नाम।
     - यदि वह ऐप या सिस्टम एक्शन फ़ोन में उपलब्ध है, तो मुन्ना तुरंत उसे खोलेगा और हिंदी में पुष्टि बोलेगा; यदि इंस्टॉल नहीं है, तो साफ और सच्ची जानकारी देगा।
  3. **हिंदी APK, वॉइस और बैटरी ट्रबलशूटिंग गाइड**:
     - एक सरल, स्टेप-बाय-स्टेप कार्ड शीट जिसमें:
       - AI Studio से `.apk` डाउनलोड और फ़ोन में इंस्टॉल करने की विधि।
       - यदि आवाज़ न आए तो Android TTS सेटिंग्स (`Install voice data`) सीधा खोलने का बटन।
       - फ़्लोटिंग माइक बैकग्राउंड में बंद न हो, इसके लिए Android App Info / Battery Optimization सेटिंग्स खोलने का सीधा बटन।

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: प्रीमियम डार्क नेवी और स्काई-ब्लू इंटरफ़ेस जो छोटे Android स्क्रीन्स पर भी साफ़ और हल्का दिखता है।
  - *Color Palette & Mood*:
    - `--background`: Deep Navy Blue (`#071124`)
    - `--surface`: Elevated Navy Card (`#102240` / `#172F54`)
    - `--primary`: Sky Blue (`#38BDF8`)
    - `--accent`: Soft Cyan Glow (`#22D3EE`)
    - `--success` / `--warning`: Emerald Green (`#34D399`) और Amber (`#FBBF24`)
  - *Typography & Hierarchy*: हिंदी देवनागरी के लिए `Noto Sans Devanagari` और अंग्रेज़ी/हिंग्लिश लेबल्स के लिए `Plus Jakarta Sans`।
  - *Interactive Feedback & Motion*: हर परमिशन ग्रांट होने पर तुरंत हरा चेकमार्क अपडेट होगा और ऐप वापस आने पर (`ON_RESUME`) स्थिति अपने-आप रिफ्रेश होगी।

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: सुरक्षित पैकेज विज़िबिलिटी और सिस्टम इंटेंट्स के साथ कस्टम ऐप लॉन्चिंग**
  - *Chosen Approach*: Google Play की गोपनीयता नीतियों का पालन करते हुए बिना संवेदनशील `QUERY_ALL_PACKAGES` परमिशन माँगे, प्रमुख ऐप्स के पैकेज नाम और मानक Android System Intents (`ACTION_IMAGE_CAPTURE`, `ACTION_DIAL`, `Settings.ACTION_SETTINGS`, `AlarmClock`, `CATEGORY_APP_CALCULATOR`, `CATEGORY_APP_MAPS` आदि) का संयोजन।
  - *Why*: इससे सुरक्षा और गोपनीयता भी बनी रहती है और यूज़र के फ़ोन के सभी ज़रूरी ऐप्स बिना किसी रुकावट के खुल जाते हैं।
- **Decision 2: वन-टैप सिस्टम सेटिंग्स शॉर्टकट्स (Direct Android Settings Intents)**
  - *Chosen Approach*: ट्रबलशूटिंग गाइड में केवल टेक्स्ट लिखने के बजाय सीधे Android के संबंधित सेटिंग्स पेज (`ACTION_TTS_SETTINGS`, `ACTION_APPLICATION_DETAILS_SETTINGS`, `ACTION_MANAGE_OVERLAY_PERMISSION`) खोलने वाले बटन देना।
  - *Why*: एक शुरुआती यूज़र को फ़ोन की सेटिंग्स के अंदर भटकना नहीं पड़ता।

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        Munna AI — UI Layer (Compose)                     │
│                                                                          │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌────────────────┐  │
│  │     Home Screen      │  │  Phone Setup & Help  │  │ Settings &     │  │
│  │ • Central Mic        │  │  • 1-Tap Permissions │  │ Troubleshooting│  │
│  │ • App Shortcuts      │  │  • APK Install Steps │  │ • TTS & Battery│  │
│  │ • Custom App Trigger │  │  • Voice/Battery Fix │  │   Shortcuts    │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └───────┬────────┘  │
└─────────────┼─────────────────────────┼──────────────────────┼───────────┘
              │                         │                      │
              ▼                         ▼                      ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                     MunnaViewModel & Domain Layer                        │
│                                                                          │
│  ┌─────────────────────────────┐      ┌───────────────────────────────┐  │
│  │     MunnaCommandParser      │      │       AppActionHandler        │  │
│  │ • Hindi/Hinglish Normalizer │─────▶│ • Package Launch Intents      │  │
│  │ • 12+ App & System Intents  │      │ • System Action Fallbacks     │  │
│  │ • Custom App Name Extractor │      │ • Truthful Launch Verifier    │  │
│  └─────────────────────────────┘      └───────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────┘
```

- **Interactive Component & State Mapping**:
  - **One-Stop Permission Hub**: `permissionsSnapshot` (`RECORD_AUDIO`, `canDrawOverlays`, `isNotificationAccessGranted`, `ttsState.isReady`) को रीयल-टाइम में ट्रैक करता है और अधूरे स्टेप्स की संख्या दिखाता है।
  - **Extended App & System Action Matcher**: जब यूज़र *"Camera kholo"*, *"Calculator kholo"*, *"Settings kholo"*, *"Call/Dialer kholo"*, या *"Maps kholo"* बोलता या लिखता है, तो पार्सर उसे पहचानकर संबंधित Android Intent चलाता है और `CommandHistory` में परिणाम दर्ज करता है।
  - **Troubleshooting Actions**: TTS आवाज़ सेटिंग्स, ऐप बैटरी सेटिंग्स, और माइक अनुमतियों को एक क्लिक में खोलता है और किसी भी डिवाइस पर क्रैश से बचने के लिए सुरक्षित `try/catch` हैंडलिंग रखता है।
