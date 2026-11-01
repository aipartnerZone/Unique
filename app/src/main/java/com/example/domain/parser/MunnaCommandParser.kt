package com.example.domain.parser

enum class TargetApp(
    val id: String,
    val displayName: String,
    val hindiName: String,
    val primaryPackages: List<String>,
    val sampleCommand: String
) {
    YOUTUBE(
        id = "youtube",
        displayName = "YouTube",
        hindiName = "YouTube (यूट्यूब)",
        primaryPackages = listOf("com.google.android.youtube"),
        sampleCommand = "Munna, YouTube kholo"
    ),
    WHATSAPP(
        id = "whatsapp",
        displayName = "WhatsApp",
        hindiName = "WhatsApp (व्हाट्सएप)",
        primaryPackages = listOf("com.whatsapp", "com.whatsapp.w4b"),
        sampleCommand = "WhatsApp kholo"
    ),
    CHROME(
        id = "chrome",
        displayName = "Chrome",
        hindiName = "Chrome (क्रोम ब्राउज़र)",
        primaryPackages = listOf("com.android.chrome"),
        sampleCommand = "Chrome open karo"
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
        sampleCommand = "Meri files kholo"
    )
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

    private val youtubeAliases = listOf(
        "youtube", "you tube", "utube", "yt", "yutube", "यूट्यूब", "यू ट्यूब", "युटुब"
    )

    private val whatsappAliases = listOf(
        "whatsapp", "whats app", "watsapp", "whatsap", "wa",
        "व्हाट्सएप", "व्हाट्सऐप", "वाट्सएप", "वॉट्सऐप", "व्हाट्स अप"
    )

    private val chromeAliases = listOf(
        "chrome", "google chrome", "crome", "krome", "browser", "internet browser",
        "क्रोम", "गूगल क्रोम", "ब्राउज़र", "ब्राउजर"
    )

    private val filesAliases = listOf(
        "meri files", "my files", "files", "file manager", "file", "folder", "storage", "documents",
        "मेरी फाइलें", "मेरी फाइल", "फाइलें", "फाइल", "फ़ाइलें", "फ़ाइल", "फाइल मैनेजर", "फ़ोल्डर"
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

        // Strip leading wake words iteratively
        var changed = true
        while (changed) {
            changed = false
            for (prefix in wakePrefixes) {
                if (cleaned == prefix) {
                    return cleaned // User only said "Munna" or "Hey Munna"
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

        // 1. Check App Launching commands (with or without explicit action verb if app name is clearly spoken)
        val hasOpenVerb = openActionVerbs.any { normalized.contains(it) }

        if (youtubeAliases.any { normalized.contains(it) } && (hasOpenVerb || youtubeAliases.any { normalized == it })) {
            return ParsedCommand.OpenApp(raw, normalized, TargetApp.YOUTUBE)
        }
        if (whatsappAliases.any { normalized.contains(it) } &&
            !notificationPhrases.any { normalized.contains(it) } &&
            (hasOpenVerb || whatsappAliases.any { normalized == it })
        ) {
            return ParsedCommand.OpenApp(raw, normalized, TargetApp.WHATSAPP)
        }
        if (chromeAliases.any { normalized.contains(it) } && (hasOpenVerb || chromeAliases.any { normalized == it })) {
            return ParsedCommand.OpenApp(raw, normalized, TargetApp.CHROME)
        }
        if (filesAliases.any { normalized.contains(it) } && (hasOpenVerb || filesAliases.any { normalized == it })) {
            return ParsedCommand.OpenApp(raw, normalized, TargetApp.FILES)
        }

        // 2. Check Time query
        if (timePhrases.any { normalized.contains(it) } || normalized == "time" || normalized == "टाइम" || normalized == "समय") {
            return ParsedCommand.TellTime(raw, normalized)
        }

        // 3. Check Date/Day query
        if (datePhrases.any { normalized.contains(it) } || normalized == "date" || normalized == "तारीख") {
            return ParsedCommand.TellDate(raw, normalized)
        }

        // 4. Check Notification reading
        if (notificationPhrases.any { normalized.contains(it) }) {
            return ParsedCommand.ReadNotifications(raw, normalized)
        }

        // 5. Check Greeting / "Munna, meri baat suno" / sole wake word
        if (wakePrefixes.contains(normalized) || greetingAndListenPhrases.any { normalized == it || normalized.contains(it) }) {
            // Make sure it's not a longer complex question that happens to contain "suno"
            if (normalized.split(" ").size <= 6) {
                return ParsedCommand.GreetingOrListen(raw, normalized)
            }
        }

        // 6. Check if user asked to open an unknown app (e.g., "Instagram kholo", "Calculator kholo")
        // Route general questions or unknown commands intelligently
        return ParsedCommand.AskAiQuestion(
            rawText = raw,
            normalizedText = normalized,
            question = raw
        )
    }
}
