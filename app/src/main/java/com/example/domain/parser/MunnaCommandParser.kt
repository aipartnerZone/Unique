package com.example.domain.parser

import com.example.data.local.MunnaAgentMode

enum class TargetApp(
    val id: String,
    val displayName: String,
    val hindiName: String,
    val primaryPackages: List<String>,
    val aliases: List<String>,
    val sampleCommand: String,
    val isHomePrimaryShortcut: Boolean = false
) {
    YOUTUBE(
        id = "youtube",
        displayName = "YouTube",
        hindiName = "YouTube (यूट्यूब)",
        primaryPackages = listOf("com.google.android.youtube"),
        aliases = listOf("youtube", "you tube", "utube", "yt", "yutube", "यूट्यूब", "यू ट्यूब", "युटुब"),
        sampleCommand = "Munna, YouTube kholo",
        isHomePrimaryShortcut = true
    ),
    WHATSAPP(
        id = "whatsapp",
        displayName = "WhatsApp",
        hindiName = "WhatsApp (व्हाट्सएप)",
        primaryPackages = listOf("com.whatsapp", "com.whatsapp.w4b"),
        aliases = listOf("whatsapp", "whats app", "watsapp", "whatsap", "wa", "व्हाट्सएप", "व्हाट्सऐप", "वाट्सएप", "वॉट्सऐप", "व्हाट्स अप"),
        sampleCommand = "WhatsApp kholo",
        isHomePrimaryShortcut = true
    ),
    CHROME(
        id = "chrome",
        displayName = "Chrome",
        hindiName = "Chrome (क्रोम ब्राउज़र)",
        primaryPackages = listOf("com.android.chrome"),
        aliases = listOf("chrome", "google chrome", "crome", "krome", "browser", "internet browser", "क्रोम", "गूगल क्रोम", "ब्राउज़र", "ब्राउजर"),
        sampleCommand = "Chrome open karo",
        isHomePrimaryShortcut = true
    ),
    FILES(
        id = "files",
        displayName = "Files",
        hindiName = "Files (फ़ाइल मैनेजर)",
        primaryPackages = listOf(
            "com.google.android.apps.nbu.files",
            "com.google.android.documentsui",
            "com.android.documentsui"
        ),
        aliases = listOf("meri files", "my files", "files", "file manager", "file", "folder", "storage", "documents", "मेरी फाइलें", "मेरी फाइल", "फाइलें", "फाइल", "फ़ाइलें", "फ़ाइल", "फाइल मैनेजर", "फ़ोल्डर"),
        sampleCommand = "Meri files kholo",
        isHomePrimaryShortcut = true
    ),
    CAMERA(
        id = "camera",
        displayName = "Camera",
        hindiName = "Camera (कैमरा)",
        primaryPackages = listOf("com.android.camera", "com.android.camera2", "com.google.android.GoogleCamera"),
        aliases = listOf("camera", "camra", "photo khicho", "selfie", "कैमरा", "फोटो खींचो", "सेल्फी"),
        sampleCommand = "Munna, Camera kholo"
    ),
    SETTINGS(
        id = "settings",
        displayName = "Settings",
        hindiName = "Settings (फ़ोन सेटिंग्स)",
        primaryPackages = listOf("com.android.settings"),
        aliases = listOf("phone settings", "system settings", "setting", "settings", "सेटिंग्स", "सेटिंग", "फ़ोन सेटिंग्स"),
        sampleCommand = "Phone settings kholo"
    ),
    DIALER(
        id = "dialer",
        displayName = "Phone Dialer",
        hindiName = "Phone Dialer (कॉल डायलर)",
        primaryPackages = listOf("com.google.android.dialer", "com.android.dialer"),
        aliases = listOf("dialer", "phone dialer", "call log", "keypad", "डायलर", "कॉल डायलर", "नंबर डायल"),
        sampleCommand = "Phone dialer kholo"
    ),
    CALCULATOR(
        id = "calculator",
        displayName = "Calculator",
        hindiName = "Calculator (कैलकुलेटर)",
        primaryPackages = listOf("com.google.android.calculator", "com.android.calculator2"),
        aliases = listOf("calculator", "calc", "hisab", "कैलकुलेटर", "हिसाब"),
        sampleCommand = "Calculator kholo"
    ),
    MAPS(
        id = "maps",
        displayName = "Google Maps",
        hindiName = "Google Maps (मैप्स)",
        primaryPackages = listOf("com.google.android.apps.maps"),
        aliases = listOf("google maps", "maps", "map", "navigation", "rasta", "गूगल मैप्स", "मैप्स", "मैप", "रास्ता"),
        sampleCommand = "Google Maps kholo"
    ),
    GALLERY(
        id = "gallery",
        displayName = "Photos / Gallery",
        hindiName = "Gallery (गैलरी / फ़ोटो)",
        primaryPackages = listOf("com.google.android.apps.photos", "com.android.gallery3d"),
        aliases = listOf("gallery", "photos", "google photos", "meri photo", "गैलरी", "फोटो", "फ़ोटो", "गूगल फोटोज"),
        sampleCommand = "Gallery kholo"
    ),
    CLOCK(
        id = "clock",
        displayName = "Clock / Alarm",
        hindiName = "Clock (अलार्म / घड़ी)",
        primaryPackages = listOf("com.google.android.deskclock", "com.android.deskclock"),
        aliases = listOf("alarm", "clock", "timer", "ghadi", "अलार्म", "घड़ी", "टाइमर"),
        sampleCommand = "Alarm kholo"
    ),
    PLAY_STORE(
        id = "play_store",
        displayName = "Play Store",
        hindiName = "Google Play Store (प्ले स्टोर)",
        primaryPackages = listOf("com.android.vending"),
        aliases = listOf("play store", "playstore", "google play", "प्ले स्टोर", "गूगल प्ले"),
        sampleCommand = "Play Store kholo"
    ),
    GMAIL(
        id = "gmail",
        displayName = "Gmail",
        hindiName = "Gmail (जीमेल / ईमेल)",
        primaryPackages = listOf("com.google.android.gm"),
        aliases = listOf("gmail", "email", "mail", "जीमेल", "ईमेल"),
        sampleCommand = "Gmail kholo"
    ),
    INSTAGRAM(
        id = "instagram",
        displayName = "Instagram",
        hindiName = "Instagram (इंस्टाग्राम)",
        primaryPackages = listOf("com.instagram.android"),
        aliases = listOf("instagram", "insta", "इंस्टाग्राम", "इंस्टा"),
        sampleCommand = "Instagram kholo"
    ),
    TELEGRAM(
        id = "telegram",
        displayName = "Telegram",
        hindiName = "Telegram (टेलीग्राम)",
        primaryPackages = listOf("com.telegram.messenger", "org.telegram.messenger"),
        aliases = listOf("telegram", "टेलीग्राम"),
        sampleCommand = "Telegram kholo"
    );

    companion object {
        val homeShortcuts: List<TargetApp> = entries.filter { it.isHomePrimaryShortcut }
        val extraUtilityApps: List<TargetApp> = entries.filter { !it.isHomePrimaryShortcut }
    }
}

