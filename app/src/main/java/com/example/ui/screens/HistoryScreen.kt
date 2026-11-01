package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.CommandHistoryEntity
import com.example.ui.theme.MunnaCardBorder
import com.example.ui.theme.MunnaCardElevated
import com.example.ui.theme.MunnaCardNavy
import com.example.ui.theme.MunnaCyanSoft
import com.example.ui.theme.MunnaErrorRed
import com.example.ui.theme.MunnaNavyDeep
import com.example.ui.theme.MunnaSkyBlue
import com.example.ui.theme.MunnaSuccessBg
import com.example.ui.theme.MunnaSuccessGreen
import com.example.ui.theme.MunnaTextSecondary
import com.example.ui.theme.MunnaTextWhite
import com.example.ui.theme.MunnaWarningAmber
import com.example.ui.theme.MunnaWarningBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class HistoryFilter {
    ALL,
    SUCCESS,
    FAILED
}

@Composable
fun HistoryScreen(
    historyList: List<CommandHistoryEntity>,
    saveHistoryEnabled: Boolean,
    onToggleSaveHistory: (Boolean) -> Unit,
    onReplayCommand: (String) -> Unit,
    onSpeakResponse: (String) -> Unit,
    onDeleteCommand: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    var selectedFilter by rememberSaveable { mutableStateOf(HistoryFilter.ALL) }
    var showClearConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val filteredHistory = remember(historyList, selectedFilter) {
        when (selectedFilter) {
            HistoryFilter.ALL -> historyList
            HistoryFilter.SUCCESS -> historyList.filter { it.isSuccess }
            HistoryFilter.FAILED -> historyList.filter { !it.isSuccess }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = MunnaCardNavy,
            titleContentColor = MunnaTextWhite,
            textContentColor = MunnaTextSecondary,
            title = { Text("क्या सारा इतिहास मिटा दें?") },
            text = {
                Text("यह आपके फ़ोन से सभी सहेजे गए कमांड का रिकॉर्ड हटा देगा। इसे वापस नहीं लाया जा सकता।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MunnaErrorRed),
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text("हाँ, साफ़ करें", color = MunnaTextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("रद्द करें", color = MunnaCyanSoft)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MunnaNavyDeep, Color(0xFF0A1830), MunnaNavyDeep)
                )
            )
            .testTag("history_screen"),
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
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MunnaSkyBlue,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "कमांड इतिहास (Command History)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MunnaTextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "कुल ${historyList.size} कमांड दर्ज किए गए",
                            style = MaterialTheme.typography.bodySmall,
                            color = MunnaCyanSoft
                        )
                    }
                }

                if (historyList.isNotEmpty()) {
                    Button(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MunnaErrorRed.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MunnaErrorRed.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "इतिहास साफ़ करें",
                            tint = MunnaErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "साफ़ करें",
                            style = MaterialTheme.typography.labelMedium,
                            color = MunnaErrorRed
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Privacy & Recording Control Card (Requirement 11)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                    border = BorderStroke(1.dp, MunnaCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MunnaSuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "गोपनीयता सुरक्षा (Privacy Control)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MunnaTextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "कच्ची ऑडियो रिकॉर्डिंग कभी सेव नहीं की जाती। केवल कमांड टेक्स्ट आपके फ़ोन में रहता है।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MunnaTextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = saveHistoryEnabled,
                                onCheckedChange = onToggleSaveHistory,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MunnaNavyDeep,
                                    checkedTrackColor = MunnaSkyBlue
                                ),
                                modifier = Modifier.testTag("toggle_save_history_switch")
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.ALL,
                        onClick = { selectedFilter = HistoryFilter.ALL },
                        label = { Text("सभी (${historyList.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MunnaSkyBlue,
                            selectedLabelColor = MunnaNavyDeep,
                            labelColor = MunnaTextWhite
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.SUCCESS,
                        onClick = { selectedFilter = HistoryFilter.SUCCESS },
                        label = { Text("सफल (${historyList.count { it.isSuccess }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MunnaSuccessGreen,
                            selectedLabelColor = MunnaNavyDeep,
                            labelColor = MunnaTextWhite
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.FAILED,
                        onClick = { selectedFilter = HistoryFilter.FAILED },
                        label = { Text("विफल (${historyList.count { !it.isSuccess }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MunnaWarningAmber,
                            selectedLabelColor = MunnaNavyDeep,
                            labelColor = MunnaTextWhite
                        )
                    )
                }
            }

            if (filteredHistory.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
                        border = BorderStroke(1.dp, MunnaCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MunnaTextSecondary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "कोई कमांड इतिहास नहीं मिला",
                                style = MaterialTheme.typography.titleMedium,
                                color = MunnaTextWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "जब आप होम स्क्रीन पर माइक दबाकर मुन्ना को कमांड देंगे, तो उनका विवरण यहाँ दिखाई देगा।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MunnaTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredHistory, key = { it.id }) { item ->
                    HistoryCommandCard(
                        item = item,
                        onReplayClick = { onReplayCommand(item.recognizedText) },
                        onSpeakClick = { onSpeakResponse(item.spokenResponse) },
                        onDeleteClick = { onDeleteCommand(item.id) }
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
private fun HistoryCommandCard(
    item: CommandHistoryEntity,
    onReplayClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MunnaCardNavy),
        border = BorderStroke(1.dp, MunnaCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Success or Failure Indicator Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (item.isSuccess) MunnaSuccessBg else MunnaWarningBg,
                    border = BorderStroke(
                        1.dp,
                        if (item.isSuccess) MunnaSuccessGreen.copy(alpha = 0.6f)
                        else MunnaWarningAmber.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (item.isSuccess) MunnaSuccessGreen else MunnaWarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (item.isSuccess) "सफल (Success)" else "असफल (Failed)",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.isSuccess) MunnaSuccessGreen else MunnaWarningAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MunnaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recognized Command Text
            Text(
                text = "🎙️ \"${item.recognizedText}\"",
                style = MaterialTheme.typography.titleMedium,
                color = MunnaTextWhite,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category & Action Status
            Text(
                text = "${item.intentCategory} • ${item.actionStatus}",
                style = MaterialTheme.typography.labelMedium,
                color = MunnaCyanSoft
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Spoken Response Detail
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MunnaCardElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.spokenResponse,
                    style = MaterialTheme.typography.bodySmall,
                    color = MunnaTextSecondary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Replay Command, Speak Response, Delete Item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onReplayClick) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "दोबारा चलाएँ",
                        tint = MunnaSkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("दोबारा चलाएँ", color = MunnaSkyBlue)
                }

                IconButton(onClick = onSpeakClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "जवाब सुनें",
                        tint = MunnaCyanSoft,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "हटाएँ",
                        tint = MunnaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
