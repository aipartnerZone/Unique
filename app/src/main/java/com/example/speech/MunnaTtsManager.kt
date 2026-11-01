package com.example.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

data class TtsState(
    val isReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val activeLocaleLabel: String = "हिंदी (hi-IN)",
    val errorMessage: String? = null
)

class MunnaTtsManager(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null

    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val engine = tts ?: return@TextToSpeech
                    val hindiLocale = Locale.forLanguageTag("hi-IN")
                    val indiaEngLocale = Locale.forLanguageTag("en-IN")

                    val hindiResult = engine.setLanguage(hindiLocale)
                    val chosenLabel = if (hindiResult != TextToSpeech.LANG_MISSING_DATA &&
                        hindiResult != TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        "हिंदी (hi-IN)"
                    } else {
                        val enResult = engine.setLanguage(indiaEngLocale)
                        if (enResult != TextToSpeech.LANG_MISSING_DATA &&
                            enResult != TextToSpeech.LANG_NOT_SUPPORTED
                        ) {
                            "हिंग्लिश (en-IN)"
                        } else {
                            engine.language = Locale.US
                            "English (Default)"
                        }
                    }

                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _ttsState.value = _ttsState.value.copy(isSpeaking = true)
                        }

                        override fun onDone(utteranceId: String?) {
                            _ttsState.value = _ttsState.value.copy(isSpeaking = false)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _ttsState.value = _ttsState.value.copy(isSpeaking = false)
                        }
                    })

                    _ttsState.value = TtsState(
                        isReady = true,
                        isSpeaking = false,
                        activeLocaleLabel = chosenLabel,
                        errorMessage = null
                    )
                } else {
                    Log.w(TAG, "TTS initialization returned status: $status")
                    _ttsState.value = TtsState(
                        isReady = false,
                        isSpeaking = false,
                        activeLocaleLabel = "अनुपलब्ध (Unavailable)",
                        errorMessage = "फ़ोन में Text-to-Speech सेवा उपलब्ध नहीं है।"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
            _ttsState.value = TtsState(
                isReady = false,
                isSpeaking = false,
                activeLocaleLabel = "त्रुटि (Error)",
                errorMessage = "आवाज़ इंजन शुरू नहीं हो सका: ${e.localizedMessage ?: ""}"
            )
        }
    }

    fun speak(
        text: String,
        enabled: Boolean = true,
        speechRate: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        if (!enabled || text.isBlank()) return
        val engine = tts ?: return
        if (!_ttsState.value.isReady) return

        try {
            engine.setSpeechRate(speechRate.coerceIn(0.6f, 1.5f))
            engine.setPitch(pitch.coerceIn(0.7f, 1.4f))
            val params = Bundle()
            val utteranceId = UUID.randomUUID().toString()
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text with TTS", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _ttsState.value = _ttsState.value.copy(isSpeaking = false)
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down TTS", e)
        }
    }

    companion object {
        private const val TAG = "MunnaTtsManager"
    }
}
