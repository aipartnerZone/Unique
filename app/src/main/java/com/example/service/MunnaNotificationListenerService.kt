package com.example.service

import android.app.Notification
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.local.MunnaPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReadableNotificationItem(
    val id: String,
    val packageName: String,
    val appDisplayName: String,
    val senderOrTitle: String,
    val contentSummary: String,
    val isSensitiveMasked: Boolean,
    val timestamp: Long
)

/**
 * Permission-based Notification Listener module (Requirement 9).
 *
 * Privacy & Security Guarantees:
 * - Only works when the user explicitly grants Notification Access in Android Settings.
 * - Does NOT bypass WhatsApp security or access private message databases.
 * - Does NOT send notification contents to any AI server automatically.
 * - Detects and masks sensitive OTP / password / bank notifications automatically.
 * - Respects the user's Enable/Disable and Pause toggles in MunnaPreferences.
 */
class MunnaNotificationListenerService : NotificationListenerService() {

    private lateinit var preferences: MunnaPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = MunnaPreferences(applicationContext)
        _isConnected.value = true
    }

    override fun onDestroy() {
        _isConnected.value = false
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        val settings = preferences.settings.value
        if (!settings.notificationReaderEnabled || settings.notificationReaderPaused) {
            return
        }

        val pkg = notification.packageName ?: return
        // Ignore our own foreground service notification
        if (pkg == applicationContext.packageName) return

        val extras = notification.notification?.extras ?: return
        val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()

        if (rawTitle.isBlank() && rawText.isBlank()) return

        val appName = resolveFriendlyAppName(pkg)
        val isSensitive = settings.hideSensitiveNotifications && containsSensitiveKeywords("$rawTitle $rawText")

        val safeContent = if (isSensitive) {
            "[संवेदनशील OTP/सुरक्षा संदेश छिपाया गया - गोपनीयता सुरक्षित]"
        } else {
            rawText.take(160)
        }

        val item = ReadableNotificationItem(
            id = "${pkg}_${notification.postTime}",
            packageName = pkg,
            appDisplayName = appName,
            senderOrTitle = rawTitle.ifBlank { appName },
            contentSummary = safeContent,
            isSensitiveMasked = isSensitive,
            timestamp = notification.postTime
        )

        val updated = (listOf(item) + _recentNotifications.value)
            .distinctBy { "${it.senderOrTitle}_${it.contentSummary}" }
            .take(15)
        _recentNotifications.value = updated
    }

    private fun resolveFriendlyAppName(pkg: String): String {
        return when {
            pkg.contains("whatsapp") -> "WhatsApp"
            pkg.contains("youtube") -> "YouTube"
            pkg.contains("messaging") || pkg.contains("mms") || pkg.contains("sms") -> "Messages (SMS)"
            pkg.contains("gmail") || pkg.contains("mail") -> "Email"
            pkg.contains("telegram") -> "Telegram"
            else -> try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                Log.w(TAG, "Could not resolve label for $pkg", e)
                pkg.substringAfterLast('.')
            }
        }
    }

    private fun containsSensitiveKeywords(text: String): Boolean {
        val lower = text.lowercase()
        val sensitivePatterns = listOf(
            "otp", "one time password", "verification code", "security code",
            "passcode", "password", "cvv", "pin", "ओटीपी", "पासवर्ड", "वेरिफिकेशन कोड"
        )
        return sensitivePatterns.any { lower.contains(it) }
    }

    companion object {
        private const val TAG = "MunnaNotifService"

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _recentNotifications = MutableStateFlow<List<ReadableNotificationItem>>(emptyList())
        val recentNotifications: StateFlow<List<ReadableNotificationItem>> = _recentNotifications.asStateFlow()

        fun clearCapturedNotifications() {
            _recentNotifications.value = emptyList()
        }

        fun isNotificationAccessGranted(context: Context): Boolean {
            return try {
                val enabledListeners = Settings.Secure.getString(
                    context.contentResolver,
                    "enabled_notification_listeners"
                ) ?: return false
                enabledListeners.contains(context.packageName)
            } catch (e: Exception) {
                false
            }
        }
    }
}
