package com.example.domain.parser

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
        override val categoryLabel: String = "कस्टम ऐप खोलें ($requestedAppName)"
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
        override val categoryLabel: String = "AI सवाल-जवाब (Gemini AI)"
    }

    data class EmptyOrUnclear(
        override val rawText: String,
        override val normalizedText: String
    ) : ParsedCommand {
        override val categoryLabel: String = "अस्पष्ट कमांड (Unclear)"
    }
}

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

    fun parse(rawInput: String): ParsedCommand {
        val raw = rawInput.trim()
        if (raw.isBlank()) {
            return ParsedCommand.EmptyOrUnclear(rawInput, "")
        }

        val normalized = normalize(raw)
        if (normalized.isBlank()) {
            return ParsedCommand.EmptyOrUnclear(rawInput, "")
        }

        // 1. Check Notification reading first (so "whatsapp message padho" isn't confused with "whatsapp kholo")
        if (notificationPhrases.any { normalized.contains(it) }) {
            return ParsedCommand.ReadNotifications(raw, normalized)
        }

        // 2. Check all known TargetApps (Primary shortcuts + Extended phone apps)
        val hasOpenVerb = openActionVerbs.any { normalized.contains(it) }
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

        // 3. Check Time query
        if (timePhrases.any { normalized.contains(it) } || normalized == "time" || normalized == "टाइम" || normalized == "समय") {
            return ParsedCommand.TellTime(raw, normalized)
        }

        // 4. Check Date/Day query
        if (datePhrases.any { normalized.contains(it) } || normalized == "date" || normalized == "तारीख") {
            return ParsedCommand.TellDate(raw, normalized)
        }

        // 5. Check Greeting / "Munna, meri baat suno"
        if (wakePrefixes.contains(normalized) || greetingAndListenPhrases.any { normalized == it || normalized.contains(it) }) {
            if (normalized.split(" ").size <= 6) {
                return ParsedCommand.GreetingOrListen(raw, normalized)
            }
        }

        // 6. Check if user explicitly asked to open a custom app by name (e.g. "Spotify kholo", "open Netflix")
        if (hasOpenVerb && normalized.split(" ").size <= 4) {
            var extractedName = normalized
            for (verb in openActionVerbs.sortedByDescending { it.length }) {
                extractedName = extractedName
                    .removePrefix("$verb ")
                    .removeSuffix(" $verb")
                    .replace(" $verb ", " ")
                    .trim()
            }
            extractedName = extractedName.removeSuffix("app").removeSuffix("ऐप").trim()
            if (extractedName.isNotBlank() && extractedName.length >= 2) {
                return ParsedCommand.OpenCustomAppByName(
                    rawText = raw,
                    normalizedText = normalized,
                    requestedAppName = extractedName
                )
            }
        }

        // 7. Route general questions to Gemini AI Conversation Handler
        return ParsedCommand.AskAiQuestion(
            rawText = raw,
            normalizedText = normalized,
            question = raw
        )
    }
}