/**
 * Security Guard that blocks opening ANY Payment, UPI, Wallet, or Banking application.
 * Fulfils user requirement: "Hamara app sabhi app bolkar kholne me saksham hoga, kevel payment app nhi kholega"
 */
object PaymentAppSecurityGuard {

    val blockedPackageNames = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay (GPay)
        "com.phonepe.app",                        // PhonePe
        "com.phonepe.app.business",               // PhonePe Business
        "net.one97.paytm",                        // Paytm
        "com.paytm.business",                     // Paytm for Business
        "in.org.npci.upiapp",                     // BHIM UPI
        "com.dreamplug.androidapp",               // CRED
        "com.mobikwik_new",                       // MobiKwik
        "com.freecharge.android",                 // Freecharge
        "com.bharatpe.app",                       // BharatPe
        "com.paypal.android.p2pmobile",           // PayPal
        "in.amazon.mShop.android.shopping.pay",   // Amazon Pay standalone
        "com.sbi.lotusintouch",                   // YONO SBI
        "com.sbi.SBIFreedomPlus",                 // YONO Lite SBI
        "com.csam.icici.bank.imobile",            // iMobile Pay ICICI
        "com.snapwork.hdfc",                      // HDFC Bank
        "com.axis.mobile",                        // Axis Mobile
        "com.msf.kbank.mobile",                   // Kotak811
        "com.infrasofttech.CentralBank",          // Central Bank
        "com.barodampay",                         // BoB World
        "com.finopaytech.bpayfino",               // FinoPay
        "com.myairtelapp.bank",                   // Airtel Payments Bank
        "com.jio.myjio.bank",                     // JioFinance
        "com.sliceit",                            // slice
        "com.naviapp",                            // Navi UPI
        "money.super.app",                        // super.money
        "in.gokiwi.kiwitpap"                      // Kiwi UPI
    )

    private val blockedAppKeywords = listOf(
        "google pay", "gpay", "g pay", "tez",
        "phonepe", "phone pe", "fonpe",
        "paytm", "pay tm",
        "bhim", "bhim upi", "upi app",
        "cred", "mobikwik", "freecharge", "bharatpe", "bharat pe",
        "amazon pay", "paypal", "slice", "super money",
        "yono", "yono sbi", "imobile", "netbanking", "net banking",
        "hdfc bank", "icici bank", "sbi bank", "axis bank", "kotak bank", "pnb one", "bob world",
        "payment app", "banking app",
        "गूगल पे", "जीपे", "फोनपे", "फ़ोनपे", "फोन पे", "पेटीएम", "भीम", "यूपीआई",
        "क्रेड", "मोबिक्विक", "भारतपे", "नेटबैंकिंग", "बैंक ऐप", "पेमेंट ऐप", "योनो"
    )

    /**
     * Checks if a spoken app name or package name belongs to a Payment/UPI/Banking app.
     */
    fun isPaymentOrBankingApp(appNameOrQuery: String, packageName: String = ""): Boolean {
        val cleanPkg = packageName.trim().lowercase()
        if (cleanPkg.isNotEmpty()) {
            if (blockedPackageNames.contains(cleanPkg)) return true
            if (cleanPkg.contains("upi") ||
                cleanPkg.contains("paisa") ||
                cleanPkg.contains("paytm") ||
                cleanPkg.contains("phonepe") ||
                cleanPkg.contains("netbanking") ||
                cleanPkg.contains("bank")
            ) {
                return true
            }
        }

        val cleanName = appNameOrQuery.trim().lowercase()
        if (cleanName.isEmpty()) return false

        return blockedAppKeywords.any { keyword ->
            cleanName == keyword ||
                cleanName.startsWith("$keyword ") ||
                cleanName.endsWith(" $keyword") ||
                cleanName.contains(" $keyword ") ||
                (keyword.length >= 5 && cleanName.contains(keyword))
        }
    }

    /**
     * Extracts the matched payment app name if the query is attempting to launch a payment app.
     */
    fun detectBlockedPaymentAppName(normalizedQuery: String): String? {
        for (keyword in blockedAppKeywords.sortedByDescending { it.length }) {
            if (normalizedQuery == keyword ||
                normalizedQuery.startsWith("$keyword ") ||
                normalizedQuery.endsWith(" $keyword") ||
                normalizedQuery.contains(" $keyword ")
            ) {
                return keyword.uppercase()
            }
        }
        return null
    }
}

