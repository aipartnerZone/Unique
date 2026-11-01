package com.example.domain.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
     * Checks if a supported TargetApp can be resolved on the current Android device.
     */
    fun isAppAvailable(app: TargetApp): Boolean {
        for (pkg in app.primaryPackages) {
            if (packageManager.getLaunchIntentForPackage(pkg) != null) {
                return true
            }
        }
        if (app == TargetApp.FILES) {
            val docIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            if (docIntent.resolveActivity(packageManager) != null) {
                return true
            }
        }
        return false
    }

    /**
     * Attempts to launch the requested installed Android application.
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
                    return buildLaunchSuccessResult(app, language)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to start package $pkg for ${app.displayName}", e)
                }
            }
        }

        // 2. For Files, fallback to native Android System Document / File Picker UI if available
        if (app == TargetApp.FILES) {
            val docIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                if (docIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(docIntent)
                    return buildLaunchSuccessResult(app, language)
                }
            } catch (e: ActivityNotFoundException) {
                Log.w(TAG, "System document UI not available", e)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to open system files UI", e)
            }
        }

        // 3. Truthful Failure Report: App is not installed on this device/emulator
        return buildAppUnavailableResult(app, language)
    }

    private fun buildLaunchSuccessResult(app: TargetApp, language: AppLanguage): ActionExecutionResult {
        val spoken = when (language) {
            AppLanguage.HINDI -> "ठीक है भाई, ${app.displayName} खोल रहा हूँ।"
            AppLanguage.HINGLISH -> "Theek hai bhai, ${app.displayName} khol raha hoon."
            AppLanguage.ENGLISH -> "Sure brother, opening ${app.displayName} now."
        }
        return ActionExecutionResult(
            isSuccess = true,
            appLaunched = true,
            spokenResponse = spoken,
            statusSummary = "${app.displayName} खोला गया (Opened)",
            detailMessage = spoken
        )
    }

    private fun buildAppUnavailableResult(app: TargetApp, language: AppLanguage): ActionExecutionResult {
        val spoken = when (language) {
            AppLanguage.HINDI -> "माफ़ करना भाई, आपके फ़ोन में ${app.displayName} ऐप इंस्टॉल नहीं मिला।"
            AppLanguage.HINGLISH -> "Maaf karna bhai, aapke phone mein ${app.displayName} app installed nahi mila."
            AppLanguage.ENGLISH -> "Sorry, ${app.displayName} is not installed on this device."
        }
        return ActionExecutionResult(
            isSuccess = false,
            appLaunched = false,
            spokenResponse = spoken,
            statusSummary = "${app.displayName} फ़ोन में उपलब्ध नहीं है (Not Installed)",
            detailMessage = "$spoken कृपया Google Play Store से ${app.displayName} इंस्टॉल करें।"
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
