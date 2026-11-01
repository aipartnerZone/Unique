package com.example.domain.action

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.AppLanguage
import com.example.data.local.UserMemoryEntity
import com.example.domain.parser.PaymentAppSecurityGuard
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

data class InstalledDeviceAppInfo(
    val label: String,
    val packageName: String,
    val isBlockedPaymentApp: Boolean
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
     * Returns all launchable apps visible on the user's device, marking any payment/banking apps as blocked.
     */
    fun getInstalledLaunchableApps(): List<InstalledDeviceAppInfo> {
        val result = mutableListOf<InstalledDeviceAppInfo>()
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
            val seenPackages = mutableSetOf<String>()
            for (info in resolveInfos) {
                val pkg = info.activityInfo?.packageName.orEmpty()
                if (pkg.isBlank() || pkg == context.packageName || !seenPackages.add(pkg)) continue
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty().ifBlank { pkg }
                val isPayment = PaymentAppSecurityGuard.isPaymentOrBankingApp(label, pkg)
                result.add(
                    InstalledDeviceAppInfo(
                        label = label,
                        packageName = pkg,
                        isBlockedPaymentApp = isPayment
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying installed launcher apps", e)
        }
        return result.sortedBy { it.label.lowercase() }
    }

    /**
     * Strictly blocks launching any Payment, UPI, Wallet, or Banking application.
     */
    fun buildBlockedPaymentAppResult(
        blockedAppName: String,
        language: AppLanguage = AppLanguage.HINDI
    ): ActionExecutionResult {
        val spoken = when (language) {
            AppLanguage.HINDI -> "सुरक्षा नियम: भाई, आपके पैसों और बैंक खाते की सुरक्षा के लिए मैं कोई भी पेमेंट या यूपीआई ऐप ($blockedAppName) नहीं खोलता हूँ। कृपया इसे स्वयं खोलें।"
            AppLanguage.HINGLISH -> "Security Rule: Bhai, aapke paiso ki suraksha ke liye main koi bhi Payment ya UPI app ($blockedAppName) nahi kholta hoon."
            AppLanguage.ENGLISH -> "Security Rule: For your financial safety, Munna AI never opens payment or banking apps ($blockedAppName)."
        }
        return ActionExecutionResult(
            isSuccess = false,
            appLaunched = false,
            spokenResponse = spoken,
            statusSummary = "🔒 पेमेंट ऐप लॉक ($blockedAppName सुरक्षित ब्लॉक)",
            detailMessage = spoken
        )
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
     * Attempts to launch ANY installed non-payment app by spoken name or package name.
     * Strictly enforces PaymentAppSecurityGuard so payment/banking apps are never opened.
     */
    fun launchCustomAppByName(
        requestedAppName: String,
        language: AppLanguage = AppLanguage.HINDI
    ): ActionExecutionResult {
        val cleanQuery = requestedAppName.trim().lowercase()

        // 1. Security Check: Never open any Payment or Banking app!
        if (PaymentAppSecurityGuard.isPaymentOrBankingApp(cleanQuery)) {
            return buildBlockedPaymentAppResult(requestedAppName.uppercase(), language)
        }

        // 2. Check if it matches any of our known TargetApp entries
        val matchedTarget = TargetApp.entries.find { target ->
            target.displayName.lowercase() == cleanQuery ||
                target.aliases.any { it == cleanQuery || cleanQuery.contains(it) }
        }
        if (matchedTarget != null) {
            return launchTargetApp(matchedTarget, language)
        }

        // 3. Dynamically search ALL installed launcher activities on the user's phone
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)

            // Prioritize exact label match first, then partial label/package match
            val matchedInfo = resolveInfos.firstOrNull { info ->
                val label = info.loadLabel(packageManager)?.toString()?.trim()?.lowercase().orEmpty()
                label == cleanQuery
            } ?: resolveInfos.firstOrNull { info ->
                val label = info.loadLabel(packageManager)?.toString()?.trim()?.lowercase().orEmpty()
                val pkg = info.activityInfo?.packageName?.lowercase().orEmpty()
                label.contains(cleanQuery) || cleanQuery.contains(label) && label.length >= 3 || pkg.contains(cleanQuery)
            }

            if (matchedInfo != null) {
                val label = matchedInfo.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                val pkg = matchedInfo.activityInfo?.packageName.orEmpty()
                val activityName = matchedInfo.activityInfo?.name.orEmpty()

                // Security Check on resolved package and label!
                if (PaymentAppSecurityGuard.isPaymentOrBankingApp(label, pkg)) {
                    return buildBlockedPaymentAppResult(label.ifBlank { requestedAppName }, language)
                }

                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                    ?: if (pkg.isNotBlank() && activityName.isNotBlank()) {
                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_LAUNCHER)
                            component = ComponentName(pkg, activityName)
                        }
                    } else null

                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return buildLaunchSuccessResult(label.ifBlank { requestedAppName }, language)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error searching custom app '$requestedAppName'", e)
        }

        return buildAppUnavailableResult(requestedAppName.replaceFirstChar { it.uppercase() }, language)
    }

    /**
     * Places a phone call using either direct CALL_PHONE (when permission granted) or Dialer (ACTION_DIAL).
     * Can match saved contact names from Munna's learned memory!
     */
    fun placeVoiceCall(
        targetNameOrNumber: String,
        extractedDigits: String,
        savedMemories: List<UserMemoryEntity>,
        language: AppLanguage = AppLanguage.HINDI
    ): ActionExecutionResult {
        val cleanTarget = targetNameOrNumber.trim()
        var resolvedDigits = extractedDigits

        // If the user spoke a contact name (like "Papa", "Mummy", "Rahul"), look up in Munna's saved contacts memory
        if (resolvedDigits.length < 3 && cleanTarget.isNotBlank()) {
            val matchedContact = savedMemories.firstOrNull { mem ->
                mem.contactPhone.isNotBlank() && (
                    mem.memoryKey.lowercase().contains(cleanTarget.lowercase()) ||
                        mem.memoryValue.lowercase().contains(cleanTarget.lowercase())
                    )
            }
            if (matchedContact != null) {
                resolvedDigits = matchedContact.contactPhone
            }
        }

        val hasDirectCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (resolvedDigits.length >= 3) {
            val telUri = Uri.parse("tel:$resolvedDigits")
            if (hasDirectCallPermission) {
                try {
                    val callIntent = Intent(Intent.ACTION_CALL, telUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(callIntent)
                    val spoken = when (language) {
                        AppLanguage.HINDI -> "ठीक है भाई, $cleanTarget ($resolvedDigits) पर सीधा कॉल लगा रहा हूँ।"
                        AppLanguage.HINGLISH -> "Theek hai bhai, $cleanTarget ($resolvedDigits) par direct call laga raha hoon."
                        AppLanguage.ENGLISH -> "Calling $cleanTarget ($resolvedDigits) now."
                    }
                    return ActionExecutionResult(
                        isSuccess = true,
                        appLaunched = true,
                        spokenResponse = spoken,
                        statusSummary = "📞 कॉल लगाया गया ($resolvedDigits)",
                        detailMessage = spoken
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Direct ACTION_CALL failed, falling back to ACTION_DIAL", e)
                }
            }

            // Fallback to opening Dialer pre-filled with the phone number
            return try {
                val dialIntent = Intent(Intent.ACTION_DIAL, telUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                val spoken = when (language) {
                    AppLanguage.HINDI -> "ठीक है भाई, $cleanTarget का नंबर ($resolvedDigits) डायलर में लगा दिया है।"
                    AppLanguage.HINGLISH -> "Theek hai bhai, $cleanTarget ka number ($resolvedDigits) dialer mein laga diya hai."
                    AppLanguage.ENGLISH -> "Opened dialer with $resolvedDigits ready to call."
                }
                ActionExecutionResult(
                    isSuccess = true,
                    appLaunched = true,
                    spokenResponse = spoken,
                    statusSummary = "📞 डायलर खोला गया ($resolvedDigits)",
                    detailMessage = "$spoken (सीधा कॉल लगाने के लिए सेटअप में 'Direct Call Permission' चालू करें)"
                )
            } catch (e: Exception) {
                ActionExecutionResult(
                    isSuccess = false,
                    spokenResponse = "माफ़ करना भाई, इस डिवाइस पर कॉल डायलर उपलब्ध नहीं है।",
                    statusSummary = "कॉल सुविधा अनुपलब्ध",
                    detailMessage = "इस डिवाइस पर फ़ोन डायलर नहीं मिला।"
                )
            }
        } else {
            // User said "Rahul ko call karo" without a saved number — open Dialer and tell user how to save the contact
            return try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                val spoken = "भाई, मैंने कॉल डायलर खोल दिया है। यदि आप बोलें कि '$cleanTarget का नंबर 98... है', तो अगली बार मैं नाम सुनते ही सीधा कॉल लगा दूँगा!"
                ActionExecutionResult(
                    isSuccess = true,
                    appLaunched = true,
                    spokenResponse = spoken,
                    statusSummary = "📞 डायलर खोला गया ($cleanTarget का नंबर सेव करें)",
                    detailMessage = spoken
                )
            } catch (e: Exception) {
                ActionExecutionResult(
                    isSuccess = false,
                    spokenResponse = "भाई, $cleanTarget को कॉल करने के लिए कृपया नंबर बोलें (जैसे: '9876543210 पर कॉल करो')।",
                    statusSummary = "नंबर आवश्यक है",
                    detailMessage = "कृपया नंबर बोलें या एजेंट मेमोरी में संपर्क नंबर सेव करें।"
                )
            }
        }
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
            AppLanguage.HINDI -> "हाँ भाई, मैं मुन्ना ध्यान से सुन रहा हूँ! मैं कोई भी ऐप खोल सकता हूँ (पेमेंट ऐप छोड़कर), कॉल लगा सकता हूँ, PDF पढ़ सकता हूँ और आपके साथ पढ़ाई या डिस्कशन कर सकता हूँ!"
            AppLanguage.HINGLISH -> "Haan bhai, main Munna dhyan se sun raha hoon! Batao, kaunsa app kholna hai, call lagana hai, PDF padhna hai ya padhai/discuss karna hai?"
            AppLanguage.ENGLISH -> "Yes brother, Munna is listening! I can open any non-payment app, make calls, read PDFs, or study and discuss with you."
        }
        return ActionExecutionResult(
            isSuccess = true,
            spokenResponse = spoken,
            statusSummary = "मुन्ना एजेंट तैयार है (Ready)",
            detailMessage = spoken
        )
    }

    companion object {
        private const val TAG = "AppActionHandler"
    }
}