sealed interface ParsedCommand {
    val rawText: String
    val normalizedText: String
    val categoryLabel: String

    data class OpenApp(
        override val rawText: String,
        override val normalizedText: String,
        val targetApp: TargetApp
    ) : ParsedCommand {
        override val categoryLabel: String = "ऐप खोलें (${targetApp.displayName})"
    }

    data class OpenCustomAppByName(
        override val rawText: String,
        override val normalizedText: String,
        val requestedAppName: String
    ) : ParsedCommand {
        override val categoryLabel: String = "ऐप खोलें ($requestedAppName)"
    }

    data class BlockedPaymentApp(
        override val rawText: String,
        override val normalizedText: String,
        val blockedAppName: String
    ) : ParsedCommand {
        override val categoryLabel: String = "🔒 पेमेंट सुरक्षा लॉक ($blockedAppName)"
    }

    data class MakePhoneCall(
        override val rawText: String,
        override val normalizedText: String,
        val targetNameOrNumber: String,
        val extractedDigits: String
    ) : ParsedCommand {
        override val categoryLabel: String = "📞 वॉइस कॉल ($targetNameOrNumber)"
    }

    data class OpenPdfReader(
        override val rawText: String,
        override val normalizedText: String
    ) : ParsedCommand {
        override val categoryLabel: String = "📄 PDF रीडर और पढ़ाई"
    }

