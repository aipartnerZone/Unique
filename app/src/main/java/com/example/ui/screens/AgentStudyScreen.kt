package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.MunnaAgentMode
import com.example.data.local.MunnaSettingsState
import com.example.data.local.UserMemoryEntity
import com.example.pdf.LoadedPdfDocumentState
import com.example.pdf.MunnaPdfHelper
import com.example.pdf.SampleStudyDocument
import com.example.speech.TtsState
import com.example.ui.DevicePermissionsSnapshot
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
import com.example.ui.theme.MunnaSuccessGreen
import com.example.ui.theme.MunnaTextSecondary
import com.example.ui.theme.MunnaTextWhite
import com.example.ui.theme.MunnaWarningAmber

private val discussionTopics = listOf(
    "मुन्ना, मुझसे AI और भविष्य की नौकरियों पर डिस्कस करो",
    "मोबाइल फ़ोन स्टूडेंट के लिए वरदान है या अभिशाप? डिस्कस करो",
    "सफलता के लिए कड़ी मेहनत ज़रूरी है या स्मार्ट वर्क? चर्चा करो",
    "गाँव की ज़िंदगी बेहतर है या शहर की? अपने विचार बताओ"
)

private val studentStudyPrompts = listOf(
    "मुन्ना, मुझे प्रकाश संश्लेषण (Photosynthesis) आसान उदाहरण से समझाओ",
    "न्यूटन के गति के तीनों नियम क्रिकेट के उदाहरण से सिखाओ",
    "आसान शब्दों में बताओ कि इंटरनेट कैसे काम करता है?",
    "मेरे साथ रोज़मर्रा की English बोलने की प्रैक्टिस करो",
    "मुझसे भारत के सामान्य ज्ञान (GK) के 3 सवाल पूछो"
)

