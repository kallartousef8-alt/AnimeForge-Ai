package com.example.ui.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.AspectRatio
import com.example.data.models.AudioTrackItem
import com.example.data.models.AudioType
import com.example.service.AiScriptService
import com.example.ui.components.AnimeButton
import com.example.ui.components.GlowBadge
import com.example.ui.editor.components.AudioControlPanel
import com.example.ui.editor.components.ClipTrimPanel
import com.example.ui.editor.components.EditorCanvasPlayer
import com.example.ui.editor.components.EditorTimeline
import com.example.ui.editor.components.EffectsPanel
import com.example.ui.editor.components.FiltersPanel
import com.example.ui.editor.components.SpeedDialog
import com.example.ui.editor.components.TransitionDialog
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import java.util.UUID

@Composable
fun EditorScreen(
    projectId: Long,
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExport: (Long) -> Unit,
    onNavigateToAiVoice: (Long) -> Unit,
    onNavigateToAiScript: (Long) -> Unit,
    onNavigateToCaptions: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scriptService = remember { AiScriptService() }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTransitionDialog by remember { mutableStateOf(false) }
    var showShortsAssistantDialog by remember { mutableStateOf(false) }

    // Android Photo Picker for video & image import
    val pickVideosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val uriStrings = uris.map { it.toString() }
            viewModel.addClips(uriStrings)
        }
    }

    // Android Audio Picker for music and sound effects import
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val track = AudioTrackItem(
                id = UUID.randomUUID().toString(),
                uriString = uri.toString(),
                title = "Imported Music/SFX",
                startTimelineMs = 0L,
                durationMs = 15000L,
                volume = 0.8f,
                isVoiceover = false,
                audioType = AudioType.MUSIC,
                fadeInMs = 500L,
                fadeOutMs = 1000L
            )
            viewModel.addAudioTrack(track)
            Toast.makeText(context, "Audio track added to timeline!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val project = state.project

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AnimeBackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = project?.title ?: "Anime Project",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${project?.aspectRatio?.label?.split(" ")?.first() ?: "9:16"} • ${(project?.durationMs ?: 0L) / 1000}s",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showShortsAssistantDialog = true }
                            .border(1.dp, NeonYellow, RoundedCornerShape(10.dp)),
                        color = Color(0xFF2B2513)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Shorts AI", color = NeonYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimeButton(
                        text = "Export",
                        onClick = {
                            if (project != null) {
                                onNavigateToExport(project.id)
                            }
                        },
                        icon = Icons.Default.Share,
                        modifier = Modifier.testTag("editor_export_cta_button")
                    )
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading || project == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Preview Player Canvas with 9:16 vertical view, active media, and live anime effects
                EditorCanvasPlayer(
                    aspectRatio = project.aspectRatio,
                    filter = project.activeFilter,
                    captions = project.captions,
                    currentPositionMs = state.currentPositionMs,
                    totalDurationMs = project.durationMs,
                    isPlaying = state.isPlaying,
                    onPlayPauseToggle = { viewModel.setPlaying(!state.isPlaying) },
                    onSeek = { viewModel.seekTo(it) },
                    activeClip = project.clips.getOrNull(state.selectedClipIndex)
                )

                // 2. Editor Function Tabs (Clips, Audio, FX, Captions, Filters, Canvas)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorTabChip(
                        label = "Clips",
                        icon = Icons.Default.VideoLibrary,
                        isSelected = state.activeTab == EditorTab.CLIPS,
                        onClick = { viewModel.selectTab(EditorTab.CLIPS) }
                    )
                    EditorTabChip(
                        label = "Anime FX",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = state.activeTab == EditorTab.EFFECTS,
                        onClick = { viewModel.selectTab(EditorTab.EFFECTS) }
                    )
                    EditorTabChip(
                        label = "Audio & AI Voice",
                        icon = Icons.Default.Mic,
                        isSelected = state.activeTab == EditorTab.AUDIO,
                        onClick = { viewModel.selectTab(EditorTab.AUDIO) }
                    )
                    EditorTabChip(
                        label = "Anime Captions",
                        icon = Icons.Default.Subtitles,
                        isSelected = state.activeTab == EditorTab.CAPTIONS,
                        onClick = { viewModel.selectTab(EditorTab.CAPTIONS) }
                    )
                    EditorTabChip(
                        label = "Color Filters",
                        icon = Icons.Default.ColorLens,
                        isSelected = state.activeTab == EditorTab.FILTERS,
                        onClick = { viewModel.selectTab(EditorTab.FILTERS) }
                    )
                    EditorTabChip(
                        label = "Canvas (Ratio)",
                        icon = Icons.Default.AspectRatio,
                        isSelected = state.activeTab == EditorTab.CANVAS,
                        onClick = { viewModel.selectTab(EditorTab.CANVAS) }
                    )
                }

                // 3. Tab Contextual Content Panel
                when (state.activeTab) {
                    EditorTab.CLIPS -> {
                        val activeClip = project.clips.getOrNull(state.selectedClipIndex)
                        if (activeClip != null) {
                            ClipTrimPanel(
                                clip = activeClip,
                                onTrimChanged = { start, end ->
                                    viewModel.trimClip(state.selectedClipIndex, start, end)
                                }
                            )
                        }
                    }
                    EditorTab.EFFECTS -> {
                        val activeClip = project.clips.getOrNull(state.selectedClipIndex)
                        if (activeClip != null) {
                            EffectsPanel(
                                activeEffect = activeClip.effectType,
                                intensity = activeClip.effectIntensity,
                                durationMs = activeClip.effectDurationMs,
                                onEffectSelected = {
                                    viewModel.setClipEffect(state.selectedClipIndex, it, activeClip.effectIntensity, activeClip.effectDurationMs)
                                },
                                onIntensityChanged = {
                                    viewModel.setClipEffect(state.selectedClipIndex, activeClip.effectType, it, activeClip.effectDurationMs)
                                },
                                onDurationChanged = {
                                    viewModel.setClipEffect(state.selectedClipIndex, activeClip.effectType, activeClip.effectIntensity, it)
                                },
                                onApplyToAllToggle = { applyAll ->
                                    viewModel.setClipEffect(state.selectedClipIndex, activeClip.effectType, activeClip.effectIntensity, activeClip.effectDurationMs, applyToAll = applyAll)
                                },
                                applyToAll = false
                            )
                        }
                    }
                    EditorTab.AUDIO -> {
                        AudioControlPanel(
                            tracks = project.audioTracks,
                            onVolumeChanged = { trackId, vol -> viewModel.setAudioVolume(trackId, vol) },
                            onFadesChanged = { trackId, inMs, outMs -> viewModel.setAudioFades(trackId, inMs, outMs) },
                            onDeleteTrack = { trackId -> viewModel.deleteAudioTrack(trackId) },
                            onOpenAiVoice = { onNavigateToAiVoice(project.id) },
                            onImportAudioClick = { pickAudioLauncher.launch("audio/*") }
                        )
                    }
                    EditorTab.CAPTIONS -> {
                        CaptionsQuickBar(
                            captionsCount = project.captions.size,
                            onOpenCaptionsEditor = { onNavigateToCaptions(project.id) },
                            onOpenAiScript = { onNavigateToAiScript(project.id) }
                        )
                    }
                    EditorTab.FILTERS -> {
                        FiltersPanel(
                            activeFilter = project.activeFilter,
                            onFilterSelected = { viewModel.setProjectFilter(it) }
                        )
                    }
                    EditorTab.CANVAS -> {
                        CanvasRatioPanel(
                            activeAspect = project.aspectRatio,
                            onAspectSelected = { viewModel.setAspectRatio(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 4. Multi-Track Timeline
                EditorTimeline(
                    clips = project.clips,
                    audioTracks = project.audioTracks,
                    captions = project.captions,
                    selectedClipIndex = state.selectedClipIndex,
                    onSelectClip = { viewModel.selectClip(it) },
                    onAddClipClick = {
                        pickVideosLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    onSplitClip = { viewModel.splitClip(state.selectedClipIndex) },
                    onDuplicateClip = { viewModel.duplicateClip(state.selectedClipIndex) },
                    onDeleteClip = { viewModel.deleteClip(state.selectedClipIndex) },
                    onSpeedClick = { showSpeedDialog = true },
                    onTransitionsClick = { showTransitionDialog = true },
                    onMoveClip = { from, to -> viewModel.moveClip(from, to) },
                    onUndoClick = { viewModel.undo() },
                    onRedoClick = { viewModel.redo() },
                    canUndo = state.canUndo,
                    canRedo = state.canRedo
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialogs
    if (showSpeedDialog) {
        val currentSpeed = project?.clips?.getOrNull(state.selectedClipIndex)?.speed ?: 1.0f
        SpeedDialog(
            currentSpeed = currentSpeed,
            onSpeedSelected = { viewModel.setClipSpeed(state.selectedClipIndex, it) },
            onDismiss = { showSpeedDialog = false }
        )
    }

    if (showTransitionDialog) {
        val currentTr = project?.clips?.getOrNull(state.selectedClipIndex)?.transition ?: com.example.data.models.AnimeTransition.CUT
        TransitionDialog(
            currentTransition = currentTr,
            onTransitionSelected = { viewModel.setClipTransition(state.selectedClipIndex, it) },
            onDismiss = { showTransitionDialog = false }
        )
    }
}

@Composable
private fun EditorTabChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                if (isSelected) NeonCyan else Color(0xFF2C325A),
                RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) Color(0xFF1F294E) else Color(0xFF151833)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NeonCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AudioQuickBar(
    onOpenAiVoice: () -> Unit,
    audioTracksCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF171A38)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Audio & Narration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("$audioTracksCount track(s) in timeline", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            AnimeButton(
                text = "Generate AI Voice",
                onClick = onOpenAiVoice,
                icon = Icons.Default.AutoAwesome,
                testTag = "open_ai_voice_button"
            )
        }
    }
}

@Composable
private fun CaptionsQuickBar(
    captionsCount: Int,
    onOpenCaptionsEditor: () -> Unit,
    onOpenAiScript: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF171A38)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Anime Subtitles", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("$captionsCount cues formatted", color = NeonCyan, fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimeButton(
                    text = "AI Script",
                    onClick = onOpenAiScript,
                    icon = Icons.Default.AutoAwesome,
                    testTag = "open_ai_script_button"
                )
                AnimeButton(
                    text = "Edit Subtitles",
                    onClick = onOpenCaptionsEditor,
                    icon = Icons.Default.Subtitles,
                    testTag = "open_captions_button"
                )
            }
        }
    }
}

@Composable
private fun CanvasRatioPanel(
    activeAspect: AspectRatio,
    onAspectSelected: (AspectRatio) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AspectRatio.entries.forEach { aspect ->
            val isSelected = aspect == activeAspect
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAspectSelected(aspect) }
                    .border(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) NeonCyan else Color(0xFF2E3460),
                        RoundedCornerShape(12.dp)
                    ),
                color = if (isSelected) Color(0xFF1C274E) else Color(0xFF151833)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = aspect.label.split(" ").first(),
                        color = if (isSelected) NeonCyan else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = aspect.label.substringAfter("(").removeSuffix(")"),
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