    data class SwitchAgentMode(
        override val rawText: String,
        override val normalizedText: String,
        val targetMode: MunnaAgentMode,
        val initialTopic: String = ""
    ) : ParsedCommand {
        override val categoryLabel: String = "${targetMode.badgeEmoji} ${targetMode.titleHindi}"
    }

    data class TellTime(
        override val rawText: String,
        override val normalizedText: String
    ) : ParsedCommand {
        override val categoryLabel: String = "समय जानकारी (Time)"
    }

    data class TellDate(
        override val rawText: String,
        override val normalizedText: String
    ) : ParsedCommand {
        override val categoryLabel: String = "तारीख जानकारी (Date)"
    }

    data class GreetingOrListen(
        override val rawText: String,
        override val normalizedText: String
    ) : ParsedCommand {
        override val categoryLabel: String = "बातचीत / अभिवादन (Greeting)"
    }

    data class ReadNotifications(
        override val rawText: String,
        override val normalizedText: String
    ) : ParsedCommand {
        override val categoryLabel: String = "नोटिफिकेशन रीडर (Notifications)"
    }

    data class AskAiQuestion(
        override val rawText: String,
        override val normalizedText: String,
        val question: String
    ) : ParsedCommand {
        override val categoryLabel: String = "AI एजेंट / पढ़ाई / डिस्कशन"
    }

    data class EmptyOrUnclear(
        override val rawText: String,
        override val normalizedText: String
    ) : ParsedCommand {
        override val categoryLabel: String = "अस्पष्ट कमांड (Unclear)"
    }
}

data class ExtractedMemoryFact(
    val category: String,
    val memoryKey: String,
    val memoryValue: String,
    val contactPhone: String = ""
)

object MunnaCommandParser {

    private val wakePrefixes = listOf(
        "hey munna", "hi munna", "hello munna", "ok munna", "o munna",
        "munna bhai", "munna ji", "munna", "अरे मुन्ना", "हे मुन्ना",
        "हेलो मुन्ना", "नमस्ते मुन्ना", "मुन्ना भाई", "मुन्ना जी", "मुन्ना",
        "bhai", "भाई", "yaar", "यार", "please", "plz", "जरा", "ज़रा", "कृपया"
    )

    private val openActionVerbs = listOf(
        "kholo", "khol", "kholna", "kholiye", "khol de", "khol do", "kholo na",
        "open karo", "open kar", "open kar do", "open", "start karo", "start",
        "chalao", "chala do", "chalu karo", "chalu kar", "launch",
        "खोलो", "खोल", "खोलिए", "खोल दो", "खोल दे", "चलाओ", "चला दो", "चालू करो", "ओपन करो", "ओपन"
    )

    private val callActionPhrases = listOf(
        "call karo", "call lagao", "call kar do", "call kar", "phone lagao", "phone karo",
        "number lagao", "dial karo", "ko call", "par call", "कॉल करो", "कॉल लगाओ",
        "कॉल कर दो", "फ़ोन लगाओ", "फोन लगाओ", "फ़ोन करो", "फोन करो", "नंबर लगाओ"
    )

    private val pdfPhrases = listOf(
        "pdf padho", "pdf kholo", "pdf sunao", "pdf samjhao", "read pdf", "open pdf",
        "मेरा पीडीएफ", "पीडीएफ पढ़ो", "पीडीएफ खोलो", "पीडीएफ सुनाओ", "पीडीएफ समझाओ", "pdf रीडर"
    )

    private val discussionModePhrases = listOf(
        "mujhse discuss karo", "mujhe discuss karo", "mere sath discuss karo",
        "chalo discuss karte hain", "discuss karo", "discussion karo", "discussion mode",
        "charcha karo", "baat cheet karte hain",
        "मुझसे डिस्कस करो", "मुझे डिस्कस करो", "डिस्कस करो", "चलो डिस्कस करते हैं",
        "चर्चा करो", "मेरे साथ चर्चा करो", "डिस्कशन मोड"
    )

    private val studentStudyPhrases = listOf(
        "mere sath padhai karo", "mujhe padhao", "mujhe sikhao", "padhai shuru karo",
        "student mode", "study mode", "padhai karo", "exam ki taiyari", "mujhe samjhao",
        "मेरे साथ पढ़ाई करो", "मुझे पढ़ाओ", "मुझे सिखाओ", "पढ़ाई शुरू करो",
        "स्टूडेंट मोड", "पढ़ाई मोड", "परीक्षा की तैयारी"
    )

