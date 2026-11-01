package com.example.ui

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppLanguage
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CommandHistoryEntity
import com.example.data.local.MunnaDatabase
import com.example.data.local.MunnaPreferences
import com.example.data.local.MunnaRepository
import com.example.data.local.MunnaSettingsState
import com.example.domain.action.AppActionHandler
import com.example.domain.parser.MunnaCommandParser
import com.example.domain.parser.ParsedCommand
import com.example.domain.parser.TargetApp
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

enum class MunnaTab {
    HOME,
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
    val hasOverlayPermission: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val appInstallStatus: Map<TargetApp, Boolean> = emptyMap()
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

    // Lazy initialization of Gemini 3.8 Flash model via Firebase AI Logic (per skill requirement)
    private val geminiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.7f
            },
            systemInstruction = content {
                text(
                    """
                    आप 'Munna AI' (मुन्ना AI) हैं — भारतीय Android यूज़र्स के लिए एक दोस्ताना, समझदार, भरोसेमंद और सरल AI वॉयस असिस्टेंट।
                    आपकी टैगलाइन है: "Bolo, Munna Karega!"
                    नियम:
                    1. हमेशा सरल, आदरपूर्ण और दोस्ताना हिंदी (या यूज़र चाहे तो हिंग्लिश/English) में जवाब दें, जैसे एक अच्छा छोटा भाई या दोस्त बात करता है ("हाँ भाई", "बिल्कुल भाई", "नमस्ते भाई")।
                    2. अपने उत्तर छोटे, स्पष्ट और काम के रखें (2 से 4 वाक्य), ताकि Text-to-Speech से सुनने में आसान हों।
                    3. कभी भी झूठा दावा न करें कि आपने कोई फ़ोन सेटिंग बदल दी है या कोई ऐसा ऐप खोल दिया है जो आपके अधिकार में नहीं है।
                    """.trimIndent()
                )
            }
        )
    }

    private val _currentTab = MutableStateFlow(MunnaTab.HOME)
    val currentTab: StateFlow<MunnaTab> = _currentTab.asStateFlow()

    // Controls whether the current voice listening session came from Home/FloatingMic vs Chat input
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

    private val _lastActionFeedback = MutableStateFlow<LastActionFeedback?>(
        LastActionFeedback(
            commandText = "नमस्ते भाई! मैं मुन्ना हूँ।",
            categoryLabel = "तैयार (Ready)",
            responseText = "माइक दबाकर बोलें: \"Munna, YouTube kholo\", \"WhatsApp kholo\", \"अभी कितने बजे हैं?\" या कोई भी सवाल पूछें!",
            statusBadge = "मुन्ना तैयार है",
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
        seedInitialWelcomeChatIfNeeded()
    }

    private fun seedInitialWelcomeChatIfNeeded() {
        viewModelScope.launch {
            repository.chatMessages.collect { messages ->
                if (messages.isEmpty()) {
                    repository.addChatMessage(
                        sender = "MUNNA",
                        messageText = "नमस्ते भाई! 👋 मैं मुन्ना AI हूँ — आपका अपना AI दोस्त।\n\nआप मुझसे हिंदी या हिंग्लिश में कोई भी सवाल पूछ सकते हैं (जैसे पढ़ाई, काम, सामान्य ज्ञान, या रोज़मर्रा की मदद)!"
                    )
                }
                return@collect
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

        val hasOverlay = Settings.canDrawOverlays(appContext)
        val hasNotif = MunnaNotificationListenerService.isNotificationAccessGranted(appContext)

        val installMap = TargetApp.entries.associateWith { app ->
            actionHandler.isAppAvailable(app)
        }

        return DevicePermissionsSnapshot(
            hasRecordAudioPermission = hasMic,
            hasOverlayPermission = hasOverlay,
            hasNotificationAccess = hasNotif,
            appInstallStatus = installMap
        )
    }

    fun selectTab(tab: MunnaTab) {
        _currentTab.value = tab
        refreshPermissionsAndAppStatus()
    }

    fun updateChatDraft(text: String) {
        _chatDraftInput.value = text
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
     * Processes a voice or text command using local command matching first (Requirement 4),
     * and falls back to Gemini AI conversation handler only for general questions.
     */
    fun processVoiceOrTextCommand(rawCommand: String) {
        val trimmed = rawCommand.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            voiceManager.setProcessingState(trimmed)
            val currentSettings = settings.value
            val parsed = MunnaCommandParser.parse(trimmed)

            when (parsed) {
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
        val sampleCmd = app.sampleCommand
        processVoiceOrTextCommand(sampleCmd)
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
            val langInstruction = when (currentSettings.language) {
                AppLanguage.HINDI -> "सरल हिंदी (देवनागरी) में अधिकतम 2-3 वाक्यों में जवाब दें।"
                AppLanguage.HINGLISH -> "सरल Hinglish (Roman script) में अधिकतम 2-3 वाक्यों में जवाब दें।"
                AppLanguage.ENGLISH -> "Respond in simple, friendly Indian English in 2-3 short sentences."
            }

            val response = withTimeout(60_000L) {
                geminiModel.generateContent("यूज़र ने कहा: \"$rawText\"\nनिर्देश: $langInstruction")
            }

            val answerText = response.text?.trim().orEmpty()
            if (answerText.isNotBlank()) {
                // Also record in AI chat history so user can continue the conversation
                repository.addChatMessage("USER", rawText)
                repository.addChatMessage("MUNNA", answerText)

                completeLocalCommand(
                    rawText = rawText,
                    normalizedText = parsed.normalizedText,
                    category = parsed.categoryLabel,
                    spokenResponse = answerText,
                    detailText = answerText,
                    statusBadge = "मुन्ना AI ने जवाब दिया",
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
            val errMsg = "माफ़ करना भाई, अभी AI सेवा से जुड़ने में समस्या आई (${e.localizedMessage ?: "Network Error"})। ऐप खोलने या समय पूछने जैसे कमांड बिना इंटरनेट के भी काम करेंगे!"
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
     * with a realistic 60s coroutine timeout and cooperative CancellationException preservation.
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
            val langHint = when (currentSettings.language) {
                AppLanguage.HINDI -> "कृपया सरल और दोस्ताना हिंदी में जवाब दें।"
                AppLanguage.HINGLISH -> "कृपया दोस्ताना Hinglish में जवाब दें।"
                AppLanguage.ENGLISH -> "Please reply in friendly, clear English."
            }

            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    geminiModel.generateContentStream("$cleanMsg\n($langHint)").collect { chunk ->
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
                        // Speak a concise portion so long responses don't overwhelm TTS
                        val ttsSnippet = finalReply.replace("*", "").take(260)
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
                // Always re-throw CancellationException per coroutine & Firebase AI Logic guidelines
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
            seedInitialWelcomeChatIfNeeded()
        }
    }

    fun toggleSystemOverlayService(enable: Boolean): Boolean {
        refreshPermissionsAndAppStatus()
        if (enable) {
            if (!Settings.canDrawOverlays(appContext)) {
                return false // Caller should prompt user to grant overlay permission
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
