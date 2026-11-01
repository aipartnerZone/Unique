package com.example.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String, val speechLocaleTag: String) {
    HINDI("hi", "हिंदी (Hindi)", "hi-IN"),
    HINGLISH("hinglish", "हिंग्लिश (Hinglish)", "en-IN"),
    ENGLISH("en", "English", "en-IN")
}

data class MunnaSettingsState(
    val language: AppLanguage = AppLanguage.HINDI,
    val spokenResponsesEnabled: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val inAppFloatingMicEnabled: Boolean = true,
    val systemOverlayEnabled: Boolean = false,
    val notificationReaderEnabled: Boolean = false,
    val notificationReaderPaused: Boolean = false,
    val hideSensitiveNotifications: Boolean = true,
    val saveCommandHistory: Boolean = true,
    val showWelcomeBanner: Boolean = false
)

class MunnaPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<MunnaSettingsState> = _settings.asStateFlow()

    private fun loadSettings(): MunnaSettingsState {
        val langName = prefs.getString(KEY_LANGUAGE, AppLanguage.HINDI.name) ?: AppLanguage.HINDI.name
        val language = AppLanguage.entries.find { it.name == langName } ?: AppLanguage.HINDI
        return MunnaSettingsState(
            language = language,
            spokenResponsesEnabled = prefs.getBoolean(KEY_SPOKEN_RESPONSES, true),
            speechRate = prefs.getFloat(KEY_SPEECH_RATE, 1.0f).coerceIn(0.6f, 1.5f),
            speechPitch = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f).coerceIn(0.7f, 1.4f),
            inAppFloatingMicEnabled = prefs.getBoolean(KEY_IN_APP_FLOATING_MIC, true),
            systemOverlayEnabled = prefs.getBoolean(KEY_SYSTEM_OVERLAY, false),
            notificationReaderEnabled = prefs.getBoolean(KEY_NOTIFICATION_READER, false),
            notificationReaderPaused = prefs.getBoolean(KEY_NOTIFICATION_PAUSED, false),
            hideSensitiveNotifications = prefs.getBoolean(KEY_HIDE_SENSITIVE_NOTIFS, true),
            saveCommandHistory = prefs.getBoolean(KEY_SAVE_HISTORY, true),
            showWelcomeBanner = prefs.getBoolean(KEY_SHOW_WELCOME, false)
        )
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun setSpokenResponsesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPOKEN_RESPONSES, enabled).apply()
        _settings.value = _settings.value.copy(spokenResponsesEnabled = enabled)
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.6f, 1.5f)
        prefs.edit().putFloat(KEY_SPEECH_RATE, clamped).apply()
        _settings.value = _settings.value.copy(speechRate = clamped)
    }

    fun setSpeechPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.7f, 1.4f)
        prefs.edit().putFloat(KEY_SPEECH_PITCH, clamped).apply()
        _settings.value = _settings.value.copy(speechPitch = clamped)
    }

    fun setInAppFloatingMicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IN_APP_FLOATING_MIC, enabled).apply()
        _settings.value = _settings.value.copy(inAppFloatingMicEnabled = enabled)
    }

    fun setSystemOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYSTEM_OVERLAY, enabled).apply()
        _settings.value = _settings.value.copy(systemOverlayEnabled = enabled)
    }

    fun setNotificationReaderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_READER, enabled).apply()
        _settings.value = _settings.value.copy(notificationReaderEnabled = enabled)
    }

    fun setNotificationReaderPaused(paused: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_PAUSED, paused).apply()
        _settings.value = _settings.value.copy(notificationReaderPaused = paused)
    }

    fun setHideSensitiveNotifications(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_SENSITIVE_NOTIFS, hide).apply()
        _settings.value = _settings.value.copy(hideSensitiveNotifications = hide)
    }

    fun setSaveCommandHistory(save: Boolean) {
        prefs.edit().putBoolean(KEY_SAVE_HISTORY, save).apply()
        _settings.value = _settings.value.copy(saveCommandHistory = save)
    }

    fun setShowWelcomeBanner(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_WELCOME, show).apply()
        _settings.value = _settings.value.copy(showWelcomeBanner = show)
    }

    companion object {
        private const val PREFS_NAME = "munna_ai_preferences"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_SPOKEN_RESPONSES = "spoken_responses_enabled"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_IN_APP_FLOATING_MIC = "in_app_floating_mic"
        private const val KEY_SYSTEM_OVERLAY = "system_overlay_enabled"
        private const val KEY_NOTIFICATION_READER = "notification_reader_enabled"
        private const val KEY_NOTIFICATION_PAUSED = "notification_reader_paused"
        private const val KEY_HIDE_SENSITIVE_NOTIFS = "hide_sensitive_notifications"
        private const val KEY_SAVE_HISTORY = "save_command_history"
        private const val KEY_SHOW_WELCOME = "show_welcome_banner"
    }
}
