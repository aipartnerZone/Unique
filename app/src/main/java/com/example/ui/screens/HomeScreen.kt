package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommandHistoryEntity
import com.example.data.local.MunnaSettingsState
import com.example.domain.parser.TargetApp
import com.example.speech.MicStatus
import com.example.speech.TtsState
import com.example.speech.VoiceUiState
import com.example.ui.DevicePermissionsSnapshot
import com.example.ui.LastActionFeedback
import com.example.ui.components.CentralAnimatedMicrophone
import com.example.ui.components.MunnaRobotLogo
import com.example.ui.theme.BrandChromeBlue
import com.example.ui.theme.BrandFilesBlue
import com.example.ui.theme.BrandWhatsAppGreen
import com.example.ui.theme.BrandYouTubeRed
import com.example.ui.theme.MunnaCardBorder
import com.example.ui.theme.MunnaCardElevated
import com.example.ui.theme.MunnaCardNavy
import com.example.ui.theme.MunnaCyanGlow
import com.example.ui.theme.MunnaCyanSoft
import com.example.ui.theme.MunnaErrorBg
import com.example.ui.theme.MunnaErrorRed
import com.example.ui.theme.MunnaNavyDeep
import com.example.ui.theme.MunnaSkyBlue
import com.example.ui.theme.MunnaSkyBlueDeep
import com.example.ui.theme.MunnaSuccessBg
import com.example.ui.theme.MunnaSuccessGreen
import com.example.ui.theme.MunnaTextSecondary
import com.example.ui.theme.MunnaTextWhite
import com.example.ui.theme.MunnaWarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val sampleQuickCommands = listOf(
    "Munna, YouTube kholo.",
    "WhatsApp kholo.",
    "Chrome open karo.",
    "Meri files kholo.",
    "Munna, abhi kitne baje hain?",
    "Munna, meri baat suno."
)