    private val timePhrases = listOf(
        "kitne baje", "kitna baja", "time kya", "kya time", "samay kya", "abhi kitne baje",
        "waqt kya", "what time", "current time", "tell me the time", "time batao", "time बताओ",
        "कितने बजे", "कितना बजा", "समय क्या", "टाइम क्या", "अभी कितने बजे", "टाइम बताओ", "समय बताओ"
    )

    private val datePhrases = listOf(
        "aaj kya tarikh", "kya tarikh", "tarikh kya", "aaj konsa din", "aaj kaun sa din",
        "date kya", "what date", "today's date", "day today", "date batao",
        "आज क्या तारीख", "तारीख क्या", "आज कौन सा दिन", "आज का दिन", "डेट क्या है"
    )

    private val greetingAndListenPhrases = listOf(
        "meri baat suno", "baat suno", "suno na", "sun rahe ho", "suno",
        "namaste", "namaskar", "ram ram", "hello", "hi", "hey",
        "kaise ho", "kya haal", "kon ho", "kaun ho", "tum kaun ho", "aap kaun ho",
        "apna parichay", "kya kar sakte ho", "help", "madad karo",
        "मेरी बात सुनो", "बात सुनो", "सुनो", "नमस्ते", "नमस्कार", "राम राम",
        "कैसे हो", "क्या हाल", "तुम कौन हो", "आप कौन हो", "क्या कर सकते हो", "मदद करो"
    )

    private val notificationPhrases = listOf(
        "notification padho", "notifications padho", "message padho", "messages padho",
        "whatsapp message padho", "kya message aaya", "koi notification", "notification sunao",
        "नोटिफिकेशन पढ़ो", "मैसेज पढ़ो", "संदेश पढ़ो", "कोई मैसेज आया", "नोटिफिकेशन सुनाओ"
    )

    fun normalize(raw: String): String {
        var cleaned = raw.trim().lowercase()
        cleaned = cleaned.replace(Regex("[।.,!?\"'`~:;\\-_()/\\\\]+"), " ")
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

        var changed = true
        while (changed) {
            changed = false
            for (prefix in wakePrefixes) {
                if (cleaned == prefix) {
                    return cleaned
                }
                if (cleaned.startsWith("$prefix ")) {
                    cleaned = cleaned.removePrefix("$prefix ").trim()
                    changed = true
                    break
                }
            }
        }
        return cleaned
    }

    /**
     * Automatically extracts personal facts, study details, or contact numbers from user statements
     * so Munna AI continuously learns from the user's data ("ye hamre data se sikhega").
     */
    fun extractLearnedMemoryIfAny(rawInput: String): ExtractedMemoryFact? {
        val text = rawInput.trim()
        val lower = text.lowercase()

        // 1. User Name ("Mera naam Rahul hai", "मेरा नाम अमन है", "My name is Priya")
        val nameRegexes = listOf(
            Regex("(?:mera naam|my name is|मेरा नाम)\\s+([a-zA-Z\\u0900-\\u097F]+)(?:\\s+hai|\\s+है)?", RegexOption.IGNORE_CASE),
            Regex("(?:main|मैं)\\s+([a-zA-Z\\u0900-\\u097F]+)\\s+(?:hoon|hu|हूँ|हूं)", RegexOption.IGNORE_CASE)
        )
        for (regex in nameRegexes) {
            val match = regex.find(text)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                val ignoredWords = setOf("student", "छात्र", "लड़का", "लड़की", "दोस्त", "तैयार", "परेशान", "खुश")
                if (candidate.length in 2..20 && !ignoredWords.contains(candidate.lowercase())) {
                    return ExtractedMemoryFact(
                        category = "प्रोफ़ाइल (Profile)",
                        memoryKey = "user_name",
                        memoryValue = "यूज़र का नाम: $candidate"
                    )
                }
            }
        }

