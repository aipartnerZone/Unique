package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppLanguage
import com.example.data.local.MunnaPreferences
import com.example.data.local.MunnaSettingsState
import com.example.service.ReadableNotificationItem
import com.example.speech.TtsState
import com.example.ui.DevicePermissionsSnapshot
import com.example.ui.theme.MunnaCardBorder
import com.example.ui.theme.MunnaCardElevated
import com.example.ui.theme.MunnaCardNavy
import com.example.ui.theme.MunnaCyanGlow
import com.example.ui.theme.MunnaCyanSoft
import com.example.ui.theme.MunnaErrorRed
import com.example.ui.theme.MunnaNavyDeep
import com.example.ui.theme.MunnaSkyBlue
import com.example.ui.theme.MunnaSuccessGreen
import com.example.ui.theme.MunnaTextSecondary
import com.example.ui.theme.MunnaTextWhite
import com.example.ui.theme.MunnaWarningAmber

@Composable
fun SettingsScreen(
    settings: MunnaSettingsState,
    preferences: MunnaPreferences,
    permissions: DevicePermissionsSnapshot,
    ttsState: TtsState,
    isOverlayServiceRunning: Boolean,
    recentNotifications: List<ReadableNotificationItem>,
    onRequestMicPermission: () -> Unit,
    onToggleSystemOverlay: (Boolean) -> Boolean,
    onTestVoice: () -> Unit,
    onSpeakNotification: (ReadableNotificationItem) -> Unit,
    onClearCommandHistory: () -> Unit,
    onRefreshPermissions: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MunnaNavyDeep, Color(0xFF0A1830), MunnaNavyDeep)
                )
            )
            .testTag("settings_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Surface(
            color = MunnaCardNavy,
            border = BorderStroke(1.dp, MunnaCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MunnaSkyBlue,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "सेटिंग्स और कंट्रोल (Settings)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MunnaTextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "भाषा, आवाज़, फ़्लोटिंग माइक और अनुमतियाँ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaCyanSoft
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRefreshPermissions,
                    border = BorderStroke(1.dp, MunnaSkyBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("रिफ्रेश", style = MaterialTheme.typography.labelMedium, color = MunnaSkyBlue)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. App Language: Hindi / Hinglish / English
            item {
                SettingsSectionCard(
                    title = "ऐप और कमांड की भाषा (App Language)",
                    icon = Icons.Default.Language
                ) {
                    Text(
                        text = "मुन्ना आपके कमांड और जवाबों के लिए इस भाषा को प्राथमिकता देगा:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MunnaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = settings.language == lang,
                                onClick = { preferences.setLanguage(lang) },
                                label = { Text(lang.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MunnaSkyBlue,
                                    selectedLabelColor = MunnaNavyDeep,
                                    labelColor = MunnaTextWhite
                                ),
                                modifier = Modifier.testTag("lang_chip_${lang.code}")
                            )
                        }
                    }
                }
            }

            // 2. Assistant Voice & Speech Rate + Spoken Responses Toggle
            item {
                SettingsSectionCard(
                    title = "मुन्ना की आवाज़ (Voice & TTS)",
                    icon = Icons.AutoMirrored.Filled.VolumeUp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "बोलकर जवाब दें (Spoken Responses)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MunnaTextWhite
                            )
                            Text(
                                text = "TTS स्थिति: ${ttsState.activeLocaleLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (ttsState.isReady) MunnaSuccessGreen else MunnaWarningAmber
                            )
                        }
                        Switch(
                            checked = settings.spokenResponsesEnabled,
                            onCheckedChange = { preferences.setSpokenResponsesEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MunnaNavyDeep,
                                checkedTrackColor = MunnaSkyBlue
                            ),
                            modifier = Modifier.testTag("toggle_spoken_responses")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "बोलने की गति (Speech Rate): ${"%.1f".format(settings.speechRate)}x",
                        style = MaterialTheme.typography.labelLarge,
                        color = MunnaCyanSoft
                    )
                    Slider(
                        value = settings.speechRate,
                        onValueChange = { preferences.setSpeechRate(it) },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = MunnaSkyBlue,
                            activeTrackColor = MunnaCyanGlow
                        )
                    )

                    Button(
                        onClick = onTestVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = MunnaCardElevated),
                        border = BorderStroke(1.dp, MunnaSkyBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("test_tts_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = MunnaSkyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("मुन्ना की आवाज़ टेस्ट करें", color = MunnaTextWhite)
                    }
                }
            }

            // 3. Floating Microphone Toggle (In-App + System-Wide Native Android Overlay)
            item {
                SettingsSectionCard(
                    title = "फ़्लोटिंग माइक (Floating Microphone)",
                    icon = Icons.Default.PictureInPictureAlt
                ) {
                    // In-App Floating Mic Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ऐप के अंदर फ़्लोटिंग माइक बटन",
                                style = MaterialTheme.typography.titleSmall,
                                color = MunnaTextWhite
                            )
                            Text(
                                text = "स्क्रीन के दाएँ किनारे पर नीला ड्रैगेबल माइक बटन दिखाएँ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MunnaTextSecondary
                            )
                        }
                        Switch(
                            checked = settings.inAppFloatingMicEnabled,
                            onCheckedChange = { preferences.setInAppFloatingMicEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MunnaNavyDeep,
                                checkedTrackColor = MunnaSkyBlue
                            ),
                            modifier = Modifier.testTag("toggle_in_app_floating_mic")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MunnaCardBorder
                    )

                    // System-Wide Overlay Service Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "अन्य ऐप्स के ऊपर फ़्लोटिंग माइक (System Overlay)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MunnaTextWhite
                            )
                            Text(
                                text = if (permissions.hasOverlayPermission) {
                                    if (isOverlayServiceRunning) "ओवरले सर्विस अभी चालू है"
                                    else "ओवरले अनुमति प्राप्त है (चालू करने के लिए टॉगल करें)"
                                } else {
                                    "इसके लिए Android की 'Display over other apps' अनुमति आवश्यक है"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (permissions.hasOverlayPermission) MunnaSuccessGreen else MunnaWarningAmber
                            )
                        }
                        Switch(
                            checked = isOverlayServiceRunning || (settings.systemOverlayEnabled && permissions.hasOverlayPermission),
                            onCheckedChange = { wantEnable ->
                                val started = onToggleSystemOverlay(wantEnable)
                                if (!started && wantEnable) {
                                    // Launch Android Overlay Permission Screen
                                    try {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MunnaNavyDeep,
                                checkedTrackColor = MunnaSkyBlue
                            ),
                            modifier = Modifier.testTag("toggle_system_overlay_mic")
                        )
                    }

                    if (!permissions.hasOverlayPermission) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            },
                            border = BorderStroke(1.dp, MunnaSkyBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_overlay_permission_settings")
                        ) {
                            Text("Android ओवरले अनुमति खोलें", color = MunnaSkyBlue)
                        }
                    }
                }
            }

            // 4. "Hey Munna" Wake-Word Setup Status (Requirement 7: Truthfully Disabled + Working Alternative)
            item {
                SettingsSectionCard(
                    title = "“Hey Munna” वेक-वर्ड स्थिति (Wake Word)",
                    icon = Icons.Default.RecordVoiceOver
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MunnaCardElevated,
                        border = BorderStroke(1.dp, MunnaWarningAmber.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = MunnaWarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "स्थिति: अभी निष्क्रिय है (Disabled — Phase 5 Future Feature)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MunnaWarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "मुन्ना बिना आपकी जानकारी के बैकग्राउंड में लगातार माइक रिकॉर्डिंग नहीं करता।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MunnaTextWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• क्यों बंद रखा गया है: Android 14+ में हमेशा चालू वेक-वर्ड के लिए लो-पावर ऑन-डिवाइस मॉडल (जैसे Porcupine / TFLite Micro) और VoiceInteractionService आवश्यक होता है ताकि बैटरी खत्म न हो और आपकी निजता सुरक्षित रहे।\n• काम करने वाला विकल्प: तब तक आप 'फ़्लोटिंग माइक' बटन का उपयोग करें — एक टैप में मुन्ना तुरंत सुनने लगता है!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MunnaTextSecondary
                    )
                }
            }

            // 5. Notification & Message Reading Module (Requirement 9)
            item {
                SettingsSectionCard(
                    title = "नोटिफिकेशन और WhatsApp संदेश रीडर",
                    icon = Icons.Default.NotificationsActive
                ) {
                    Text(
                        text = "अनुमति का अर्थ: यदि आप Notification Access चालू करते हैं, तो मुन्ना आपके फ़ोन के स्टेटस बार पर आने वाले नए WhatsApp या SMS नोटिफिकेशन को बोलकर सुना सकता है। मुन्ना कभी भी किसी निजी चैट डेटाबेस को नहीं खोलता और न ही आपके संदेश किसी AI सर्वर को भेजता है।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MunnaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "नोटिफिकेशन पढ़ना चालू करें",
                                style = MaterialTheme.typography.titleSmall,
                                color = MunnaTextWhite
                            )
                            Text(
                                text = if (permissions.hasNotificationAccess) {
                                    "Android Notification Access: प्राप्त है"
                                } else {
                                    "Android सेटिंग्स में Notification Access आवश्यक है"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (permissions.hasNotificationAccess) MunnaSuccessGreen else MunnaWarningAmber
                            )
                        }
                        Switch(
                            checked = settings.notificationReaderEnabled,
                            onCheckedChange = { preferences.setNotificationReaderEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MunnaNavyDeep,
                                checkedTrackColor = MunnaSkyBlue
                            ),
                            modifier = Modifier.testTag("toggle_notification_reader")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "अस्थायी रूप से रोकें (Pause Reading)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MunnaTextWhite
                        )
                        Switch(
                            checked = settings.notificationReaderPaused,
                            onCheckedChange = { preferences.setNotificationReaderPaused(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MunnaNavyDeep,
                                checkedTrackColor = MunnaWarningAmber
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "संवेदनशील OTP/पासवर्ड संदेश छुपाएँ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MunnaTextWhite
                        )
                        Switch(
                            checked = settings.hideSensitiveNotifications,
                            onCheckedChange = { preferences.setHideSensitiveNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MunnaNavyDeep,
                                checkedTrackColor = MunnaSuccessGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        border = BorderStroke(1.dp, MunnaSkyBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("open_notification_access_settings")
                    ) {
                        Text("Android Notification Access सेटिंग्स खोलें", color = MunnaSkyBlue)
                    }

                    if (recentNotifications.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "हाल के प्राप्त नोटिफिकेशन (${recentNotifications.size}):",
                            style = MaterialTheme.typography.labelLarge,
                            color = MunnaCyanSoft
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        recentNotifications.take(3).forEach { notif ->
                            Surface(
                                onClick = { onSpeakNotification(notif) },
                                shape = RoundedCornerShape(10.dp),
                                color = MunnaCardElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${notif.appDisplayName} • ${notif.senderOrTitle}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MunnaSkyBlue
                                        )
                                        Text(
                                            text = notif.contentSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MunnaTextWhite
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "पढ़कर सुनाएँ",
                                        tint = MunnaCyanSoft
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Device Permissions Status Overview
            item {
                SettingsSectionCard(
                    title = "डिवाइस अनुमतियों की वास्तविक स्थिति",
                    icon = Icons.Default.Security
                ) {
                    PermissionStatusRow(
                        label = "माइक्रोफ़ोन अनुमति (RECORD_AUDIO)",
                        isGranted = permissions.hasRecordAudioPermission,
                        actionLabel = if (!permissions.hasRecordAudioPermission) "अनुमति दें" else null,
                        onActionClick = onRequestMicPermission
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionStatusRow(
                        label = "फ़्लोटिंग ओवरले (SYSTEM_ALERT_WINDOW)",
                        isGranted = permissions.hasOverlayPermission
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionStatusRow(
                        label = "नोटिफिकेशन एक्सेस (Notification Listener)",
                        isGranted = permissions.hasNotificationAccess
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionStatusRow(
                        label = "हिंदी Text-to-Speech (TTS Engine)",
                        isGranted = ttsState.isReady
                    )
                }
            }

            // 7. Privacy Information & Clear History
            item {
                SettingsSectionCard(
                    title = "गोपनीयता और डेटा नियंत्रण (Privacy)",
                    icon = Icons.Default.PrivacyTip
                ) {
                    Text(
                        text = "• मुन्ना AI आपकी कोई भी कच्ची आवाज़ (Audio Recording) कभी सेव नहीं करता।\n• YouTube, WhatsApp, Chrome, Files और समय के कमांड सीधे आपके फ़ोन पर लोकल रूप से प्रोसेस होते हैं।\n• केवल सामान्य ज्ञान या AI चैट के सवाल सुरक्षित Firebase AI Logic (Gemini) को भेजे जाते हैं।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MunnaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onClearCommandHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = MunnaErrorRed.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MunnaErrorRed.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("settings_clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = MunnaErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("सारा कमांड इतिहास साफ़ करें", color = MunnaErrorRed)
                    }
                }
            }

            // 8. App Version & About / Beginner Testing Guide
            item {
                SettingsSectionCard(
                    title = "Munna AI के बारे में (About & Version)",
                    icon = Icons.Default.Info
                ) {
                    Text(
                        text = "Munna AI — Bolo, Munna Karega!\nसंस्करण (Version): 1.0.0 (Native Android Edition)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MunnaTextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "कैसे टेस्ट करें:\n1. होम स्क्रीन पर बड़ा नीला माइक दबाएँ या नीचे दिए गए उदाहरण कमांड चिप्स पर टैप करें।\n2. बोलें: \"Munna, YouTube kholo\", \"WhatsApp kholo\", \"अभी कितने बजे हैं?\"\n3. किसी भी सवाल के लिए 'AI चैट' टैब खोलें।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MunnaTextSecondary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
        border = BorderStroke(1.dp, MunnaCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MunnaTextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun PermissionStatusRow(
    label: String,
    isGranted: Boolean,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                contentDescription = null,
                tint = if (isGranted) MunnaSuccessGreen else MunnaWarningAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MunnaTextWhite
            )
        }
        if (actionLabel != null && onActionClick != null) {
            OutlinedButton(
                onClick = onActionClick,
                border = BorderStroke(1.dp, MunnaSkyBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = MunnaSkyBlue)
            }
        } else {
            Text(
                text = if (isGranted) "सक्रिय (Granted)" else "बंद (Not Granted)",
                style = MaterialTheme.typography.labelMedium,
                color = if (isGranted) MunnaSuccessGreen else MunnaWarningAmber,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
