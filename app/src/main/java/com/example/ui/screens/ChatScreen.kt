package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.local.ChatMessageEntity
import com.example.speech.MicStatus
import com.example.speech.TtsState
import com.example.speech.VoiceUiState
import com.example.ui.AiChatGenerationState
import com.example.ui.components.MunnaRobotLogo
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
import com.example.ui.theme.MunnaTextSecondary
import com.example.ui.theme.MunnaTextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val starterPrompts = listOf(
    "मुन्ना भाई, आज कुछ नया सिखाओ!",
    "चाय बनाने का आसान तरीका बताओ",
    "English बोलना कैसे सीखें?",
    "भारत के प्रमुख त्योहार कौन से हैं?",
    "फ़ोन की बैटरी कैसे बचाएँ?"
)

@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    aiChatState: AiChatGenerationState,
    draftInput: String,
    voiceState: VoiceUiState,
    ttsState: TtsState,
    onDraftChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onMicClick: () -> Unit,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onClearChat: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val isGenerating = aiChatState is AiChatGenerationState.Loading ||
        aiChatState is AiChatGenerationState.Streaming

    LaunchedEffect(messages.size, aiChatState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MunnaNavyDeep, Color(0xFF0A1830), MunnaNavyDeep)
                )
            )
            .testTag("chat_screen")
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MunnaRobotLogo(
                        size = 40.dp,
                        isAnimated = isGenerating || ttsState.isSpeaking
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Munna AI दोस्त (AI Chat)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MunnaTextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isGenerating) "मुन्ना सोच रहा है…" else "हिंदी और हिंग्लिश सवाल-जवाब",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaCyanSoft
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ttsState.isSpeaking) {
                        IconButton(onClick = onStopSpeaking) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "बोलना रोकें",
                                tint = MunnaCyanGlow
                            )
                        }
                    }
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "चैट साफ़ करें (Clear Chat)",
                            tint = MunnaTextSecondary
                        )
                    }
                }
            }
        }

        // Quick Starter Prompts Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            starterPrompts.forEachIndexed { idx, prompt ->
                Surface(
                    onClick = { if (!isGenerating) onSendMessage(prompt) },
                    shape = RoundedCornerShape(50),
                    color = MunnaCardElevated,
                    border = BorderStroke(1.dp, MunnaSkyBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("chat_starter_chip_$idx")
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelMedium,
                        color = MunnaCyanSoft,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    onSpeakClick = { onSpeakText(msg.messageText) }
                )
            }

            // Streaming / Loading Bubble
            if (aiChatState is AiChatGenerationState.Loading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .testTag("chat_loading_indicator")
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MunnaCyanGlow,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "मुन्ना जवाब लिख रहा है…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaCyanSoft
                        )
                    }
                }
            } else if (aiChatState is AiChatGenerationState.Streaming) {
                item {
                    ChatBubbleItem(
                        message = ChatMessageEntity(
                            id = -1L,
                            sender = "MUNNA",
                            messageText = aiChatState.partialText.ifBlank { "…" }
                        ),
                        onSpeakClick = {}
                    )
                }
            }
        }

        // Bottom Input Bar: Microphone Button + Text Input + Send Button
        Surface(
            color = MunnaCardNavy,
            border = BorderStroke(1.dp, MunnaCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Mic Button for Chat
                val isListening = voiceState.status == MicStatus.LISTENING
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) MunnaCyanGlow else MunnaCardElevated
                        )
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = "बोलकर सवाल पूछें (Speak question)",
                        tint = if (isListening) MunnaNavyDeep else MunnaSkyBlue
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = draftInput,
                    onValueChange = onDraftChange,
                    placeholder = {
                        Text(
                            text = if (isListening) "सुन रहा हूँ… बोलिए" else "मुन्ना से हिंदी या हिंग्लिश में पूछें…",
                            color = MunnaTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MunnaSkyBlue,
                        unfocusedBorderColor = MunnaCardBorder,
                        focusedContainerColor = MunnaNavyDeep,
                        unfocusedContainerColor = MunnaNavyDeep,
                        focusedTextColor = MunnaTextWhite,
                        unfocusedTextColor = MunnaTextWhite
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (draftInput.isNotBlank() && !isGenerating) {
                                onSendMessage(draftInput)
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (draftInput.isNotBlank() && !isGenerating) {
                            onSendMessage(draftInput)
                            focusManager.clearFocus()
                        }
                    },
                    enabled = draftInput.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (draftInput.isNotBlank() && !isGenerating) MunnaSkyBlue
                            else MunnaCardElevated
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "संदेश भेजें (Send Message)",
                        tint = if (draftInput.isNotBlank() && !isGenerating) MunnaNavyDeep else MunnaTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessageEntity,
    onSpeakClick: () -> Unit
) {
    val isUser = message.sender == "USER"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = when {
                message.isError -> MunnaErrorBg
                isUser -> MunnaSkyBlueDeep
                else -> MunnaCardElevated
            },
            border = BorderStroke(
                1.dp,
                when {
                    message.isError -> MunnaErrorRed.copy(alpha = 0.6f)
                    isUser -> MunnaSkyBlue.copy(alpha = 0.6f)
                    else -> MunnaCardBorder
                }
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isUser) "आप (You)" else "🤖 मुन्ना AI",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isUser) MunnaCyanSoft else MunnaSkyBlue,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isUser && message.id != -1L) {
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "उत्तर सुनें (Play TTS)",
                                tint = MunnaCyanSoft,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isError) MunnaErrorRed else MunnaTextWhite
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = timeFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MunnaTextSecondary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
