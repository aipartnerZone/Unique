package com.example.ui

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppLanguage
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CommandHistoryEntity
import com.example.data.local.MunnaAgentMode
import com.example.data.local.MunnaDatabase
import com.example.data.local.MunnaPreferences
import com.example.data.local.MunnaRepository
import com.example.data.local.MunnaSettingsState
import com.example.data.local.UserMemoryEntity
import com.example.domain.action.AppActionHandler
import com.example.domain.action.InstalledDeviceAppInfo
import com.example.domain.parser.MunnaCommandParser
import com.example.domain.parser.ParsedCommand
import com.example.domain.parser.TargetApp
import com.example.pdf.LoadedPdfDocumentState
import com.example.pdf.MunnaPdfHelper
import com.example.pdf.SampleStudyDocument
import com.example.service.FloatingMicService
import com.example.service.MunnaNotificationListenerService
import com.example.service.ReadableNotificationItem
import com.example.speech.MicStatus
import com.example.speech.MunnaTtsManager
import com.example.speech.MunnaVoiceManager
import com.example.speech.TtsState
import com.example.speech.VoiceUiState
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

enum class MunnaTab {
    HOME,
    AGENT_STUDY,
    CHAT,
    HISTORY,
    SETTINGS
}

sealed interface AiChatGenerationState {
    data object Idle : AiChatGenerationState
    data object Loading : AiChatGenerationState
    data class Streaming(val partialText: String) : AiChatGenerationState
    data class Error(val errorMessage: String) : AiChatGenerationState
}