        // 2. Contact phone number ("Papa ka number 9876543210 hai", "पापा का नंबर 9876543210")
        val contactRegex = Regex("([a-zA-Z\\u0900-\\u097F]+)\\s+(?:ka number|का नंबर|number)\\s*[:\\-]?\\s*(\\+?\\d[\\d\\s\\-]{7,14})", RegexOption.IGNORE_CASE)
        val contactMatch = contactRegex.find(text)
        if (contactMatch != null) {
            val person = contactMatch.groupValues[1].trim()
            val digits = contactMatch.groupValues[2].filter { it.isDigit() || it == '+' }
            if (person.isNotBlank() && digits.length >= 8) {
                return ExtractedMemoryFact(
                    category = "कॉल संपर्क (Contact)",
                    memoryKey = "contact_${person.lowercase()}",
                    memoryValue = "$person का फ़ोन नंबर: $digits",
                    contactPhone = digits
                )
            }
        }

        // 3. Student Class / Course / Subject ("Main class 12 me padhta hu", "मैं कक्षा 10 का छात्र हूँ", "Mera subject Science hai")
        if (lower.contains("class ") || lower.contains("कक्षा") || lower.contains("student") ||
            lower.contains("छात्र") || lower.contains("पढ़ता") || lower.contains("पढ़ती") ||
            lower.contains(" तैयारी ") || lower.contains("taiyari") || lower.contains("subject")
        ) {
            if (text.length in 8..110 && !lower.endsWith("?")) {
                return ExtractedMemoryFact(
                    category = "पढ़ाई (Study)",
                    memoryKey = "study_profile_${text.take(16).lowercase().hashCode()}",
                    memoryValue = text
                )
            }
        }

        // 4. Explicit "Yaad rakho / Remember" command ("Munna yaad rakho ...", "याद रखो ...")
        val rememberPrefixes = listOf("yaad rakho", "yaad rakhna", "remember that", "remember", "याद रखो", "याद रखना")
        for (prefix in rememberPrefixes) {
            val idx = lower.indexOf(prefix)
            if (idx >= 0) {
                val fact = text.substring(idx + prefix.length).trim().removePrefix("ki ").removePrefix("कि ").trim()
                if (fact.length >= 3) {
                    return ExtractedMemoryFact(
                        category = "याददाश्त (Memory)",
                        memoryKey = "fact_${fact.take(20).lowercase().hashCode()}",
                        memoryValue = fact
                    )
                }
            }
        }

