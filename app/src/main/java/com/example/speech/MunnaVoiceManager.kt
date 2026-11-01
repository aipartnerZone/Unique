package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MicStatus {
    IDLE,
    LISTENING,
    PROCESSING,
    COMPLETED,
    ERROR
}

data class VoiceUiState(
    val status: MicStatus = MicStatus.IDLE,
    val statusLabel: String = "बोलने के लिए माइक दबाएँ",
    val partialTranscript: String = "",
    val finalTranscript: String = "",
    val rmsDb: Float = 0f,
    val errorMessage: String? = null,
    val isServiceAvailable: Boolean = true
)

class MunnaVoiceManager(
    context: Context,
    private val onCommandRecognized: (String) -> Unit
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _voiceState = MutableStateFlow(
        VoiceUiState(
            isServiceAvailable = SpeechRecognizer.isRecognitionAvailable(appContext)
        )
    )
    val voiceState: StateFlow<VoiceUiState> = _voiceState.asStateFlow()

    fun startListening(languageTag: String = "hi-IN") {
        mainHandler.post {
            val available = SpeechRecognizer.isRecognitionAvailable(appContext)
            if (!available) {
                _voiceState.value = VoiceUiState(
                    status = MicStatus.ERROR,
                    statusLabel = "वॉइस सेवा उपलब्ध नहीं है",
                    errorMessage = "इस डिवाइस/एम्युलेटर पर Google Speech Recognition सेवा उपलब्ध नहीं है। आप नीचे दिए गए क्विक कमांड बटन या टाइपिंग बॉक्स का उपयोग कर सकते हैं।",
                    isServiceAvailable = false
                )
                return@post
            }

            try {
                destroyRecognizerInternal()
                val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = _voiceState.value.copy(
                            status = MicStatus.LISTENING,
                            statusLabel = "सुन रहा हूँ…",
                            partialTranscript = "",
                            errorMessage = null,
                            isServiceAvailable = true
                        )
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = _voiceState.value.copy(
                            status = MicStatus.LISTENING,
                            statusLabel = "सुन रहा हूँ…"
                        )
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalizedRms = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                        _voiceState.value = _voiceState.value.copy(rmsDb = normalizedRms)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = _voiceState.value.copy(
                            status = MicStatus.PROCESSING,
                            statusLabel = "आपकी बात समझ रहा हूँ…",
                            rmsDb = 0f
                        )
                    }

                    override fun onError(error: Int) {
                        val (hindiTitle, hindiDetail) = mapSpeechErrorToHindi(error)
                        Log.w(TAG, "SpeechRecognizer error code: $error ($hindiTitle)")
                        _voiceState.value = _voiceState.value.copy(
                            status = MicStatus.ERROR,
                            statusLabel = hindiTitle,
                            rmsDb = 0f,
                            errorMessage = hindiDetail
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val bestMatch = matches?.firstOrNull()?.trim().orEmpty()
                        if (bestMatch.isNotBlank()) {
                            _voiceState.value = _voiceState.value.copy(
                                status = MicStatus.COMPLETED,
                                statusLabel = "काम पूरा हुआ!",
                                partialTranscript = bestMatch,
                                finalTranscript = bestMatch,
                                rmsDb = 0f,
                                errorMessage = null
                            )
                            onCommandRecognized(bestMatch)
                        } else {
                            _voiceState.value = _voiceState.value.copy(
                                status = MicStatus.ERROR,
                                statusLabel = "आवाज़ सुनाई नहीं दी",
                                rmsDb = 0f,
                                errorMessage = "कोई शब्द पहचाना नहीं जा सका। कृपया माइक के पास साफ़ बोलें।"
                            )
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()
                        if (partial.isNotBlank()) {
                            _voiceState.value = _voiceState.value.copy(
                                partialTranscript = partial
                            )
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                _voiceState.value = _voiceState.value.copy(
                    status = MicStatus.LISTENING,
                    statusLabel = "सुन रहा हूँ…",
                    partialTranscript = "",
                    errorMessage = null
                )
                recognizer.startListening(recognizerIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start SpeechRecognizer", e)
                _voiceState.value = _voiceState.value.copy(
                    status = MicStatus.ERROR,
                    statusLabel = "माइक शुरू नहीं हो सका",
                    rmsDb = 0f,
                    errorMessage = "वॉइस रिकग्निशन शुरू करने में समस्या आई। आप नीचे दिए गए क्विक कमांड या टेक्स्ट बॉक्स से कमांड दे सकते हैं।"
                )
            }
        }
    }

    fun cancelListening() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.w(TAG, "Error cancelling SpeechRecognizer", e)
            }
            _voiceState.value = _voiceState.value.copy(
                status = MicStatus.IDLE,
                statusLabel = "बोलने के लिए माइक दबाएँ",
                partialTranscript = "",
                rmsDb = 0f,
                errorMessage = null
            )
        }
    }

    fun setProcessingState(transcript: String) {
        _voiceState.value = _voiceState.value.copy(
            status = MicStatus.PROCESSING,
            statusLabel = "आपकी बात समझ रहा हूँ…",
            partialTranscript = transcript,
            finalTranscript = transcript,
            errorMessage = null
        )
    }

    fun setCompletedState(transcript: String) {
        _voiceState.value = _voiceState.value.copy(
            status = MicStatus.COMPLETED,
            statusLabel = "काम पूरा हुआ!",
            partialTranscript = transcript,
            finalTranscript = transcript,
            rmsDb = 0f,
            errorMessage = null
        )
    }

    fun setPermissionDeniedError() {
        _voiceState.value = _voiceState.value.copy(
            status = MicStatus.ERROR,
            statusLabel = "माइक की अनुमति आवश्यक है",
            rmsDb = 0f,
            errorMessage = "भाई, आपकी आवाज़ सुनने के लिए माइक्रोफ़ोन की अनुमति (Permission) ज़रूरी है। कृपया 'अनुमति दें' पर टैप करें या टेक्स्ट से कमांड दें।"
        )
    }

    fun resetToIdle() {
        _voiceState.value = _voiceState.value.copy(
            status = MicStatus.IDLE,
            statusLabel = "बोलने के लिए माइक दबाएँ",
            rmsDb = 0f,
            errorMessage = null
        )
    }

    fun destroy() {
        mainHandler.post {
            destroyRecognizerInternal()
        }
    }

    private fun destroyRecognizerInternal() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying SpeechRecognizer", e)
        } finally {
            speechRecognizer = null
        }
    }

    private fun mapSpeechErrorToHindi(errorCode: Int): Pair<String, String> {
        return when (errorCode) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "माइक अनुमति नहीं मिली" to "माइक्रोफ़ोन की अनुमति नहीं दी गई है। कृपया सेटिंग्स में जाकर माइक की अनुमति दें।"
            SpeechRecognizer.ERROR_AUDIO ->
                "ऑडियो रिकॉर्डिंग त्रुटि" to "माइक्रोफ़ोन से आवाज़ रिकॉर्ड करने में समस्या आई। यदि आप एम्युलेटर पर हैं, तो नीचे दिए गए टेस्ट कमांड चिप्स दबाएँ।"
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "इंटरनेट कनेक्शन की समस्या" to "वॉइस पहचान के लिए इंटरनेट कनेक्शन में देरी हुई। कृपया अपना इंटरनेट जांचें और फिर कोशिश करें।"
            SpeechRecognizer.ERROR_NO_MATCH ->
                "आवाज़ समझ नहीं आई" to "माफ़ करना भाई, आपकी आवाज़ साफ़ समझ नहीं आई। कृपया दोबारा माइक दबाकर बोलें (जैसे: 'YouTube kholo')।"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "कोई आवाज़ नहीं मिली" to "माइक चालू था लेकिन कोई आवाज़ सुनाई नहीं दी। बोलने के लिए फिर से माइक दबाएँ।"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "माइक अभी व्यस्त है" to "वॉइस सेवा अभी व्यस्त है। एक सेकंड रुककर दोबारा कोशिश करें।"
            SpeechRecognizer.ERROR_CLIENT ->
                "माइक रोका गया" to "वॉइस पहचान रोक दी गई है। दोबारा बोलने के लिए माइक दबाएँ।"
            else ->
                "वॉइस सेवा उपलब्ध नहीं" to "इस डिवाइस पर लाइव माइक सेवा उपलब्ध नहीं हो सकी। आप नीचे दिए गए टेस्ट बटन या टाइपिंग बॉक्स से मुन्ना को कमांड दे सकते हैं।"
        }
    }

    companion object {
        private const val TAG = "MunnaVoiceManager"
    }
}
