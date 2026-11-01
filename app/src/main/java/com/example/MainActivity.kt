package com.example

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security.configureAppCheck
import com.example.service.FloatingMicService
import com.example.ui.MunnaTab
import com.example.ui.MunnaViewModel
import com.example.ui.components.DraggableFloatingMicOverlay
import com.example.ui.screens.AgentStudyScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MunnaAITheme
import com.example.ui.theme.MunnaCardNavy
import com.example.ui.theme.MunnaNavyDeep
import com.example.ui.theme.MunnaSkyBlue
import com.example.ui.theme.MunnaTextSecondary

class MainActivity : ComponentActivity() {

    private var pendingVoiceQuery by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent BEFORE UI or ViewModels
        configureAppCheck(this, intent)

        pendingVoiceQuery = intent?.getStringExtra(FloatingMicService.EXTRA_VOICE_QUERY)

        enableEdgeToEdge()

        setContent {
            MunnaAITheme {
                MunnaAppRoot(
                    externalVoiceQuery = pendingVoiceQuery,
                    onConsumeExternalQuery = { pendingVoiceQuery = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val query = intent.getStringExtra(FloatingMicService.EXTRA_VOICE_QUERY)
        if (!query.isNullOrBlank()) {
            pendingVoiceQuery = query
        }
    }
}

private data class NavTabItem(
    val tab: MunnaTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navItems = listOf(
    NavTabItem(
        tab = MunnaTab.HOME,
        label = "होम",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    NavTabItem(
        tab = MunnaTab.AGENT_STUDY,
        label = "एजेंट & PDF",
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School,
        testTag = "nav_tab_agent_study"
    ),
    NavTabItem(
        tab = MunnaTab.CHAT,
        label = "AI चैट",
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline,
        testTag = "nav_tab_chat"
    ),
    NavTabItem(
        tab = MunnaTab.HISTORY,
        label = "इतिहास",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        testTag = "nav_tab_history"
    ),
    NavTabItem(
        tab = MunnaTab.SETTINGS,
        label = "सेटिंग्स",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_tab_settings"
    )
)

@Composable
fun MunnaAppRoot(
    externalVoiceQuery: String? = null,
    onConsumeExternalQuery: () -> Unit = {},
    viewModel: MunnaViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()
    val permissions by viewModel.permissionsSnapshot.collectAsStateWithLifecycle()
    val lastFeedback by viewModel.lastActionFeedback.collectAsStateWithLifecycle()
    val commandHistory by viewModel.commandHistory.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val aiChatState by viewModel.aiChatState.collectAsStateWithLifecycle()
    val chatDraft by viewModel.chatDraftInput.collectAsStateWithLifecycle()
    val isOverlayServiceRunning by viewModel.isOverlayServiceRunning.collectAsStateWithLifecycle()
    val recentNotifications by viewModel.recentNotifications.collectAsStateWithLifecycle()
    val loadedPdfState by viewModel.loadedPdfState.collectAsStateWithLifecycle()
    val userMemories by viewModel.userMemories.collectAsStateWithLifecycle()

    var pendingForChatInput by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onMicPermissionResult(isGranted, forChatInput = pendingForChatInput)
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissionsAndAppStatus()
    }

    // Refresh permissions when returning from Android Settings (Overlay / Notification Access)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionsAndAppStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Consume voice query forwarded from FloatingMicService
    LaunchedEffect(externalVoiceQuery) {
        if (!externalVoiceQuery.isNullOrBlank()) {
            viewModel.processVoiceOrTextCommand(externalVoiceQuery)
            onConsumeExternalQuery()
        }
    }

    val handleMicTap: (Boolean) -> Unit = { forChat ->
        pendingForChatInput = forChat
        viewModel.refreshPermissionsAndAppStatus()
        if (viewModel.permissionsSnapshot.value.hasRecordAudioPermission) {
            viewModel.onMicButtonTapped(forChatInput = forChat)
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MunnaNavyDeep,
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MunnaCardNavy,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        navItems.forEach { item ->
                            val selected = currentTab == item.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(item.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MunnaNavyDeep,
                                    selectedTextColor = MunnaSkyBlue,
                                    indicatorColor = MunnaSkyBlue,
                                    unselectedIconColor = MunnaTextSecondary,
                                    unselectedTextColor = MunnaTextSecondary
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (isWideScreen) {
                        NavigationRail(
                            containerColor = MunnaCardNavy,
                            modifier = Modifier.testTag("side_navigation_rail")
                        ) {
                            navItems.forEach { item ->
                                val selected = currentTab == item.tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(item.tab) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MunnaNavyDeep,
                                        selectedTextColor = MunnaSkyBlue,
                                        indicatorColor = MunnaSkyBlue,
                                        unselectedIconColor = MunnaTextSecondary,
                                        unselectedTextColor = MunnaTextSecondary
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        when (currentTab) {
                            MunnaTab.HOME -> HomeScreen(
                                voiceState = voiceState,
                                ttsState = ttsState,
                                settings = settings,
                                permissions = permissions,
                                lastFeedback = lastFeedback,
                                recentCommands = commandHistory,
                                onMicClick = { handleMicTap(false) },
                                onCancelListening = { viewModel.cancelListening() },
                                onRequestMicPermission = {
                                    pendingForChatInput = false
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                onStartOverlayService = {
                                    viewModel.toggleSystemOverlayService(true)
                                },
                                onExecuteTextCommand = { cmd -> viewModel.processVoiceOrTextCommand(cmd) },
                                onShortcutClick = { app -> viewModel.launchShortcutApp(app) },
                                onSpeakResponse = { text -> viewModel.speakTextAloud(text) },
                                onStopSpeaking = { viewModel.stopSpeaking() },
                                onNavigateToSettings = { viewModel.selectTab(MunnaTab.SETTINGS) },
                                onNavigateToHistory = { viewModel.selectTab(MunnaTab.HISTORY) },
                                onNavigateToChat = { viewModel.selectTab(MunnaTab.CHAT) },
                                onNavigateToAgentStudy = { viewModel.selectTab(MunnaTab.AGENT_STUDY) }
                            )

                            MunnaTab.AGENT_STUDY -> AgentStudyScreen(
                                settings = settings,
                                permissions = permissions,
                                ttsState = ttsState,
                                loadedPdf = loadedPdfState,
                                userMemories = userMemories,
                                onSelectAgentMode = { mode -> viewModel.setAgentMode(mode) },
                                onOpenPdfUri = { uri -> viewModel.loadPdfFromUri(uri) },
                                onSelectPdfPage = { pageIdx -> viewModel.selectPdfPage(pageIdx) },
                                onLoadSampleStudyDoc = { doc -> viewModel.loadSampleStudyDocument(doc) },
                                onAnalyzePdfWithAi = { action -> viewModel.analyzeCurrentPdfPageWithAi(action) },
                                onExecuteCommand = { cmd -> viewModel.processVoiceOrTextCommand(cmd) },
                                onStartStudyOrDiscussionInChat = { mode, prompt ->
                                    viewModel.setAgentMode(mode, speakConfirmation = false)
                                    viewModel.selectTab(MunnaTab.CHAT)
                                    viewModel.sendChatMessage(prompt)
                                },
                                onAddMemoryFact = { cat, fact, phone ->
                                    viewModel.addPersonalMemoryFact(cat, fact, phone)
                                },
                                onDeleteMemoryFact = { id -> viewModel.deletePersonalMemory(id) },
                                onRequestCallPermission = {
                                    callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                },
                                onSpeakText = { text -> viewModel.speakTextAloud(text) },
                                onStopSpeaking = { viewModel.stopSpeaking() },
                                onMicClick = { handleMicTap(false) },
                                onBackToHome = { viewModel.selectTab(MunnaTab.HOME) }
                            )

                            MunnaTab.CHAT -> ChatScreen(
                                messages = chatMessages,
                                aiChatState = aiChatState,
                                draftInput = chatDraft,
                                voiceState = voiceState,
                                ttsState = ttsState,
                                onDraftChange = { viewModel.updateChatDraft(it) },
                                onSendMessage = { msg -> viewModel.sendChatMessage(msg) },
                                onMicClick = { handleMicTap(true) },
                                onSpeakText = { text -> viewModel.speakTextAloud(text) },
                                onStopSpeaking = { viewModel.stopSpeaking() },
                                onClearChat = { viewModel.clearChatHistory() },
                                onBackToHome = { viewModel.selectTab(MunnaTab.HOME) }
                            )

                            MunnaTab.HISTORY -> HistoryScreen(
                                historyList = commandHistory,
                                saveHistoryEnabled = settings.saveCommandHistory,
                                onToggleSaveHistory = { viewModel.preferences.setSaveCommandHistory(it) },
                                onReplayCommand = { cmd ->
                                    viewModel.selectTab(MunnaTab.HOME)
                                    viewModel.processVoiceOrTextCommand(cmd)
                                },
                                onSpeakResponse = { text -> viewModel.speakTextAloud(text) },
                                onDeleteCommand = { id -> viewModel.deleteCommandHistoryItem(id) },
                                onClearAllHistory = { viewModel.clearCommandHistory() },
                                onBackToHome = { viewModel.selectTab(MunnaTab.HOME) }
                            )

                            MunnaTab.SETTINGS -> SettingsScreen(
                                settings = settings,
                                preferences = viewModel.preferences,
                                permissions = permissions,
                                ttsState = ttsState,
                                isOverlayServiceRunning = isOverlayServiceRunning,
                                recentNotifications = recentNotifications,
                                onRequestMicPermission = {
                                    pendingForChatInput = false
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                onToggleSystemOverlay = { enable ->
                                    viewModel.toggleSystemOverlayService(enable)
                                },
                                onTestVoice = {
                                    viewModel.speakTextAloud("नमस्ते भाई! मैं मुन्ना हूँ। बोलो, मुन्ना करेगा!")
                                },
                                onSpeakNotification = { notif ->
                                    viewModel.speakTextAloud("${notif.appDisplayName} पर ${notif.senderOrTitle} का संदेश है: ${notif.contentSummary}")
                                },
                                onClearCommandHistory = { viewModel.clearCommandHistory() },
                                onRefreshPermissions = { viewModel.refreshPermissionsAndAppStatus() },
                                onBackToHome = { viewModel.selectTab(MunnaTab.HOME) }
                            )
                        }
                    }
                }

                // Floating Microphone near the right edge of the screen
                if (settings.inAppFloatingMicEnabled) {
                    DraggableFloatingMicOverlay(
                        status = voiceState.status,
                        onTapMic = {
                            handleMicTap(currentTab == MunnaTab.CHAT)
                        },
                        onHideFloatingMic = {
                            viewModel.preferences.setInAppFloatingMicEnabled(false)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                    )
                }
            }
        }
    }
}
