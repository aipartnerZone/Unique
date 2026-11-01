package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.MunnaDatabase
import com.example.data.local.MunnaPreferences
import com.example.data.local.MunnaRepository
import com.example.domain.action.AppActionHandler
import com.example.domain.parser.MunnaCommandParser
import com.example.domain.parser.ParsedCommand
import com.example.speech.MicStatus
import com.example.speech.MunnaTtsManager
import com.example.speech.MunnaVoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Native Android Background Agent & Floating Microphone Service (`TYPE_APPLICATION_OVERLAY`).
 *
 * - Runs in the background so the user can give voice commands from any screen.
 * - Opens any installed non-payment app, strictly blocks payment/banking apps, places voice calls,
 *   tells time/date, and routes study/discussion/PDF queries to Munna AI.
 */
class FloatingMicService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var overlayRootView: View? = null
    private var statusBadgeView: TextView? = null
    private var micCircleContainer: FrameLayout? = null

    private lateinit var preferences: MunnaPreferences
    private lateinit var repository: MunnaRepository
    private lateinit var actionHandler: AppActionHandler
    private var ttsManager: MunnaTtsManager? = null
    private var voiceManager: MunnaVoiceManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        preferences = MunnaPreferences(applicationContext)
        repository = MunnaRepository(MunnaDatabase.getInstance(applicationContext).munnaDao())
        actionHandler = AppActionHandler(applicationContext)
        ttsManager = MunnaTtsManager(applicationContext)
        voiceManager = MunnaVoiceManager(applicationContext) { recognizedText ->
            handleOverlayVoiceCommand(recognizedText)
        }

        serviceScope.launch {
            voiceManager?.voiceState?.collect { state ->
                updateOverlayVisualState(state.status)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_FLOATING_MIC) {
            preferences.setSystemOverlayEnabled(false)
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_LISTEN_NOW) {
            onFloatingMicTapped()
            return START_STICKY
        }

        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Overlay permission not granted; stopping FloatingMicService.")
            preferences.setSystemOverlayEnabled(false)
            _isRunning.value = false
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        showOverlayIfNeeded()
        preferences.setSystemOverlayEnabled(true)
        _isRunning.value = true

        return START_STICKY
    }

    private fun showOverlayIfNeeded() {
        if (overlayRootView != null) return

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dpToPx(16)
            y = dpToPx(260)
        }

        val rootContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
        }

        val statusText = TextView(this).apply {
            text = "मुन्ना एजेंट"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.parseColor("#E60A192F"))
                setStroke(dpToPx(1), Color.parseColor("#38BDF8"))
            }
        }
        statusBadgeView = statusText

        val micSize = dpToPx(58)
        val micFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(micSize, micSize).apply {
                topMargin = dpToPx(4)
                bottomMargin = dpToPx(4)
            }
            background = buildMicCircleDrawable(isActive = false)
            elevation = dpToPx(8).toFloat()
        }
        micCircleContainer = micFrame

        val micIcon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            setColorFilter(Color.WHITE)
            val iconPad = dpToPx(14)
            setPadding(iconPad, iconPad, iconPad, iconPad)
        }
        micFrame.addView(
            micIcon,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )

        val closeBtn = TextView(this).apply {
            text = "✕ बंद"
            setTextColor(Color.parseColor("#CBD5E1"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setPadding(dpToPx(7), dpToPx(2), dpToPx(7), dpToPx(2))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.parseColor("#CC0F1F38"))
            }
            setOnClickListener {
                preferences.setSystemOverlayEnabled(false)
                stopSelf()
            }
        }

        rootContainer.addView(statusText)
        rootContainer.addView(micFrame)
        rootContainer.addView(closeBtn)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        micFrame.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (initialTouchX - event.rawX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > dpToPx(6) || abs(dy) > dpToPx(6)) {
                        isDragging = true
                        params.x = (initialX + dx).coerceAtLeast(0)
                        params.y = (initialY + dy).coerceAtLeast(dpToPx(48))
                        try {
                            wm.updateViewLayout(rootContainer, params)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error updating floating overlay position", e)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        onFloatingMicTapped()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            wm.addView(rootContainer, params)
            overlayRootView = rootContainer
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay view to WindowManager", e)
            _isRunning.value = false
            stopSelf()
        }
    }

    private fun onFloatingMicTapped() {
        val currentStatus = voiceManager?.voiceState?.value?.status ?: MicStatus.IDLE
        if (currentStatus == MicStatus.LISTENING) {
            voiceManager?.cancelListening()
        } else {
            ttsManager?.stop()
            val langTag = preferences.settings.value.language.speechLocaleTag
            voiceManager?.startListening(langTag)
        }
    }

    private fun handleOverlayVoiceCommand(recognizedText: String) {
        serviceScope.launch {
            val settings = preferences.settings.value

            // Auto-learn personal facts even from background voice commands
            if (settings.agentLearningEnabled) {
                MunnaCommandParser.extractLearnedMemoryIfAny(recognizedText)?.let { learned ->
                    repository.saveLearnedMemory(
                        category = learned.category,
                        memoryKey = learned.memoryKey,
                        memoryValue = learned.memoryValue,
                        contactPhone = learned.contactPhone
                    )
                }
            }

            val parsed = MunnaCommandParser.parse(recognizedText)
            val result = when (parsed) {
                is ParsedCommand.BlockedPaymentApp -> {
                    actionHandler.buildBlockedPaymentAppResult(parsed.blockedAppName, settings.language)
                }
                is ParsedCommand.OpenApp -> {
                    actionHandler.launchTargetApp(parsed.targetApp, settings.language)
                }
                is ParsedCommand.OpenCustomAppByName -> {
                    actionHandler.launchCustomAppByName(parsed.requestedAppName, settings.language)
                }
                is ParsedCommand.MakePhoneCall -> {
                    val memories = repository.getMemoriesSnapshot()
                    actionHandler.placeVoiceCall(
                        targetNameOrNumber = parsed.targetNameOrNumber,
                        extractedDigits = parsed.extractedDigits,
                        savedMemories = memories,
                        language = settings.language
                    )
                }
                is ParsedCommand.TellTime -> actionHandler.getCurrentTimeResponse(settings.language)
                is ParsedCommand.TellDate -> actionHandler.getCurrentDateResponse(settings.language)
                is ParsedCommand.GreetingOrListen -> actionHandler.getGreetingResponse(settings.language)
                else -> {
                    val openAppIntent = Intent(this@FloatingMicService, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra(EXTRA_VOICE_QUERY, recognizedText)
                    }
                    startActivity(openAppIntent)
                    com.example.domain.action.ActionExecutionResult(
                        isSuccess = true,
                        spokenResponse = "ठीक है भाई, मुन्ना एजेंट में जवाब और चर्चा खोल रहा हूँ।",
                        statusSummary = "मुन्ना ऐप में खोला गया",
                        detailMessage = recognizedText
                    )
                }
            }

            if (settings.spokenResponsesEnabled) {
                ttsManager?.speak(
                    text = result.spokenResponse,
                    enabled = true,
                    speechRate = settings.speechRate,
                    pitch = settings.speechPitch
                )
            }

            if (settings.saveCommandHistory) {
                repository.logCommand(
                    recognizedText = recognizedText,
                    normalizedCommand = parsed.normalizedText,
                    intentCategory = parsed.categoryLabel,
                    actionStatus = result.statusSummary,
                    spokenResponse = result.spokenResponse,
                    isSuccess = result.isSuccess
                )
            }
        }
    }

    private fun updateOverlayVisualState(status: MicStatus) {
        val badge = statusBadgeView ?: return
        val circle = micCircleContainer ?: return

        when (status) {
            MicStatus.LISTENING -> {
                badge.text = "सुन रहा हूँ…"
                circle.background = buildMicCircleDrawable(isActive = true)
            }
            MicStatus.PROCESSING -> {
                badge.text = "समझ रहा हूँ…"
                circle.background = buildMicCircleDrawable(isActive = true)
            }
            MicStatus.COMPLETED -> {
                badge.text = "काम पूरा!"
                circle.background = buildMicCircleDrawable(isActive = false)
            }
            MicStatus.ERROR -> {
                badge.text = "दोबारा बोलें"
                circle.background = buildMicCircleDrawable(isActive = false)
            }
            MicStatus.IDLE -> {
                badge.text = "मुन्ना एजेंट"
                circle.background = buildMicCircleDrawable(isActive = false)
            }
        }
    }

    private fun buildMicCircleDrawable(isActive: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(if (isActive) Color.parseColor("#0284C7") else Color.parseColor("#0EA5E9"))
            setStroke(
                dpToPx(if (isActive) 4 else 3),
                if (isActive) Color.parseColor("#67E8F9") else Color.parseColor("#38BDF8")
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.floating_mic_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Munna AI Background Agent & Floating Microphone Service"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val listenIntent = Intent(this, FloatingMicService::class.java).apply {
            action = ACTION_LISTEN_NOW
        }
        val listenPendingIntent = PendingIntent.getService(
            this,
            2,
            listenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FloatingMicService::class.java).apply {
            action = ACTION_STOP_FLOATING_MIC
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Munna AI बैकग्राउंड एजेंट सक्रिय है")
            .setContentText("बोलकर कोई भी नॉन-पेमेंट ऐप खोलें या कॉल लगाएँ")
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_btn_speak_now,
                "🎙️ बोलें (Listen)",
                listenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "बंद करें (Stop)",
                stopPendingIntent
            )
            .build()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroy() {
        _isRunning.value = false
        try {
            overlayRootView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Error removing overlay view", e)
        }
        overlayRootView = null
        voiceManager?.destroy()
        ttsManager?.shutdown()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "FloatingMicService"
        private const val CHANNEL_ID = "munna_floating_mic_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_FLOATING_MIC = "com.example.action.STOP_FLOATING_MIC"
        const val ACTION_LISTEN_NOW = "com.example.action.LISTEN_NOW_FLOATING_MIC"
        const val EXTRA_VOICE_QUERY = "extra_voice_query"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    }
}