@Composable
fun HomeScreen(
    voiceState: VoiceUiState,
    ttsState: TtsState,
    settings: MunnaSettingsState,
    permissions: DevicePermissionsSnapshot,
    lastFeedback: LastActionFeedback?,
    recentCommands: List<CommandHistoryEntity>,
    onMicClick: () -> Unit,
    onCancelListening: () -> Unit,
    onRequestMicPermission: () -> Unit,
    onExecuteTextCommand: (String) -> Unit,
    onShortcutClick: (TargetApp) -> Unit,
    onSpeakResponse: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTextInputBox by rememberSaveable { mutableStateOf(false) }
    var textCommandDraft by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MunnaNavyDeep, Color(0xFF0A1830), MunnaNavyDeep)
                )
            )
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1, 2, 3. Top Header: Small Munna AI Logo (Top-Left), Title "Munna AI", Settings Icon (Top-Right)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MunnaRobotLogo(
                        size = 46.dp,
                        isAnimated = voiceState.status == MicStatus.LISTENING || ttsState.isSpeaking
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Munna ",
                                style = MaterialTheme.typography.titleLarge,
                                color = MunnaTextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.titleLarge,
                                color = MunnaSkyBlue,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "Bolo, Munna Karega! • ${settings.language.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaCyanSoft
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ttsState.isSpeaking) {
                        IconButton(
                            onClick = onStopSpeaking,
                            modifier = Modifier.testTag("stop_tts_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "बोलना रोकें (Stop Speaking)",
                                tint = MunnaCyanGlow
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MunnaCardNavy)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "सेटिंग्स खोलें (Open Settings)",
                            tint = MunnaSkyBlue
                        )
                    }
                }
            }
        }

        // 4. Friendly Greeting Card: “नमस्ते भाई! मैं मुन्ना हूँ। बताओ, क्या करना है?”
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("greeting_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                border = BorderStroke(1.dp, MunnaCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👋 नमस्ते भाई!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MunnaCyanSoft,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = MunnaSkyBlueDeep.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MunnaSkyBlue.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "मैं मुन्ना हूँ 😊",
                                style = MaterialTheme.typography.labelMedium,
                                color = MunnaTextWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "नमस्ते भाई! मैं मुन्ना हूँ। बताओ, क्या करना है?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MunnaTextWhite
                    )

                    // Live Speech Transcript or Last Spoken Feedback
                    val liveWords = voiceState.partialTranscript.ifBlank { voiceState.finalTranscript }
                    if (liveWords.isNotBlank() && voiceState.status == MicStatus.LISTENING) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MunnaCardElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MunnaCyanGlow.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "🎙️ आप बोल रहे हैं: \"$liveWords\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MunnaCyanSoft,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else if (lastFeedback != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MunnaCardElevated.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (lastFeedback.isSuccess) MunnaSkyBlue.copy(alpha = 0.45f)
                                else MunnaWarningAmber.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "कमांड: \"${lastFeedback.commandText}\"",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MunnaSkyBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onSpeakResponse(lastFeedback.responseText) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "जवाब सुनें (Listen to response)",
                                            tint = MunnaCyanSoft,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastFeedback.responseText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MunnaTextWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5 & 6. Central Animated Microphone Button + Status Label Below
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CentralAnimatedMicrophone(
                    status = voiceState.status,
                    rmsDb = voiceState.rmsDb,
                    isSpeaking = ttsState.isSpeaking,
                    onMicClick = onMicClick,
                    onCancelClick = onCancelListening
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Label below the microphone
                val statusColor = when (voiceState.status) {
                    MicStatus.LISTENING -> MunnaCyanGlow
                    MicStatus.PROCESSING -> MunnaSkyBlue
                    MicStatus.COMPLETED -> MunnaSuccessGreen
                    MicStatus.ERROR -> MunnaErrorRed
                    MicStatus.IDLE -> MunnaTextWhite
                }

                Text(
                    text = voiceState.statusLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("mic_status_label")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Toggleable pill: "या लिखकर भी पूछ सकते हैं"
                Surface(
                    onClick = { showTextInputBox = !showTextInputBox },
                    shape = RoundedCornerShape(50),
                    color = MunnaCardElevated,
                    border = BorderStroke(1.dp, MunnaCardBorder),
                    modifier = Modifier.testTag("toggle_text_command_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = MunnaSkyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showTextInputBox) "टाइपिंग बॉक्स बंद करें" else "या लिखकर भी कमांड दे सकते हैं",
                            style = MaterialTheme.typography.labelMedium,
                            color = MunnaCyanSoft
                        )
                    }
                }

                AnimatedVisibility(visible = showTextInputBox) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textCommandDraft,
                            onValueChange = { textCommandDraft = it },
                            placeholder = {
                                Text(
                                    text = "जैसे: Munna, YouTube kholo...",
                                    color = MunnaTextSecondary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MunnaSkyBlue,
                                unfocusedBorderColor = MunnaCardBorder,
                                focusedContainerColor = MunnaCardNavy,
                                unfocusedContainerColor = MunnaCardNavy,
                                focusedTextColor = MunnaTextWhite,
                                unfocusedTextColor = MunnaTextWhite
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (textCommandDraft.isNotBlank()) {
                                        onExecuteTextCommand(textCommandDraft)
                                        textCommandDraft = ""
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_command_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (textCommandDraft.isNotBlank()) {
                                    onExecuteTextCommand(textCommandDraft)
                                    textCommandDraft = ""
                                    focusManager.clearFocus()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MunnaSkyBlue),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("home_command_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "कमांड भेजें (Send Command)",
                                tint = MunnaNavyDeep
                            )
                        }
                    }
                }

                // Error Banner with Actionable Next Step (Requirement 14)
                if (voiceState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_error_card"),
                        colors = CardDefaults.cardColors(containerColor = MunnaErrorBg),
                        border = BorderStroke(1.dp, MunnaErrorRed.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MunnaErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = voiceState.statusLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MunnaErrorRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = voiceState.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MunnaTextWhite
                            )
                            if (!permissions.hasRecordAudioPermission) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onRequestMicPermission,
                                    colors = ButtonDefaults.buttonColors(containerColor = MunnaSkyBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("grant_mic_permission_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = MunnaNavyDeep,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "माइक की अनुमति दें (Allow Mic)",
                                        color = MunnaNavyDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Voice Command Simulator Chips (For instant testing of Hindi & Hinglish commands)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MunnaCyanGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "बोलकर या टैप करके आज़माएँ (Example Voice Commands)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MunnaTextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleQuickCommands.forEachIndexed { idx, cmd ->
                        Surface(
                            onClick = { onExecuteTextCommand(cmd) },
                            shape = RoundedCornerShape(50),
                            color = MunnaCardElevated,
                            border = BorderStroke(1.dp, MunnaSkyBlue.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("sample_command_chip_$idx")
                        ) {
                            Text(
                                text = "🎙️ \"$cmd\"",
                                style = MaterialTheme.typography.labelMedium,
                                color = MunnaTextWhite,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Four App Shortcut Cards: YouTube, WhatsApp, Chrome, Files
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("app_shortcuts_section"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                border = BorderStroke(1.dp, MunnaCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MunnaSkyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "जल्दी करने के लिए (App Shortcuts)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MunnaTextWhite
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppShortcutCard(
                            app = TargetApp.YOUTUBE,
                            icon = Icons.Default.PlayArrow,
                            brandColor = BrandYouTubeRed,
                            isInstalled = permissions.appInstallStatus[TargetApp.YOUTUBE] == true,
                            onClick = { onShortcutClick(TargetApp.YOUTUBE) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_card_youtube")
                        )
                        AppShortcutCard(
                            app = TargetApp.WHATSAPP,
                            icon = Icons.Default.ChatBubbleOutline,
                            brandColor = BrandWhatsAppGreen,
                            isInstalled = permissions.appInstallStatus[TargetApp.WHATSAPP] == true,
                            onClick = { onShortcutClick(TargetApp.WHATSAPP) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_card_whatsapp")
                        )
                        AppShortcutCard(
                            app = TargetApp.CHROME,
                            icon = Icons.Default.Public,
                            brandColor = BrandChromeBlue,
                            isInstalled = permissions.appInstallStatus[TargetApp.CHROME] == true,
                            onClick = { onShortcutClick(TargetApp.CHROME) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_card_chrome")
                        )
                        AppShortcutCard(
                            app = TargetApp.FILES,
                            icon = Icons.Default.Folder,
                            brandColor = BrandFilesBlue,
                            isInstalled = permissions.appInstallStatus[TargetApp.FILES] == true,
                            onClick = { onShortcutClick(TargetApp.FILES) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_card_files")
                        )
                    }
                }
            }
        }

        // 8. Recent Commands Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("recent_commands_section"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                border = BorderStroke(1.dp, MunnaCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MunnaSkyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "हाल के कमांड (Recent Commands)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MunnaTextWhite
                            )
                        }
                        TextButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.testTag("view_all_history_button")
                        ) {
                            Text(
                                text = "सभी देखें →",
                                style = MaterialTheme.typography.labelLarge,
                                color = MunnaCyanSoft
                            )
                        }
                    }

                    if (recentCommands.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "अभी तक कोई कमांड नहीं दिया गया है। ऊपर माइक दबाएँ या कोई शॉर्टकट चुनें!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaTextSecondary
                        )
                    } else {
                        val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentCommands.take(3).forEach { cmd ->
                                RecentCommandMiniRow(
                                    item = cmd,
                                    formattedTime = timeFormat.format(Date(cmd.timestamp)),
                                    onReplayClick = { onExecuteTextCommand(cmd.recognizedText) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick AI Chat Invite Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MunnaCardElevated),
                border = BorderStroke(1.dp, MunnaSkyBlue.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🤖 मुन्ना AI से हिंदी में बातचीत करें",
                            style = MaterialTheme.typography.titleSmall,
                            color = MunnaCyanSoft,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "पढ़ाई, सलाह, रेसिपी या किसी भी सवाल का जवाब बोलकर और लिखकर पाएँ।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = onNavigateToChat,
                        border = BorderStroke(1.dp, MunnaSkyBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_ai_chat_card_button")
                    ) {
                        Text("AI चैट", color = MunnaSkyBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun AppShortcutCard(
    app: TargetApp,
    icon: ImageVector,
    brandColor: Color,
    isInstalled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MunnaCardElevated,
        border = BorderStroke(1.dp, MunnaCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(brandColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "${app.displayName} खोलें",
                    tint = brandColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MunnaTextWhite,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isInstalled) "तैयार" else "जांचें",
                fontSize = 10.sp,
                color = if (isInstalled) MunnaSuccessGreen else MunnaTextSecondary
            )
        }
    }
}

@Composable
private fun RecentCommandMiniRow(
    item: CommandHistoryEntity,
    formattedTime: String,
    onReplayClick: () -> Unit
) {
    Surface(
        onClick = onReplayClick,
        shape = RoundedCornerShape(14.dp),
        color = MunnaCardElevated,
        border = BorderStroke(1.dp, MunnaCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (item.isSuccess) MunnaSuccessGreen else MunnaWarningAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "\"${item.recognizedText}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MunnaTextWhite,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.actionStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.isSuccess) MunnaCyanSoft else MunnaWarningAmber,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodySmall,
                color = MunnaTextSecondary
            )
        }
    }
}