        return null
    }

    fun parse(rawInput: String): ParsedCommand {
        val raw = rawInput.trim()
        if (raw.isBlank()) {
            return ParsedCommand.EmptyOrUnclear(rawInput, "")
        }

        val normalized = normalize(raw)
        if (normalized.isBlank()) {
            return ParsedCommand.EmptyOrUnclear(rawInput, "")
        }

        // 1. Check Notification reading first
        if (notificationPhrases.any { normalized.contains(it) }) {
            return ParsedCommand.ReadNotifications(raw, normalized)
        }

        // 2. Check PDF Reader command ("PDF padho", "PDF kholo")
        if (pdfPhrases.any { normalized.contains(it) } || normalized == "pdf" || normalized == "पीडीएफ") {
            return ParsedCommand.OpenPdfReader(raw, normalized)
        }

        // 3. Check Discussion Mode trigger ("Mujhse discuss karo", "चलो डिस्कस करते हैं")
        if (discussionModePhrases.any { normalized.contains(it) }) {
            var topic = normalized
            for (phrase in discussionModePhrases.sortedByDescending { it.length }) {
                topic = topic.replace(phrase, "").trim()
            }
            topic = topic.removePrefix("par").removeSuffix("par").removePrefix("पर").removeSuffix("पर").trim()
            return ParsedCommand.SwitchAgentMode(
                rawText = raw,
                normalizedText = normalized,
                targetMode = MunnaAgentMode.DISCUSSION_PARTNER,
                initialTopic = topic
            )
        }

        // 4. Check Student Study / Tutor Mode trigger ("Mere sath padhai karo", "मुझे पढ़ाओ")
        if (studentStudyPhrases.any { normalized.contains(it) }) {
            var topic = normalized
            for (phrase in studentStudyPhrases.sortedByDescending { it.length }) {
                topic = topic.replace(phrase, "").trim()
            }
            return ParsedCommand.SwitchAgentMode(
                rawText = raw,
                normalizedText = normalized,
                targetMode = MunnaAgentMode.STUDENT_TUTOR,
                initialTopic = topic
            )
        }

        // 5. Check Voice Call command ("Papa ko call karo", "9876543210 par call lagao", "Call Rahul")
        val hasCallPhrase = callActionPhrases.any { normalized.contains(it) } ||
            normalized.startsWith("call ") ||
            normalized.startsWith("कॉल ")
        if (hasCallPhrase && !normalized.contains("dialer kholo")) {
            val digits = normalized.filter { it.isDigit() || it == '+' }
            var target = normalized
            for (phrase in callActionPhrases.sortedByDescending { it.length }) {
                target = target.replace(phrase, " ").trim()
            }
            target = target
                .removePrefix("call ")
                .removePrefix("कॉल ")
                .removeSuffix(" ko")
                .removeSuffix(" को")
                .removeSuffix(" par")
                .removeSuffix(" पर")
                .removeSuffix(" se")
                .trim()
            val finalTarget = target.ifBlank { digits.ifBlank { "डायलर" } }
            return ParsedCommand.MakePhoneCall(
                rawText = raw,
                normalizedText = normalized,
                targetNameOrNumber = finalTarget,
                extractedDigits = digits
            )
        }

        // 6. SECURITY CHECK: Block ANY Payment / UPI / Banking App BEFORE opening any app!
        val hasOpenVerb = openActionVerbs.any { normalized.contains(it) }
        val detectedPaymentApp = PaymentAppSecurityGuard.detectBlockedPaymentAppName(normalized)
        if (detectedPaymentApp != null && (hasOpenVerb || normalized.split(" ").size <= 4)) {
            return ParsedCommand.BlockedPaymentApp(
                rawText = raw,
                normalizedText = normalized,
                blockedAppName = detectedPaymentApp
            )
        }

        // 7. Check all known TargetApps (Primary shortcuts + Extended phone apps)
        for (targetApp in TargetApp.entries) {
            val matchesAlias = targetApp.aliases.any { alias ->
                normalized == alias ||
                    normalized.startsWith("$alias ") ||
                    normalized.endsWith(" $alias") ||
                    normalized.contains(" $alias ")
            }
            if (matchesAlias && (hasOpenVerb || targetApp.aliases.any { normalized == it })) {
                return ParsedCommand.OpenApp(raw, normalized, targetApp)
            }
        }

        // 8. Check Time query
        if (timePhrases.any { normalized.contains(it) } || normalized == "time" || normalized == "टाइम" || normalized == "समय") {
            return ParsedCommand.TellTime(raw, normalized)
        }

        // 9. Check Date/Day query
        if (datePhrases.any { normalized.contains(it) } || normalized == "date" || normalized == "तारीख") {
            return ParsedCommand.TellDate(raw, normalized)
        }

        // 10. Check Greeting / "Munna, meri baat suno"
        if (wakePrefixes.contains(normalized) || greetingAndListenPhrases.any { normalized == it || normalized.contains(it) }) {
            if (normalized.split(" ").size <= 6) {
                return ParsedCommand.GreetingOrListen(raw, normalized)
            }
        }

        // 11. Check if user asked to open ANY custom installed app by name (e.g. "Spotify kholo", "Truecaller open karo")
        if (hasOpenVerb && normalized.split(" ").size <= 6) {
            var extractedName = normalized
            for (verb in openActionVerbs.sortedByDescending { it.length }) {
                extractedName = extractedName
                    .removePrefix("$verb ")
                    .removeSuffix(" $verb")
                    .replace(" $verb ", " ")
                    .trim()
            }
            extractedName = extractedName
                .removeSuffix("app")
                .removeSuffix("ऐप")
                .removeSuffix("को")
                .removeSuffix("ko")
                .trim()

            if (extractedName.isNotBlank() && extractedName.length >= 2) {
                // Double check Payment App Security Guard on extracted name
                if (PaymentAppSecurityGuard.isPaymentOrBankingApp(extractedName)) {
                    return ParsedCommand.BlockedPaymentApp(
                        rawText = raw,
                        normalizedText = normalized,
                        blockedAppName = extractedName.uppercase()
                    )
                }
                return ParsedCommand.OpenCustomAppByName(
                    rawText = raw,
                    normalizedText = normalized,
                    requestedAppName = extractedName
                )
            }
        }

        // 12. Route general questions, study queries, or discussions to Gemini AI Agent Handler
        return ParsedCommand.AskAiQuestion(
            rawText = raw,
            normalizedText = normalized,
            question = raw
        )
    }
}
