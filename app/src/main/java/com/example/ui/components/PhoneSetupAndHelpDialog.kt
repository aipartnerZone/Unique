package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.parser.TargetApp
import com.example.speech.TtsState
import com.example.ui.DevicePermissionsSnapshot
import com.example.ui.theme.MunnaCardBorder
import com.example.ui.theme.MunnaCardElevated
import com.example.ui.theme.MunnaCardNavy
import com.example.ui.theme.MunnaCyanGlow
import com.example.ui.theme.MunnaCyanSoft
import com.example.ui.theme.MunnaNavyDeep
import com.example.ui.theme.MunnaSkyBlue
import com.example.ui.theme.MunnaSuccessBg
import com.example.ui.theme.MunnaSuccessGreen
import com.example.ui.theme.MunnaTextSecondary
import com.example.ui.theme.MunnaTextWhite
import com.example.ui.theme.MunnaWarningAmber
import com.example.ui.theme.MunnaWarningBg

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhoneSetupAndHelpDialog(
    permissions: DevicePermissionsSnapshot,
    ttsState: TtsState,
    onDismiss: () -> Unit,
    onRequestMicPermission: () -> Unit,
    onStartOverlayService: () -> Boolean,
    onTestTtsVoice: () -> Unit,
    onLaunchTargetApp: (TargetApp) -> Unit,
    onExecuteCustomAppCommand: (String) -> Unit,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var selectedSection by rememberSaveable { mutableIntStateOf(initialTab) }
    var customAppInput by rememberSaveable { mutableStateOf("") }

    val readyCount = listOf(
        permissions.hasRecordAudioPermission,
        permissions.hasOverlayPermission,
        permissions.hasNotificationAccess,
        ttsState.isReady
    ).count { it }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("phone_setup_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
            border = BorderStroke(1.5.dp, MunnaSkyBlue.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MunnaRobotLogo(size = 38.dp, isAnimated = true)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "फ़ोन सेटअप और सहायता केंद्र",
                                style = MaterialTheme.typography.titleMedium,
                                color = MunnaTextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "परमिशन्स ($readyCount/4 तैयार) • कस्टम ऐप्स • APK गाइड",
                                style = MaterialTheme.typography.bodySmall,
                                color = MunnaCyanSoft
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_phone_setup_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "बंद करें (Close)",
                            tint = MunnaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { readyCount / 4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (readyCount == 4) MunnaSuccessGreen else MunnaSkyBlue,
                    trackColor = MunnaCardElevated
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Section Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        label = { Text("1. परमिशन्स") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MunnaSkyBlue,
                            selectedLabelColor = MunnaNavyDeep,
                            labelColor = MunnaTextWhite
                        ),
                        modifier = Modifier.testTag("setup_tab_permissions")
                    )
                    FilterChip(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        label = { Text("2. कोई भी ऐप खोलें") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MunnaSkyBlue,
                            selectedLabelColor = MunnaNavyDeep,
                            labelColor = MunnaTextWhite
                        ),
                        modifier = Modifier.testTag("setup_tab_custom_apps")
                    )
                    FilterChip(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        label = { Text("3. APK/बैटरी टिप्स") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MunnaSkyBlue,
                            selectedLabelColor = MunnaNavyDeep,
                            labelColor = MunnaTextWhite
                        ),
                        modifier = Modifier.testTag("setup_tab_troubleshooting")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedSection) {
                    0 -> {
                        // TAB 0: One-Stop Permissions Check & Allow
                        Text(
                            text = "अपने फ़ोन में मुन्ना AI के सभी फीचर्स चलाने के लिए नीचे दिए गए बटन से अनुमतियाँ (Permissions) चालू करें:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SetupPermissionCard(
                            icon = Icons.Default.Mic,
                            title = "1. माइक्रोफ़ोन अनुमति (Voice Commands)",
                            subtitle = if (permissions.hasRecordAudioPermission) {
                                "माइक अनुमति चालू है! आप बोलकर कमांड दे सकते हैं।"
                            } else {
                                "आपकी आवाज़ सुनने के लिए माइक की अनुमति ज़रूरी है।"
                            },
                            isGranted = permissions.hasRecordAudioPermission,
                            buttonText = if (permissions.hasRecordAudioPermission) "चालू है ✓" else "Allow करें",
                            onButtonClick = onRequestMicPermission
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SetupPermissionCard(
                            icon = Icons.Default.PictureInPictureAlt,
                            title = "2. फ़्लोटिंग माइक ओवरले (Display Over Apps)",
                            subtitle = if (permissions.hasOverlayPermission) {
                                "ओवरले अनुमति चालू है! अन्य ऐप्स के ऊपर माइक दिख सकता है।"
                            } else {
                                "अन्य ऐप्स चलाते समय भी साइड में छोटा माइक बटन दिखाने के लिए।"
                            },
                            isGranted = permissions.hasOverlayPermission,
                            buttonText = if (permissions.hasOverlayPermission) "सर्विस चलाएँ" else "सेटिंग खोलें",
                            onButtonClick = {
                                if (permissions.hasOverlayPermission) {
                                    onStartOverlayService()
                                } else {
                                    try {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SetupPermissionCard(
                            icon = Icons.Default.NotificationsActive,
                            title = "3. नोटिफिकेशन और WhatsApp संदेश रीडर",
                            subtitle = if (permissions.hasNotificationAccess) {
                                "Notification Access चालू है! मुन्ना नए संदेश पढ़कर सुना सकता है।"
                            } else {
                                "आने वाले WhatsApp/SMS नोटिफिकेशन सुनने के लिए (वैकल्पिक)।"
                            },
                            isGranted = permissions.hasNotificationAccess,
                            buttonText = if (permissions.hasNotificationAccess) "चालू है ✓" else "सेटिंग खोलें",
                            onButtonClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SetupPermissionCard(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            title = "4. हिंदी बोलने वाली आवाज़ (Hindi TTS Engine)",
                            subtitle = "स्थिति: ${ttsState.activeLocaleLabel}",
                            isGranted = ttsState.isReady,
                            buttonText = "आवाज़ सुनें",
                            onButtonClick = onTestTtsVoice
                        )
                    }

                    1 -> {
                        // TAB 1: Custom App Launcher & Extra Phone Apps
                        Text(
                            text = "यहाँ से किसी भी इंस्टॉल्ड ऐप का नाम लिखकर या नीचे दिए गए बटन दबाकर सीधे खोलें (या होम पर माइक दबाकर बोलें जैसे: \"Camera kholo\", \"Calculator kholo\"):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customAppInput,
                                onValueChange = { customAppInput = it },
                                placeholder = {
                                    Text(
                                        text = "ऐप का नाम लिखें (जैसे: Camera, Maps, Settings)",
                                        color = MunnaTextSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MunnaSkyBlue,
                                    unfocusedBorderColor = MunnaCardBorder,
                                    focusedContainerColor = MunnaNavyDeep,
                                    unfocusedContainerColor = MunnaNavyDeep,
                                    focusedTextColor = MunnaTextWhite,
                                    unfocusedTextColor = MunnaTextWhite
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        if (customAppInput.isNotBlank()) {
                                            onExecuteCustomAppCommand("${customAppInput.trim()} kholo")
                                            customAppInput = ""
                                            focusManager.clearFocus()
                                            onDismiss()
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_app_name_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (customAppInput.isNotBlank()) {
                                        onExecuteCustomAppCommand("${customAppInput.trim()} kholo")
                                        customAppInput = ""
                                        focusManager.clearFocus()
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MunnaSkyBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("launch_custom_app_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Launch,
                                    contentDescription = "खोलें",
                                    tint = MunnaNavyDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("खोलें", color = MunnaNavyDeep, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "फ़ोन के प्रमुख टूल्स और ऐप्स (1-Tap Launch):",
                            style = MaterialTheme.typography.labelLarge,
                            color = MunnaCyanSoft
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TargetApp.entries.forEach { app ->
                                val isReady = permissions.appInstallStatus[app] == true
                                Surface(
                                    onClick = {
                                        onLaunchTargetApp(app)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MunnaCardElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isReady) MunnaSkyBlue.copy(alpha = 0.55f) else MunnaCardBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Apps,
                                            contentDescription = null,
                                            tint = if (isReady) MunnaSkyBlue else MunnaTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = app.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MunnaTextWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Hindi APK Install, GitHub + Vercel Guide, Battery & Voice Troubleshooting
                        TroubleshootingGuideCard(
                            icon = Icons.Default.Download,
                            title = "📲 1. अपने फ़ोन में APK कैसे डाउनलोड और इंस्टॉल करें?",
                            bulletPoints = listOf(
                                "Google AI Studio में ऊपर दाईं ओर Settings (⚙️) या Export आइकन पर टैप करें।",
                                "'Generate APK' (या Download APK) चुनें और फ़ाइल डाउनलोड होने दें।",
                                "फ़ोन के Downloads फ़ोल्डर में जाकर APK फ़ाइल पर टैप करें।",
                                "यदि 'Install unknown apps' दिखे तो उसे Allow करें और Install दबाएँ।"
                            ),
                            actionText = null,
                            onAction = null
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TroubleshootingGuideCard(
                            icon = Icons.AutoMirrored.Filled.Launch,
                            title = "🌐 2. GitHub से Vercel 404 ठीक करें और ऑटोमैटिक APK बनाएँ:",
                            bulletPoints = listOf(
                                "इस प्रोजेक्ट में अब index.html, vercel.json और GitHub Actions (.github/workflows/build-android-apk.yml) जोड़ दिए गए हैं।",
                                "Google AI Studio में ऊपर से कोड को अपने GitHub रिपॉजिटरी में Push / Sync करें।",
                                "Push होते ही आपकी Vercel वेबसाइट (unique-public.vercel.app) पर 404 हट जाएगा और Munna AI Web App + APK Portal खुल जाएगा!",
                                "साथ ही GitHub के 'Actions' और 'Releases' टैब में 'Munna-AI.apk' अपने-आप बनकर डाउनलोड के लिए तैयार मिलेगा।"
                            ),
                            actionText = null,
                            onAction = null
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TroubleshootingGuideCard(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            title = "🔊 2. यदि मुन्ना की हिंदी आवाज़ साफ़ न सुनाई दे:",
                            bulletPoints = listOf(
                                "फ़ोन की आवाज़ (Media Volume) बढ़ाएँ।",
                                "नीचे दिए गए बटन से 'Text-to-Speech Settings' खोलें और 'Speech Recognition & Synthesis from Google' चुनकर हिंदी (India) आवाज़ डाउनलोड करें।"
                            ),
                            actionText = "Android TTS आवाज़ सेटिंग्स खोलें",
                            onAction = {
                                try {
                                    val ttsIntent = Intent("com.android.settings.TTS_SETTINGS").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(ttsIntent)
                                } catch (_: Exception) {
                                    try {
                                        context.startActivity(
                                            Intent(Settings.ACTION_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TroubleshootingGuideCard(
                            icon = Icons.Default.BatteryChargingFull,
                            title = "🔋 3. फ़्लोटिंग माइक बैकग्राउंड में बंद हो जाता है?",
                            bulletPoints = listOf(
                                "कई फ़ोन (Xiaomi, Realme, Vivo, Samsung) बैटरी बचाने के लिए बैकग्राउंड सर्विस बंद कर देते हैं।",
                                "नीचे दिए गए बटन से 'App Info' खोलें → 'Battery Usage' में जाकर 'Unrestricted' (बिना रुकावट) चुनें।"
                            ),
                            actionText = "Munna AI बैटरी / App Info खोलें",
                            onAction = {
                                try {
                                    val appInfoIntent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    context.startActivity(appInfoIntent)
                                } catch (_: Exception) {
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MunnaCardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MunnaSkyBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "समझ गया, होम स्क्रीन पर लौटें",
                        color = MunnaNavyDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupPermissionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MunnaCardElevated,
        border = BorderStroke(
            1.dp,
            if (isGranted) MunnaSuccessGreen.copy(alpha = 0.55f) else MunnaWarningAmber.copy(alpha = 0.55f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (isGranted) MunnaSuccessGreen else MunnaWarningAmber,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MunnaTextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MunnaTextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) MunnaSuccessBg else MunnaSkyBlue
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isGranted) MunnaSuccessGreen else MunnaNavyDeep,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TroubleshootingGuideCard(
    icon: ImageVector,
    title: String,
    bulletPoints: List<String>,
    actionText: String?,
    onAction: (() -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MunnaCardElevated,
        border = BorderStroke(1.dp, MunnaCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MunnaSkyBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MunnaTextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            bulletPoints.forEach { point ->
                Text(
                    text = "• $point",
                    style = MaterialTheme.typography.bodySmall,
                    color = MunnaTextSecondary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onAction,
                    border = BorderStroke(1.dp, MunnaSkyBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(actionText, color = MunnaSkyBlue, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
