package com.example.domain.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import com.example.data.local.AppLanguage
import com.example.domain.parser.TargetApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActionExecutionResult(
    val isSuccess: Boolean,
    val appLaunched: Boolean = false,
    val spokenResponse: String,
    val statusSummary: String,
    val detailMessage: String
)

class AppActionHandler(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Checks if a supported TargetApp or its corresponding Android System Action can be resolved.
     */
    fun isAppAvailable(app: TargetApp): Boolean {
        for (pkg in app.primaryPackages) {
            if (packageManager.getLaunchIntentForPackage(pkg) != null) {
                return true
            }
        }
        val fallbackIntent = buildSystemFallbackIntent(app)
        if (fallbackIntent != null && fallbackIntent.resolveActivity(packageManager) != null) {
            return true
        }
        return false
    }

    /**
     * Attempts to launch the requested installed Android application or system utility.
     * NEVER reports that an app opened unless startActivity() actually succeeded.
     */
    fun launchTargetApp(app: TargetApp, language: AppLanguage = AppLanguage.HINDI): ActionExecutionResult {
        // 1. Try explicit package launch intents first
        for (pkg in app.primaryPackages) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(launchIntent)
                    return buildLaunchSuccessResult(app.displayName, language)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to start package $pkg for ${app.displayName}", e)
                }
            }
        }

        // 2. Try standard Android System Action / Category fallback intents
        val fallbackIntent = buildSystemFallbackIntent(app)
        if (fallbackIntent != null) {
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(fallbackIntent)
                return buildLaunchSuccessResult(app.displayName, language)
            } catch (e: ActivityNotFoundException) {
                Log.w(TAG, "System fallback intent not found for ${app.displayName}", e)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start system fallback intent for ${app.displayName}", e)
            }
        }

        // 3. Truthful Failure Report: App is not installed on this device/emulator
        return buildAppUnavailableResult(app.displayName, language)
    }

    /**
     * Attempts to launch a custom app by name from visible launcher activities.
     * Truthfully reports if the requested app is not installed or not visible.
     */
    fun launchCustomAppByName(
        requestedAppName: String,
        language: AppLanguage = AppLanguage.HINDI
    ): ActionExecutionResult {
        val cleanQuery = requestedAppName.trim().lowercase()

        // First check if it matches any of our TargetApp entries
        val matchedTarget = TargetApp.entries.find { target ->
            target.displayName.lowercase().contains(cleanQuery) ||
                target.aliases.any { it.contains(cleanQuery) || cleanQuery.contains(it) }
        }
        if (matchedTarget != null) {
            return launchTargetApp(matchedTarget, language)
        }

        // Search visible launcher activities on the device
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
            for (info in resolveInfos) {
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                val pkg = info.activityInfo?.packageName.orEmpty()
                if (label.lowercase().contains(cleanQuery) || pkg.lowercase().contains(cleanQuery)) {
                    val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return buildLaunchSuccessResult(label.ifBlank { requestedAppName }, language)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error searching custom app '$requestedAppName'", e)
        }

        return buildAppUnavailableResult(requestedAppName.replaceFirstChar { it.uppercase() }, language)
    }

    private fun buildSystemFallbackIntent(app: TargetApp): Intent? {
        return when (app) {
            TargetApp.FILES -> Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            TargetApp.CAMERA -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            TargetApp.SETTINGS -> Intent(Settings.ACTION_SETTINGS)
            TargetApp.DIALER -> Intent(Intent.ACTION_DIAL)
            TargetApp.CALCULATOR -> Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_CALCULATOR
            )
            TargetApp.MAPS -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
            TargetApp.GALLERY -> Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_GALLERY
            )
            TargetApp.CLOCK -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            TargetApp.PLAY_STORE -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store")
            )
            TargetApp.GMAIL -> Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_EMAIL
            )
            else -> null
        }
    }

    private fun buildLaunchSuccessResult(appName: String, language: AppLanguage): ActionExecutionResult {
        val spoken = when (language) {
            AppLanguage.HINDI -> "ठीक है भाई, $appName खोल रहा हूँ।"
            AppLanguage.HINGLISH -> "Theek hai bhai, $appName khol raha hoon."
            AppLanguage.ENGLISH -> "Sure brother, opening $appName now."
        }
        return ActionExecutionResult(
            isSuccess = true,
            appLaunched = true,
            spokenResponse = spoken,
            statusSummary = "$appName खोला गया (Opened)",
            detailMessage = spoken
        )
    }

    private fun buildAppUnavailableResult(appName: String, language: AppLanguage): ActionExecutionResult {
        val spoken = when (language) {
            AppLanguage.HINDI -> "माफ़ करना भाई, आपके फ़ोन में $appName ऐप इंस्टॉल नहीं मिला।"
            AppLanguage.HINGLISH -> "Maaf karna bhai, aapke phone mein $appName app installed nahi mila."
            AppLanguage.ENGLISH -> "Sorry, $appName is not installed on this device."
        }
        return ActionExecutionResult(
            isSuccess = false,
            appLaunched = false,
            spokenResponse = spoken,
            statusSummary = "$appName फ़ोन में उपलब्ध नहीं है (Not Installed)",
            detailMessage = "$spoken कृपया Google Play Store से $appName इंस्टॉल करें।"
        )
    }

    fun getCurrentTimeResponse(language: AppLanguage = AppLanguage.HINDI): ActionExecutionResult {
        val now = Date()
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.forLanguageTag("hi-IN"))
        val formattedTime = timeFormatter.format(now)

        val spoken = when (language) {
            AppLanguage.HINDI -> "भाई, अभी समय $formattedTime हुआ है।"
            AppLanguage.HINGLISH -> "Bhai, abhi time $formattedTime hua hai."
            AppLanguage.ENGLISH -> "Right now, the time is $formattedTime."
        }
        return ActionExecutionResult(
            isSuccess = true,
            spokenResponse = spoken,
            statusSummary = "समय बताया गया: $formattedTime",
            detailMessage = spoken
        )
    }

    fun getCurrentDateResponse(language: AppLanguage = AppLanguage.HINDI): ActionExecutionResult {
        val now = Date()
        val dateFormatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("hi-IN"))
        val formattedDate = dateFormatter.format(now)

        val spoken = when (language) {
            AppLanguage.HINDI -> "भाई, आज $formattedDate है।"
            AppLanguage.HINGLISH -> "Bhai, aaj $formattedDate hai."
            AppLanguage.ENGLISH -> "Today is $formattedDate."
        }
        return ActionExecutionResult(
            isSuccess = true,
            spokenResponse = spoken,
            statusSummary = "तारीख बताई गई: $formattedDate",
            detailMessage = spoken
        )
    }

    fun getGreetingResponse(language: AppLanguage = AppLanguage.HINDI): ActionExecutionResult {
        val spoken = when (language) {
            AppLanguage.HINDI -> "हाँ भाई, मैं मुन्ना ध्यान से सुन रहा हूँ! बताओ, कौन सा ऐप खोलना है या क्या सवाल पूछना है?"
            AppLanguage.HINGLISH -> "Haan bhai, main Munna dhyan se sun raha hoon! Batao, kya kaam karna hai?"
            AppLanguage.ENGLISH -> "Yes brother, Munna is listening! Tell me which app to open or ask me anything."
        }
        return ActionExecutionResult(
            isSuccess = true,
            spokenResponse = spoken,
            statusSummary = "मुन्ना तैयार है (Ready)",
            detailMessage = spoken
        )
    }

    companion object {
        private const val TAG = "AppActionHandler"
    }
}