@Composable
fun AgentStudyScreen(
    settings: MunnaSettingsState,
    permissions: DevicePermissionsSnapshot,
    ttsState: TtsState,
    loadedPdf: LoadedPdfDocumentState,
    userMemories: List<UserMemoryEntity>,
    onSelectAgentMode: (MunnaAgentMode) -> Unit,
    onOpenPdfUri: (android.net.Uri) -> Unit,
    onSelectPdfPage: (Int) -> Unit,
    onLoadSampleStudyDoc: (SampleStudyDocument) -> Unit,
    onAnalyzePdfWithAi: (String) -> Unit,
    onExecuteCommand: (String) -> Unit,
    onStartStudyOrDiscussionInChat: (MunnaAgentMode, String) -> Unit,
    onAddMemoryFact: (String, String, String) -> Unit,
    onDeleteMemoryFact: (Long) -> Unit,
    onRequestCallPermission: () -> Unit,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onMicClick: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    val focusManager = LocalFocusManager.current
    var newMemoryText by rememberSaveable { mutableStateOf("") }
    var newContactPhone by rememberSaveable { mutableStateOf("") }
    var callTargetInput by rememberSaveable { mutableStateOf("") }
    var customAppInput by rememberSaveable { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onOpenPdfUri(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MunnaNavyDeep, Color(0xFF0A1830), MunnaNavyDeep)
                )
            )
            .testTag("agent_study_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header
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
                        size = 44.dp,
                        isAnimated = ttsState.isSpeaking || loadedPdf.isAnalyzingWithAi
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "मुन्ना एजेंट, PDF & स्टूडेंट गुरु",
                            style = MaterialTheme.typography.titleMedium,
                            color = MunnaTextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${settings.agentMode.badgeEmoji} ${settings.agentMode.titleHindi}",
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
                        onClick = onMicClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MunnaSkyBlue)
                            .testTag("agent_screen_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "माइक से बोलें",
                            tint = MunnaNavyDeep
                        )
                    }
                }
            }
        }

        // 2. Agent Persona Mode Switcher (Smart Agent / Discussion Partner / Student Study Tutor)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                border = BorderStroke(1.dp, MunnaSkyBlue.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🤖 मुन्ना का रोल चुनें (Agent, Discussion & Study Mode)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MunnaCyanSoft,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "आप बोलकर भी मोड बदल सकते हैं: \"मुझसे डिस्कस करो\" या \"मेरे साथ पढ़ाई करो\"।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MunnaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MunnaAgentMode.entries.forEach { mode ->
                            val selected = settings.agentMode == mode
                            Surface(
                                onClick = { onSelectAgentMode(mode) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) MunnaSkyBlueDeep.copy(alpha = 0.35f) else MunnaCardElevated,
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) MunnaCyanGlow else MunnaCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("agent_mode_card_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mode.badgeEmoji,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.titleHindi,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (selected) MunnaCyanSoft else MunnaTextWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = mode.subtitleHindi,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MunnaTextSecondary
                                        )
                                    }
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "सक्रिय मोड",
                                            tint = MunnaSuccessGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MunnaCardBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Student Tutor & Discussion Starters
                    Text(
                        text = if (settings.agentMode == MunnaAgentMode.DISCUSSION_PARTNER) {
                            "🗣️ दोस्ताना डिस्कशन शुरू करने के लिए टैप करें:"
                        } else {
                            "🎓 स्टूडेंट पढ़ाई और डिस्कशन के विषय (टैप करके शुरू करें):"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MunnaTextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val prompts = if (settings.agentMode == MunnaAgentMode.DISCUSSION_PARTNER) {
                        discussionTopics
                    } else {
                        studentStudyPrompts + discussionTopics.take(2)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prompts.forEachIndexed { idx, topic ->
                            Surface(
                                onClick = {
                                    val targetMode = if (topic.contains("डिस्कस") || topic.contains("चर्चा")) {
                                        MunnaAgentMode.DISCUSSION_PARTNER
                                    } else {
                                        MunnaAgentMode.STUDENT_TUTOR
                                    }
                                    onStartStudyOrDiscussionInChat(targetMode, topic)
                                },
                                shape = RoundedCornerShape(50),
                                color = MunnaCardElevated,
                                border = BorderStroke(1.dp, MunnaSkyBlue.copy(alpha = 0.45f)),
                                modifier = Modifier.testTag("study_discussion_chip_$idx")
                            ) {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MunnaCyanSoft,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. PDF Reader & AI Study Explainer Card ("pdf padh sakta hai")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("pdf_reader_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                border = BorderStroke(1.5.dp, MunnaCyanGlow.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MunnaCyanGlow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "📄 मुन्ना PDF रीडर और नोट्स गुरु",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MunnaTextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "कोई भी PDF खोलें — मुन्ना पढ़कर सुनाएगा, समझाएगा और सवाल पूछेगा!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MunnaTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Open PDF File Button
                    Button(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        colors = ButtonDefaults.buttonColors(containerColor = MunnaSkyBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_pdf_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MunnaNavyDeep
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📂 फ़ोन से अपनी PDF फ़ाइल चुनें (Open PDF)",
                            color = MunnaNavyDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Built-in Sample Study Notes selector
                    Text(
                        text = "या तुरंत टेस्ट करने के लिए तैयार स्टडी नोट्स चुनें:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MunnaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MunnaPdfHelper.sampleStudyDocuments.forEach { sampleDoc ->
                            val isCurrent = loadedPdf.fileName.startsWith(sampleDoc.title)
                            Surface(
                                onClick = { onLoadSampleStudyDoc(sampleDoc) },
                                shape = RoundedCornerShape(50),
                                color = if (isCurrent) MunnaSkyBlueDeep.copy(alpha = 0.45f) else MunnaCardElevated,
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) MunnaCyanGlow else MunnaCardBorder
                                )
                            ) {
                                Text(
                                    text = "📘 ${sampleDoc.subjectBadge}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MunnaTextWhite,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active PDF Preview & Page Controls
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MunnaCardElevated,
                        border = BorderStroke(1.dp, MunnaCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "📄 ${loadedPdf.fileName.ifBlank { "स्टडी नोट्स" }}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MunnaCyanSoft,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (loadedPdf.totalPages > 0) {
                                    Text(
                                        text = "पेज ${loadedPdf.currentPageIndex + 1} / ${loadedPdf.totalPages}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MunnaTextSecondary
                                    )
                                }
                            }

                            if (loadedPdf.totalPages > 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onSelectPdfPage(loadedPdf.currentPageIndex - 1) },
                                        enabled = loadedPdf.currentPageIndex > 0 && !loadedPdf.isAnalyzingWithAi,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("◀ पिछला पेज", color = MunnaSkyBlue)
                                    }
                                    OutlinedButton(
                                        onClick = { onSelectPdfPage(loadedPdf.currentPageIndex + 1) },
                                        enabled = loadedPdf.currentPageIndex < loadedPdf.totalPages - 1 && !loadedPdf.isAnalyzingWithAi,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("अगला पेज ▶", color = MunnaSkyBlue)
                                    }
                                }
                            }

                            // Show rendered PDF page Bitmap if loaded from a real PDF file
                            if (loadedPdf.currentPageBitmap != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    bitmap = loadedPdf.currentPageBitmap.asImageBitmap(),
                                    contentDescription = "PDF पेज ${loadedPdf.currentPageIndex + 1}",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 260.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (loadedPdf.isAnalyzingWithAi) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MunnaCyanGlow,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "मुन्ना AI इस PDF पेज को पढ़ और समझ रहा है…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MunnaCyanSoft
                                    )
                                }
                            } else {
                                Text(
                                    text = loadedPdf.extractedOrAnalyzedText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MunnaTextWhite
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons on current PDF / Study Notes
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onSpeakText(loadedPdf.extractedOrAnalyzedText) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MunnaSkyBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("pdf_speak_aloud_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = MunnaNavyDeep,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("बोलकर सुनाओ", color = MunnaNavyDeep, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onAnalyzePdfWithAi("READ_AND_EXPLAIN") },
                                    border = BorderStroke(1.dp, MunnaCyanGlow),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("pdf_explain_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = MunnaCyanSoft,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("आसान भाषा में समझाओ", color = MunnaCyanSoft)
                                }

                                OutlinedButton(
                                    onClick = { onAnalyzePdfWithAi("QUIZ_ME") },
                                    border = BorderStroke(1.dp, MunnaSkyBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("pdf_quiz_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Quiz,
                                        contentDescription = null,
                                        tint = MunnaSkyBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("मुझसे 3 सवाल पूछो", color = MunnaSkyBlue)
                                }

                                OutlinedButton(
                                    onClick = { onAnalyzePdfWithAi("DISCUSS_PDF") },
                                    border = BorderStroke(1.dp, MunnaSkyBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = null,
                                        tint = MunnaSkyBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("इस पर डिस्कस करो", color = MunnaSkyBlue)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Smart Voice Calling Card ("ham kahenge to ye coll bhi karega")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("voice_call_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = MunnaSuccessGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "📞 स्मार्ट वॉइस कॉलिंग (बोलकर कॉल लगाएँ)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MunnaTextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "बोलें: \"9876543210 पर कॉल करो\" या \"पापा को कॉल लगाओ\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MunnaTextSecondary
                                )
                            }
                        }

                        if (!permissions.hasCallPhonePermission) {
                            OutlinedButton(
                                onClick = onRequestCallPermission,
                                border = BorderStroke(1.dp, MunnaWarningAmber),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("सीधा कॉल अनुमति दें", color = MunnaWarningAmber, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = callTargetInput,
                            onValueChange = { callTargetInput = it },
                            placeholder = {
                                Text(
                                    text = "नंबर या सेव किया नाम लिखें (जैसे: 112 या पापा)…",
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
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (callTargetInput.isNotBlank()) {
                                        onExecuteCommand("${callTargetInput.trim()} पर कॉल करो")
                                        callTargetInput = ""
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (callTargetInput.isNotBlank()) {
                                    onExecuteCommand("${callTargetInput.trim()} पर कॉल करो")
                                    callTargetInput = ""
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MunnaSuccessGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = MunnaNavyDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("कॉल करें", color = MunnaNavyDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. All-Apps Voice Launcher + Payment App Safety Shield ("sabhi app bolkar kholne me saksham, kevel payment app nhi kholega")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("payment_security_shield_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                border = BorderStroke(1.dp, MunnaCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MunnaSuccessGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "🔒 सभी ऐप्स वॉइस लॉन्चर (Payment App Lock सक्रिय)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MunnaTextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "मुन्ना फ़ोन का कोई भी सामान्य ऐप बोलकर खोल सकता है, लेकिन सुरक्षा के लिए Google Pay, PhonePe, Paytm, BHIM और बैंकिंग ऐप्स कभी नहीं खोलेगा।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MunnaTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Payment App Block vs Any Custom App
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customAppInput,
                            onValueChange = { customAppInput = it },
                            placeholder = {
                                Text(
                                    text = "कोई भी ऐप नाम लिखें (जैसे Calculator, PhonePe)…",
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
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customAppInput.isNotBlank()) {
                                    onExecuteCommand("${customAppInput.trim()} खोलो")
                                    customAppInput = ""
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MunnaSkyBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("खोलें", color = MunnaNavyDeep, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Security Test Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Google Pay kholo", "PhonePe kholo", "Paytm kholo", "BHIM UPI kholo").forEach { blockedCmd ->
                            Surface(
                                onClick = { onExecuteCommand(blockedCmd) },
                                shape = RoundedCornerShape(50),
                                color = MunnaErrorBg,
                                border = BorderStroke(1.dp, MunnaErrorRed.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MunnaErrorRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "टेस्ट ब्लॉक: \"$blockedCmd\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MunnaTextWhite
                                    )
                                }
                            }
                        }
                    }

                    if (permissions.installedApps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "📱 आपके फ़ोन में मिले ऐप्स (टैप करके या बोलकर खोलें):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MunnaCyanSoft
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            permissions.installedApps.take(20).forEach { appInfo ->
                                Surface(
                                    onClick = { onExecuteCommand("${appInfo.label} खोलो") },
                                    shape = RoundedCornerShape(50),
                                    color = if (appInfo.isBlockedPaymentApp) MunnaErrorBg else MunnaCardElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (appInfo.isBlockedPaymentApp) MunnaErrorRed else MunnaCardBorder
                                    )
                                ) {
                                    Text(
                                        text = if (appInfo.isBlockedPaymentApp) "🔒 ${appInfo.label}" else "🚀 ${appInfo.label}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MunnaTextWhite,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Personal Agent Learning Memory Card ("ye hamre data se sikhega")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("agent_memory_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                border = BorderStroke(1.dp, MunnaCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MunnaCyanGlow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "🧠 मुन्ना की याददाश्त (आपके डेटा से सीखने वाला एजेंट)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MunnaCyanSoft,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "जब आप अपना नाम, क्लास, सब्जेक्ट या किसी का नंबर बताते हैं, तो मुन्ना उसे याद रखता है।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MunnaTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add a new memory / contact manually
                    OutlinedTextField(
                        value = newMemoryText,
                        onValueChange = { newMemoryText = it },
                        placeholder = {
                            Text(
                                text = "मुन्ना को सिखाएँ (जैसे: मैं 12वीं साइंस का छात्र हूँ / पापा)…",
                                color = MunnaTextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MunnaSkyBlue,
                            unfocusedBorderColor = MunnaCardBorder,
                            focusedContainerColor = MunnaNavyDeep,
                            unfocusedContainerColor = MunnaNavyDeep,
                            focusedTextColor = MunnaTextWhite,
                            unfocusedTextColor = MunnaTextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newContactPhone,
                            onValueChange = { newContactPhone = it },
                            placeholder = {
                                Text(
                                    text = "फ़ोन नंबर (यदि संपर्क जोड़ रहे हैं)…",
                                    color = MunnaTextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MunnaSkyBlue,
                                unfocusedBorderColor = MunnaCardBorder,
                                focusedContainerColor = MunnaNavyDeep,
                                unfocusedContainerColor = MunnaNavyDeep,
                                focusedTextColor = MunnaTextWhite,
                                unfocusedTextColor = MunnaTextWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newMemoryText.isNotBlank()) {
                                    val cat = if (newContactPhone.isNotBlank()) "कॉल संपर्क (Contact)" else "याददाश्त (Memory)"
                                    onAddMemoryFact(cat, newMemoryText, newContactPhone)
                                    newMemoryText = ""
                                    newContactPhone = ""
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MunnaSkyBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MunnaNavyDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("याद रखें", color = MunnaNavyDeep, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of learned memories
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        userMemories.take(10).forEach { mem ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MunnaCardElevated,
                                border = BorderStroke(1.dp, MunnaCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mem.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MunnaCyanSoft
                                        )
                                        Text(
                                            text = mem.memoryValue,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MunnaTextWhite,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (mem.contactPhone.isNotBlank()) {
                                        IconButton(
                                            onClick = { onExecuteCommand("${mem.contactPhone} पर कॉल करो") }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "कॉल करें",
                                                tint = MunnaSuccessGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteMemoryFact(mem.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "हटाएँ",
                                            tint = MunnaTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