data class LastActionFeedback(
    val commandText: String,
    val categoryLabel: String,
    val responseText: String,
    val statusBadge: String,
    val isSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class DevicePermissionsSnapshot(
    val hasRecordAudioPermission: Boolean = false,
    val hasCallPhonePermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val appInstallStatus: Map<TargetApp, Boolean> = emptyMap(),
    val installedApps: List<InstalledDeviceAppInfo> = emptyList()
)

class MunnaViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val database = MunnaDatabase.getInstance(appContext)
    private val repository = MunnaRepository(database.munnaDao())
    val preferences = MunnaPreferences(appContext)
    private val actionHandler = AppActionHandler(appContext)
    private val ttsManager = MunnaTtsManager(appContext)
    private val voiceManager = MunnaVoiceManager(appContext) { recognizedText ->
        onVoiceInputRecognized(recognizedText)
    }

    // Lazy initialization of Gemini 3.8 Flash model via Firebase AI Logic
    private val geminiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.7f
            },
            systemInstruction = content {
                text(
                    """
                    आप 'Munna AI' (मुन्ना AI) हैं — भारतीय Android यूज़र्स के लिए एक बेहद दोस्ताना, समझदार, भरोसेमंद पर्सनल AI एजेंट, डिस्कशन पार्टनर और स्टूडेंट टीचर (Study Buddy)।
                    आपकी टैगलाइन है: "Bolo, Munna Karega!"
                    मुख्य नियम:
                    1. हमेशा सरल, आदरपूर्ण और दोस्ताना हिंदी (या यूज़र चाहे तो हिंग्लिश/English) में बात करें, जैसे एक अपना दोस्त या बड़ा भाई बात करता है ("हाँ भाई", "बिल्कुल दोस्त", "चलो समझते हैं")।
                    2. सुरक्षा नियम: आप फ़ोन के सभी सामान्य ऐप्स खोल सकते हैं, लेकिन किसी भी Payment, UPI या Banking ऐप (Google Pay, PhonePe, Paytm, BHIM आदि) को कभी नहीं खोलते।
                    3. आप यूज़र के डेटा और पुरानी बातों से सीखते हैं (Personal Memory) और बातचीत में उसका ध्यान रखते हैं।
                    4. जब यूज़र डिस्कस करने कहे ('Discussion Mode'), तो अपने विचार रखें और अंत में यूज़र से एक दिलचस्प सवाल भी पूछें ताकि चर्चा जारी रहे।
                    5. जब यूज़र स्टूडेंट हो या पढ़ाई करने कहे ('Student Tutor Mode'), तो कठिन से कठिन विषय को रोज़मर्रा के आसान भारतीय उदाहरणों से समझाएँ और समझ जांचने के लिए छोटे सवाल पूछें।
                    """.trimIndent()
                )
            }
        )
    }

    private val _currentTab = MutableStateFlow(MunnaTab.HOME)
    val currentTab: StateFlow<MunnaTab> = _currentTab.asStateFlow()

    private var voiceTargetMode: VoiceTargetMode = VoiceTargetMode.COMMAND_ASSISTANT

    val settings: StateFlow<MunnaSettingsState> = preferences.settings
    val voiceState: StateFlow<VoiceUiState> = voiceManager.voiceState
    val ttsState: StateFlow<TtsState> = ttsManager.ttsState
    val isOverlayServiceRunning: StateFlow<Boolean> = FloatingMicService.isRunning
    val recentNotifications: StateFlow<List<ReadableNotificationItem>> =
        MunnaNotificationListenerService.recentNotifications

    val commandHistory: StateFlow<List<CommandHistoryEntity>> = repository.commandHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userMemories: StateFlow<List<UserMemoryEntity>> = repository.userMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loadedPdfState = MutableStateFlow(LoadedPdfDocumentState())
    val loadedPdfState: StateFlow<LoadedPdfDocumentState> = _loadedPdfState.asStateFlow()

    private var currentSampleDoc: SampleStudyDocument? = null

    private val _lastActionFeedback = MutableStateFlow<LastActionFeedback?>(
        LastActionFeedback(
            commandText = "नमस्ते भाई! मैं मुन्ना एजेंट हूँ।",
            categoryLabel = "स्मार्ट एजेंट तैयार (Ready)",
            responseText = "बोलकर कोई भी नॉन-पेमेंट ऐप खोलें, कॉल लगाएँ, PDF पढ़ें, या मेरे साथ दोस्ताना डिस्कशन और पढ़ाई करें!",
            statusBadge = "मुन्ना एजेंट तैयार है",
            isSuccess = true
        )
    )
    val lastActionFeedback: StateFlow<LastActionFeedback?> = _lastActionFeedback.asStateFlow()

    private val _aiChatState = MutableStateFlow<AiChatGenerationState>(AiChatGenerationState.Idle)
    val aiChatState: StateFlow<AiChatGenerationState> = _aiChatState.asStateFlow()

    private val _chatDraftInput = MutableStateFlow("")
    val chatDraftInput: StateFlow<String> = _chatDraftInput.asStateFlow()

    private val _permissionsSnapshot = MutableStateFlow(readDevicePermissions())
    val permissionsSnapshot: StateFlow<DevicePermissionsSnapshot> = _permissionsSnapshot.asStateFlow()

    init {
        refreshPermissionsAndAppStatus()
        seedInitialDataIfNeeded()
        // Load default sample study note so PDF reader is immediately ready to test
        loadSampleStudyDocument(MunnaPdfHelper.sampleStudyDocuments.first(), speakIntro = false)
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch {
            val existingMessages = repository.chatMessages.first()
            if (existingMessages.isEmpty()) {
                repository.addChatMessage(
                    sender = "MUNNA",
                    messageText = "नमस्ते भाई! 👋 मैं मुन्ना AI हूँ — आपका पर्सनल एजेंट, डिस्कशन पार्टनर और पढ़ाई का साथी।\n\n• बोलकर कोई भी ऐप खोलें (🔒 Payment Apps सुरक्षित रूप से ब्लॉक हैं)\n• बोलकर कॉल लगाएँ (\"9876543210 पर कॉल करो\")\n• PDF फ़ाइल अपलोड करके मुझसे पढ़वाएँ और समझें\n• बोलें: \"मुझसे डिस्कस करो\" या \"मेरे साथ पढ़ाई करो\"!"
                )
            }
            val existingMemories = repository.getMemoriesSnapshot()
            if (existingMemories.isEmpty()) {
                repository.saveLearnedMemory(
                    category = "सुरक्षा नियम (Security)",
                    memoryKey = "payment_lock_rule",
                    memoryValue = "पेमेंट और बैंकिंग ऐप्स (GPay, PhonePe, Paytm, BHIM) हमेशा लॉक हैं"
                )
                repository.saveLearnedMemory(
                    category = "कॉल संपर्क (Contact)",
                    memoryKey = "contact_emergency",
                    memoryValue = "आपातकालीन सहायता हेल्पलाइन: 112",
                    contactPhone = "112"
                )
            }
        }
    }

    fun refreshPermissionsAndAppStatus() {
        _permissionsSnapshot.value = readDevicePermissions()
    }

    private fun readDevicePermissions(): DevicePermissionsSnapshot {
        val hasMic = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val hasCall = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val hasOverlay = Settings.canDrawOverlays(appContext)
        val hasNotif = MunnaNotificationListenerService.isNotificationAccessGranted(appContext)

        val installMap = TargetApp.entries.associateWith { app ->
            actionHandler.isAppAvailable(app)
        }
        val installedList = actionHandler.getInstalledLaunchableApps()

        return DevicePermissionsSnapshot(
            hasRecordAudioPermission = hasMic,
            hasCallPhonePermission = hasCall,
            hasOverlayPermission = hasOverlay,
            hasNotificationAccess = hasNotif,
            appInstallStatus = installMap,
            installedApps = installedList
        )
    }

    fun selectTab(tab: MunnaTab) {
        _currentTab.value = tab
        refreshPermissionsAndAppStatus()
    }

    fun setAgentMode(mode: MunnaAgentMode, speakConfirmation: Boolean = true) {
        preferences.setAgentMode(mode)
        if (speakConfirmation) {
            val msg = when (mode) {
                MunnaAgentMode.SMART_AGENT -> "भाई, अब मैं स्मार्ट एजेंट मोड में हूँ। बताओ कौन सा ऐप खोलना है या किसे कॉल लगाना है?"
                MunnaAgentMode.DISCUSSION_PARTNER -> "भाई, लाइव डिस्कशन मोड चालू हो गया है! बताओ आज किस मुद्दे या विचार पर चर्चा करें?"
                MunnaAgentMode.STUDENT_TUTOR -> "भाई, स्टूडेंट पढ़ाई मोड चालू है! बताओ आज कौन-सा विषय या PDF चैप्टर आसान भाषा में समझना है?"
            }
            speakTextAloud(msg)
        }
    }

    fun updateChatDraft(text: String) {
        _chatDraftInput.value = text
    }

    fun addPersonalMemoryFact(category: String, factText: String, contactPhone: String = "") {
        val cleanFact = factText.trim()
        if (cleanFact.isBlank()) return
        viewModelScope.launch {
            val key = if (contactPhone.isNotBlank()) {
                "contact_${cleanFact.lowercase().take(18)}"
            } else {
                "custom_${cleanFact.lowercase().take(20).hashCode()}"
            }
            repository.saveLearnedMemory(
                category = category,
                memoryKey = key,
                memoryValue = if (contactPhone.isNotBlank()) "$cleanFact ($contactPhone)" else cleanFact,
                contactPhone = contactPhone.trim()
            )
        }
    }

    fun deletePersonalMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    /**
     * Loads a real PDF document from Android's Document Picker URI and renders page 0.
     */
    fun loadPdfFromUri(uri: Uri) {
        viewModelScope.launch {
            currentSampleDoc = null
            val fileName = MunnaPdfHelper.resolveFileName(appContext, uri)
            _loadedPdfState.value = LoadedPdfDocumentState(
                uriString = uri.toString(),
                fileName = fileName,
                currentPageIndex = 0,
                totalPages = 1,
                isSampleDocument = false,
                isAnalyzingWithAi = true,
                extractedOrAnalyzedText = "PDF पेज लोड हो रहा है और मुन्ना इसे पढ़ रहा है…"
            )

            val (bitmap, pageCount) = MunnaPdfHelper.renderPdfPageBitmap(appContext, uri, 0)
            if (bitmap != null && pageCount > 0) {
                _loadedPdfState.value = _loadedPdfState.value.copy(
                    currentPageBitmap = bitmap,
                    totalPages = pageCount,
                    currentPageIndex = 0
                )
                analyzeCurrentPdfPageWithAi(promptAction = "READ_AND_EXPLAIN")
            } else {
                val err = "माफ़ करना भाई, यह PDF फ़ाइल खुल नहीं सकी। कृपया कोई दूसरी PDF फ़ाइल चुनें।"
                _loadedPdfState.value = _loadedPdfState.value.copy(
                    isAnalyzingWithAi = false,
                    extractedOrAnalyzedText = err
                )
                speakTextAloud(err)
            }
        }
    }

    fun selectPdfPage(newPageIndex: Int) {
        val current = _loadedPdfState.value
        if (current.totalPages <= 0) return
        val targetIndex = newPageIndex.coerceIn(0, current.totalPages - 1)
        if (targetIndex == current.currentPageIndex && !current.isAnalyzingWithAi) return

        viewModelScope.launch {
            if (current.isSampleDocument) {
                val sample = currentSampleDoc ?: return@launch
                val pageText = sample.contentPages.getOrElse(targetIndex) { sample.contentPages.first() }
                _loadedPdfState.value = current.copy(
                    currentPageIndex = targetIndex,
                    extractedOrAnalyzedText = pageText
                )
                speakTextAloud(pageText)
            } else if (current.uriString.isNotBlank()) {
                val uri = Uri.parse(current.uriString)
                _loadedPdfState.value = current.copy(
                    currentPageIndex = targetIndex,
                    isAnalyzingWithAi = true,
                    extractedOrAnalyzedText = "पेज ${targetIndex + 1} पढ़ा जा रहा है…"
                )
                val (bitmap, total) = MunnaPdfHelper.renderPdfPageBitmap(appContext, uri, targetIndex)
                _loadedPdfState.value = _loadedPdfState.value.copy(
                    currentPageBitmap = bitmap,
                    totalPages = total.coerceAtLeast(1)
                )
                if (bitmap != null) {
                    analyzeCurrentPdfPageWithAi(promptAction = "READ_AND_EXPLAIN")
                }
            }
        }
    }

    fun loadSampleStudyDocument(doc: SampleStudyDocument, speakIntro: Boolean = true) {
        currentSampleDoc = doc
        val firstPage = doc.contentPages.firstOrNull().orEmpty()
        _loadedPdfState.value = LoadedPdfDocumentState(
            uriString = "",
            fileName = "${doc.title}.pdf",
            currentPageIndex = 0,
            totalPages = doc.contentPages.size,
            currentPageBitmap = null,
            extractedOrAnalyzedText = firstPage,
            isSampleDocument = true,
            isAnalyzingWithAi = false
        )
        if (speakIntro) {
            speakTextAloud("भाई, मैंने '${doc.title}' के नोट्स खोल दिए हैं। $firstPage")
        }
    }

    /**
     * Uses Gemini Multimodal (`gemini-3.8-flash`) to read, explain, quiz, or discuss the active PDF page.
     */
    fun analyzeCurrentPdfPageWithAi(promptAction: String) {
        val current = _loadedPdfState.value
        viewModelScope.launch {
            _loadedPdfState.value = current.copy(isAnalyzingWithAi = true)
            val instruction = when (promptAction) {
                "READ_ONLY" -> "इस पेज पर जो लिखा है उसे साफ़ और सरल हिंदी में पढ़कर सुनाएँ ताकि यूज़र सुनकर समझ सके।"
                "QUIZ_ME" -> "एक अच्छे शिक्षक (Student Tutor) की तरह इस पेज के आधार पर छात्र से 3 महत्वपूर्ण सवाल पूछें और उनके संक्षिप्त उत्तर भी नीचे बताएँ।"
                "DISCUSS_PDF" -> "एक दोस्ताना डिस्कशन पार्टनर की तरह इस पेज के मुख्य विचार पर चर्चा शुरू करें और यूज़र से उसकी राय पूछें।"
                else -> "इस PDF पेज को ध्यान से पढ़ें। पहले मुख्य बातें सरल हिंदी में बताएँ और फिर एक बड़े भाई/टीचर की तरह आसान उदाहरण देकर समझाएँ।"
            }

            try {
                val bitmap = current.currentPageBitmap
                val response = withTimeout(60_000L) {
                    if (bitmap != null && !current.isSampleDocument) {
                        geminiModel.generateContent(
                            content {
                                image(bitmap)
                                text("फ़ाइल: ${current.fileName}, पेज ${current.currentPageIndex + 1}/${current.totalPages}.\nनिर्देश: $instruction")
                            }
                        )
                    } else {
                        val baseText = current.extractedOrAnalyzedText
                        geminiModel.generateContent(
                            "नोट्स सामग्री:\n$baseText\n\nनिर्देश: $instruction"
                        )
                    }
                }

                val output = response.text?.trim().orEmpty()
                if (output.isNotBlank()) {
                    _loadedPdfState.value = _loadedPdfState.value.copy(
                        isAnalyzingWithAi = false,
                        extractedOrAnalyzedText = output
                    )
                    repository.addChatMessage("MUNNA", "📄 [${current.fileName} - पेज ${current.currentPageIndex + 1}]:\n$output")
                    speakTextAloud(output.replace("*", "").take(320))
                } else {
                    _loadedPdfState.value = _loadedPdfState.value.copy(isAnalyzingWithAi = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "PDF AI analysis failed", e)
                val fallbackMsg = if (current.isSampleDocument) {
                    current.extractedOrAnalyzedText
                } else {
                    "माफ़ करना भाई, अभी AI से PDF पेज का विश्लेषण करने में नेटवर्क समस्या आई: ${e.localizedMessage ?: "Network Error"}"
                }
                _loadedPdfState.value = _loadedPdfState.value.copy(
                    isAnalyzingWithAi = false,
                    extractedOrAnalyzedText = fallbackMsg
                )
            }
        }
    }

    /**
     * Starts or cancels microphone speech recognition.
     */
    fun onMicButtonTapped(forChatInput: Boolean = false) {
        refreshPermissionsAndAppStatus()
        if (!_permissionsSnapshot.value.hasRecordAudioPermission) {
            voiceManager.setPermissionDeniedError()
            return
        }

        if (voiceState.value.status == MicStatus.LISTENING) {
            voiceManager.cancelListening()
            return
        }

        ttsManager.stop()
        voiceTargetMode = if (forChatInput) {
            VoiceTargetMode.CHAT_INPUT
        } else {
            VoiceTargetMode.COMMAND_ASSISTANT
        }

        val localeTag = settings.value.language.speechLocaleTag
        voiceManager.startListening(localeTag)
    }

    fun cancelListening() {
        voiceManager.cancelListening()
    }

    fun onMicPermissionResult(granted: Boolean, forChatInput: Boolean = false) {
        refreshPermissionsAndAppStatus()
        if (granted) {
            onMicButtonTapped(forChatInput = forChatInput)
        } else {
            voiceManager.setPermissionDeniedError()
        }
    }

    private fun onVoiceInputRecognized(recognizedText: String) {
        if (voiceTargetMode == VoiceTargetMode.CHAT_INPUT) {
            _chatDraftInput.value = recognizedText
            sendChatMessage(recognizedText)
        } else {
            processVoiceOrTextCommand(recognizedText)
        }
    }

    /**
     * Automatically learns from user statements and builds a personalized context string
     * from Room (`user_memories` + `command_history`) so Munna acts as an intelligent learning agent.
     */
    private suspend fun learnAndBuildAgentContext(latestInput: String): String {
        val currentSettings = settings.value
        if (currentSettings.agentLearningEnabled) {
            MunnaCommandParser.extractLearnedMemoryIfAny(latestInput)?.let { learned ->
                repository.saveLearnedMemory(
                    category = learned.category,
                    memoryKey = learned.memoryKey,
                    memoryValue = learned.memoryValue,
                    contactPhone = learned.contactPhone
                )
            }
        }

        val memories = repository.getMemoriesSnapshot()
        val recentCmds = commandHistory.value.take(5)
        val mode = currentSettings.agentMode

        val modeDirective = when (mode) {
            MunnaAgentMode.SMART_AGENT ->
                "आप अभी 'स्मार्ट पर्सनल एजेंट' मोड में हैं। दोस्त की तरह मददगार जवाब दें और यूज़र की पसंद का ध्यान रखें।"
            MunnaAgentMode.DISCUSSION_PARTNER ->
                "आप अभी 'लाइव डिस्कशन पार्टनर' (Discussion Mode) में हैं। यूज़र के साथ दोस्त की तरह विचार-विमर्श करें, दोनों पहलू बताएँ और अंत में यूज़र से एक रोचक सवाल ज़रूर पूछें ताकि चर्चा आगे बढ़े।"
            MunnaAgentMode.STUDENT_TUTOR ->
                "आप अभी 'स्टूडेंट गुरु / टीचर' (Student Study Mode) में हैं। छात्र को बड़े भाई की तरह बहुत आसान भाषा और उदाहरण देकर समझाएँ, और अंत में उसकी समझ जांचने के लिए 1 छोटा सवाल पूछें।"
        }

        val memoryLines = if (memories.isNotEmpty()) {
            memories.take(10).joinToString("\n") { "- [${it.category}] ${it.memoryValue}" }
        } else {
            "- अभी कोई विशेष व्यक्तिगत जानकारी सेव नहीं है।"
        }

        val usageLines = if (recentCmds.isNotEmpty()) {
            recentCmds.joinToString(", ") { it.recognizedText }
        } else {
            "कोई हालिया कमांड नहीं"
        }

        return """
            सक्रिय मोड निर्देश: $modeDirective
            यूज़र के डेटा से सीखी गई यादें (Learned User Memory):
            $memoryLines
            यूज़र के हालिया कमांड/आदतें: $usageLines
        """.trimIndent()
    }

    /**
     * Processes a voice or text command using local command matching first,
     * enforcing PaymentAppSecurityGuard, supporting voice calling, PDF reading,
     * mode switching, and falling back to the memory-aware Gemini AI Agent.
     */
    fun processVoiceOrTextCommand(rawCommand: String) {
        val trimmed = rawCommand.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            voiceManager.setProcessingState(trimmed)
            val currentSettings = settings.value

            // Auto-learn any personal fact / contact number mentioned in the command
            if (currentSettings.agentLearningEnabled) {
                MunnaCommandParser.extractLearnedMemoryIfAny(trimmed)?.let { learned ->
                    repository.saveLearnedMemory(
                        category = learned.category,
                        memoryKey = learned.memoryKey,
                        memoryValue = learned.memoryValue,
                        contactPhone = learned.contactPhone
                    )
                }
            }

            val parsed = MunnaCommandParser.parse(trimmed)

            when (parsed) {
                is ParsedCommand.BlockedPaymentApp -> {
                    val execResult = actionHandler.buildBlockedPaymentAppResult(
                        blockedAppName = parsed.blockedAppName,
                        language = currentSettings.language
                    )
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = parsed.normalizedText,
                        category = parsed.categoryLabel,
                        spokenResponse = execResult.spokenResponse,
                        detailText = execResult.detailMessage,
                        statusBadge = execResult.statusSummary,
                        isSuccess = false
                    )
                }

                is ParsedCommand.OpenApp -> {
                    val execResult = actionHandler.launchTargetApp(parsed.targetApp, currentSettings.language)
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = parsed.normalizedText,
                        category = parsed.categoryLabel,
                        spokenResponse = execResult.spokenResponse,
                        detailText = execResult.detailMessage,
                        statusBadge = execResult.statusSummary,
                        isSuccess = execResult.isSuccess
                    )
                }

                is ParsedCommand.OpenCustomAppByName -> {
                    val execResult = actionHandler.launchCustomAppByName(parsed.requestedAppName, currentSettings.language)
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = parsed.normalizedText,
                        category = parsed.categoryLabel,
                        spokenResponse = execResult.spokenResponse,
                        detailText = execResult.detailMessage,
                        statusBadge = execResult.statusSummary,
                        isSuccess = execResult.isSuccess
                    )
                }

                is ParsedCommand.MakePhoneCall -> {
                    val memories = repository.getMemoriesSnapshot()
                    val execResult = actionHandler.placeVoiceCall(
                        targetNameOrNumber = parsed.targetNameOrNumber,
                        extractedDigits = parsed.extractedDigits,
                        savedMemories = memories,
                        language = currentSettings.language
                    )
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = parsed.normalizedText,
                        category = parsed.categoryLabel,
                        spokenResponse = execResult.spokenResponse,
                        detailText = execResult.detailMessage,
                        statusBadge = execResult.statusSummary,
                        isSuccess = execResult.isSuccess
                    )
                }

                is ParsedCommand.OpenPdfReader -> {
                    _currentTab.value = MunnaTab.AGENT_STUDY
                    val msg = "ठीक है भाई, मैंने PDF रीडर और स्टूडेंट स्टडी सेंटर खोल दिया है। यहाँ आप कोई भी PDF चुनकर मुझसे सुन और समझ सकते हैं!"
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = parsed.normalizedText,
                        category = parsed.categoryLabel,
                        spokenResponse = msg,
                        detailText = msg,
                        statusBadge = "📄 PDF रीडर खोला गया",
                        isSuccess = true
                    )
                }

                is ParsedCommand.SwitchAgentMode -> {
                    preferences.setAgentMode(parsed.targetMode)
                    if (parsed.initialTopic.length >= 2) {
                        handleGeneralAiCommand(
                            rawText = trimmed,
                            parsed = ParsedCommand.AskAiQuestion(trimmed, parsed.normalizedText, trimmed)
                        )
                    } else {
                        val msg = when (parsed.targetMode) {
                            MunnaAgentMode.DISCUSSION_PARTNER ->
                                "बिल्कुल भाई! चलो दोस्त की तरह डिस्कस करते हैं। बताओ आज किस टॉपिक पर बात करें — करियर, टेक्नोलॉजी, पढ़ाई या कोई और मुद्दा?"
                            MunnaAgentMode.STUDENT_TUTOR ->
                                "शाबाश भाई! स्टूडेंट पढ़ाई मोड चालू हो गया है। बताओ आज कौन-सा सब्जेक्ट या टॉपिक पढ़ना है? मैं तुम्हें आसान उदाहरणों के साथ सिखाऊँगा!"
                            MunnaAgentMode.SMART_AGENT ->
                                "हाँ भाई, मैं स्मार्ट एजेंट मोड में तैयार हूँ!"
                        }
                        completeLocalCommand(
                            rawText = trimmed,
                            normalizedText = parsed.normalizedText,
                            category = parsed.categoryLabel,
                            spokenResponse = msg,
                            detailText = msg,
                            statusBadge = "${parsed.targetMode.badgeEmoji} ${parsed.targetMode.titleHindi}",
                            isSuccess = true
                        )
                    }
                }

                is ParsedCommand.TellTime -> {
                    val execResult = actionHandler.getCurrentTimeResponse(currentSettings.language)
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = parsed.normalizedText,
                        category = parsed.categoryLabel,
                        spokenResponse = execResult.spokenResponse,
                        detailText = execResult.detailMessage,
                        statusBadge = execResult.statusSummary,
                        isSuccess = true
                    )
                }

                is ParsedCommand.TellDate -> {
                    val execResult = actionHandler.getCurrentDateResponse(currentSettings.language)
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = parsed.normalizedText,
                        category = parsed.categoryLabel,
                        spokenResponse = execResult.spokenResponse,
                        detailText = execResult.detailMessage,
                        statusBadge = execResult.statusSummary,
                        isSuccess = true
                    )
                }

                is ParsedCommand.GreetingOrListen -> {
                    val execResult = actionHandler.getGreetingResponse(currentSettings.language)
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = parsed.normalizedText,
                        category = parsed.categoryLabel,
                        spokenResponse = execResult.spokenResponse,
                        detailText = execResult.detailMessage,
                        statusBadge = execResult.statusSummary,
                        isSuccess = true
                    )
                }

                is ParsedCommand.ReadNotifications -> {
                    handleReadNotificationsCommand(trimmed, parsed)
                }

                is ParsedCommand.AskAiQuestion -> {
                    handleGeneralAiCommand(trimmed, parsed)
                }

                is ParsedCommand.EmptyOrUnclear -> {
                    val msg = "माफ़ करना भाई, आपकी बात साफ़ समझ नहीं आई। कृपया दोबारा बोलें।"
                    completeLocalCommand(
                        rawText = trimmed,
                        normalizedText = "",
                        category = parsed.categoryLabel,
                        spokenResponse = msg,
                        detailText = msg,
                        statusBadge = "अस्पष्ट कमांड",
                        isSuccess = false
                    )
                }
            }
        }
    }

    fun launchShortcutApp(app: TargetApp) {
        processVoiceOrTextCommand(app.sampleCommand)
    }

    private suspend fun handleReadNotificationsCommand(
        rawText: String,
        parsed: ParsedCommand.ReadNotifications
    ) {
        refreshPermissionsAndAppStatus()
        val hasAccess = _permissionsSnapshot.value.hasNotificationAccess
        val notifs = recentNotifications.value

        if (!hasAccess) {
            val msg = "भाई, नोटिफिकेशन पढ़ने के लिए पहले सेटिंग्स में जाकर 'Notification Access' की अनुमति देनी होगी।"
            completeLocalCommand(
                rawText = rawText,
                normalizedText = parsed.normalizedText,
                category = parsed.categoryLabel,
                spokenResponse = msg,
                detailText = msg,
                statusBadge = "अनुमति आवश्यक (Permission Needed)",
                isSuccess = false
            )
            return
        }

        if (notifs.isEmpty()) {
            val msg = "भाई, अभी कोई नया नोटिफिकेशन या WhatsApp संदेश नहीं आया है।"
            completeLocalCommand(
                rawText = rawText,
                normalizedText = parsed.normalizedText,
                category = parsed.categoryLabel,
                spokenResponse = msg,
                detailText = msg,
                statusBadge = "कोई नया संदेश नहीं",
                isSuccess = true
            )
        } else {
            val latest = notifs.first()
            val spoken = "${latest.appDisplayName} पर ${latest.senderOrTitle} का संदेश है: ${latest.contentSummary}"
            completeLocalCommand(
                rawText = rawText,
                normalizedText = parsed.normalizedText,
                category = parsed.categoryLabel,
                spokenResponse = spoken,
                detailText = spoken,
                statusBadge = "${latest.appDisplayName} नोटिफिकेशन पढ़ा गया",
                isSuccess = true
            )
        }
    }

    private suspend fun handleGeneralAiCommand(
        rawText: String,
        parsed: ParsedCommand.AskAiQuestion
    ) {
        val currentSettings = settings.value
        try {
            val agentContext = learnAndBuildAgentContext(rawText)
            val langInstruction = when (currentSettings.language) {
                AppLanguage.HINDI -> "सरल, दोस्ताना हिंदी (देवनागरी) में 2 से 4 वाक्यों में जवाब दें।"
                AppLanguage.HINGLISH -> "सरल, दोस्ताना Hinglish (Roman script) में 2 से 4 वाक्यों में जवाब दें।"
                AppLanguage.ENGLISH -> "Respond in simple, friendly Indian English in 2-4 sentences."
            }

            val response = withTimeout(60_000L) {
                geminiModel.generateContent(
                    """
                    $agentContext
                    
                    यूज़र ने कहा: "$rawText"
                    निर्देश: $langInstruction
                    """.trimIndent()
                )
            }

            val answerText = response.text?.trim().orEmpty()
            if (answerText.isNotBlank()) {
                repository.addChatMessage("USER", rawText)
                repository.addChatMessage("MUNNA", answerText)

                completeLocalCommand(
                    rawText = rawText,
                    normalizedText = parsed.normalizedText,
                    category = "${currentSettings.agentMode.badgeEmoji} ${currentSettings.agentMode.titleHindi}",
                    spokenResponse = answerText.replace("*", ""),
                    detailText = answerText,
                    statusBadge = "मुन्ना एजेंट ने जवाब दिया",
                    isSuccess = true
                )
            } else {
                throw IllegalStateException("AI से खाली उत्तर प्राप्त हुआ।")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI command response timed out", e)
            val errMsg = "माफ़ करना भाई, जवाब लाने में 60 सेकंड से ज़्यादा समय लग गया। कृपया इंटरनेट जांचकर दोबारा पूछें।"
            completeLocalCommand(
                rawText = rawText,
                normalizedText = parsed.normalizedText,
                category = parsed.categoryLabel,
                spokenResponse = errMsg,
                detailText = errMsg,
                statusBadge = "AI टाइमआउट (Timeout)",
                isSuccess = false
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI command generation failed", e)
            val errMsg = "माफ़ करना भाई, अभी AI सेवा से जुड़ने में समस्या आई (${e.localizedMessage ?: "Network Error"})। ऐप खोलने, कॉल लगाने या समय पूछने जैसे कमांड बिना इंटरनेट के भी काम करेंगे!"
            completeLocalCommand(
                rawText = rawText,
                normalizedText = parsed.normalizedText,
                category = parsed.categoryLabel,
                spokenResponse = "माफ़ करना भाई, अभी AI सेवा उपलब्ध नहीं हो सकी।",
                detailText = errMsg,
                statusBadge = "AI सेवा अनुपलब्ध",
                isSuccess = false
            )
        }
    }

    private suspend fun completeLocalCommand(
        rawText: String,
        normalizedText: String,
        category: String,
        spokenResponse: String,
        detailText: String,
        statusBadge: String,
        isSuccess: Boolean
    ) {
        val currentSettings = settings.value
        _lastActionFeedback.value = LastActionFeedback(
            commandText = rawText,
            categoryLabel = category,
            responseText = detailText,
            statusBadge = statusBadge,
            isSuccess = isSuccess
        )

        voiceManager.setCompletedState(rawText)

        if (currentSettings.spokenResponsesEnabled) {
            ttsManager.speak(
                text = spokenResponse,
                enabled = true,
                speechRate = currentSettings.speechRate,
                pitch = currentSettings.speechPitch
            )
        }

        if (currentSettings.saveCommandHistory) {
            repository.logCommand(
                recognizedText = rawText,
                normalizedCommand = normalizedText,
                intentCategory = category,
                actionStatus = statusBadge,
                spokenResponse = detailText,
                isSuccess = isSuccess
            )
        }
    }

    /**
     * Sends a message in the dedicated AI Chat screen using streaming (`generateContentStream`)
     * enriched with Munna's learned Personal Memory and active Agent Mode (Agent / Discussion / Student Tutor).
     */
    fun sendChatMessage(userMessage: String = _chatDraftInput.value) {
        val cleanMsg = userMessage.trim()
        if (cleanMsg.isBlank()) return
        if (_aiChatState.value is AiChatGenerationState.Loading ||
            _aiChatState.value is AiChatGenerationState.Streaming
        ) {
            return
        }

        _chatDraftInput.value = ""

        viewModelScope.launch {
            repository.addChatMessage(sender = "USER", messageText = cleanMsg)
            _aiChatState.value = AiChatGenerationState.Loading

            val currentSettings = settings.value
            val agentContext = learnAndBuildAgentContext(cleanMsg)
            val langHint = when (currentSettings.language) {
                AppLanguage.HINDI -> "कृपया सरल और दोस्ताना हिंदी में जवाब दें।"
                AppLanguage.HINGLISH -> "कृपया दोस्ताना Hinglish में जवाब दें।"
                AppLanguage.ENGLISH -> "Please reply in friendly, clear English."
            }

            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    val fullPrompt = """
                        $agentContext
                        
                        यूज़र का संदेश: $cleanMsg
                        ($langHint)
                    """.trimIndent()
                    geminiModel.generateContentStream(fullPrompt).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        accumulated.append(chunkText)
                        _aiChatState.value = AiChatGenerationState.Streaming(accumulated.toString())
                    }
                }

                val finalReply = accumulated.toString().trim()
                if (finalReply.isNotBlank()) {
                    repository.addChatMessage(sender = "MUNNA", messageText = finalReply)
                    _aiChatState.value = AiChatGenerationState.Idle

                    if (currentSettings.spokenResponsesEnabled) {
                        val ttsSnippet = finalReply.replace("*", "").take(280)
                        ttsManager.speak(
                            text = ttsSnippet,
                            enabled = true,
                            speechRate = currentSettings.speechRate,
                            pitch = currentSettings.speechPitch
                        )
                    }
                } else {
                    val emptyErr = "AI मॉडल से कोई उत्तर प्राप्त नहीं हुआ। कृपया दोबारा प्रयास करें।"
                    repository.addChatMessage(sender = "MUNNA", messageText = emptyErr, isError = true)
                    _aiChatState.value = AiChatGenerationState.Error(emptyErr)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat streaming timed out", e)
                val timeoutMsg = "माफ़ करना भाई, जवाब आने में 60 सेकंड से अधिक समय लग गया। कृपया अपना इंटरनेट जांचें और फिर कोशिश करें।"
                repository.addChatMessage(sender = "MUNNA", messageText = timeoutMsg, isError = true)
                _aiChatState.value = AiChatGenerationState.Error(timeoutMsg)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI chat generation failed", e)
                val errMsg = "माफ़ करना भाई, अभी नेटवर्क या AI सेवा में समस्या है: ${e.localizedMessage ?: "अज्ञात त्रुटि"}"
                repository.addChatMessage(sender = "MUNNA", messageText = errMsg, isError = true)
                _aiChatState.value = AiChatGenerationState.Error(errMsg)
            }
        }
    }

    fun speakTextAloud(text: String) {
        val s = settings.value
        ttsManager.speak(
            text = text.replace("*", ""),
            enabled = true,
            speechRate = s.speechRate,
            pitch = s.speechPitch
        )
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun clearCommandHistory() {
        viewModelScope.launch {
            repository.clearCommandHistory()
        }
    }

    fun deleteCommandHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteCommand(id)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            seedInitialDataIfNeeded()
        }
    }

    fun toggleSystemOverlayService(enable: Boolean): Boolean {
        refreshPermissionsAndAppStatus()
        if (enable) {
            if (!Settings.canDrawOverlays(appContext)) {
                return false
            }
            val serviceIntent = Intent(appContext, FloatingMicService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(serviceIntent)
            } else {
                appContext.startService(serviceIntent)
            }
            preferences.setSystemOverlayEnabled(true)
            return true
        } else {
            val stopIntent = Intent(appContext, FloatingMicService::class.java).apply {
                action = FloatingMicService.ACTION_STOP_FLOATING_MIC
            }
            appContext.startService(stopIntent)
            preferences.setSystemOverlayEnabled(false)
            return true
        }
    }

    override fun onCleared() {
        voiceManager.destroy()
        ttsManager.shutdown()
        super.onCleared()
    }

    private enum class VoiceTargetMode {
        COMMAND_ASSISTANT,
        CHAT_INPUT
    }

    companion object {
        private const val TAG = "MunnaViewModel"
    }
}
