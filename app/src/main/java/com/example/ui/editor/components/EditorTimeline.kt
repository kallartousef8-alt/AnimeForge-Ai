package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AudioTrackItem
import com.example.data.models.CaptionItem
import com.example.data.models.VideoClipItem
import com.example.ui.editor.EditorTab
import com.example.ui.theme.AnimeSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow

@Composable
fun EditorTimeline(
    clips: List<VideoClipItem>,
    audioTracks: List<AudioTrackItem>,
    captions: List<CaptionItem>,
    selectedClipIndex: Int,
    onSelectClip: (Int) -> Unit,
    onAddClipClick: () -> Unit,
    onSplitClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onSpeedClick: () -> Unit,
    onTransitionsClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onMoveClip: ((from: Int, to: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AnimeSurfaceDark)
            .padding(vertical = 8.dp)
    ) {
        // Quick Action Bar (Undo, Redo, Split, Duplicate, Speed, Delete, Move)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onUndoClick,
                    enabled = canUndo,
                    modifier = Modifier.size(36.dp).testTag("timeline_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else Color(0xFF475569)
                    )
                }
                IconButton(
                    onClick = onRedoClick,
                    enabled = canRedo,
                    modifier = Modifier.size(36.dp).testTag("timeline_redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else Color(0xFF475569)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                if (onMoveClip != null && clips.size > 1) {
                    TimelineToolButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = "Left",
                        onClick = {
                            if (selectedClipIndex > 0) onMoveClip(selectedClipIndex, selectedClipIndex - 1)
                        },
                        tag = "timeline_move_left_button"
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    TimelineToolButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        label = "Right",
                        onClick = {
                            if (selectedClipIndex < clips.size - 1) onMoveClip(selectedClipIndex, selectedClipIndex + 1)
                        },
                        tag = "timeline_move_right_button"
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
                TimelineToolButton(
                    icon = Icons.AutoMirrored.Filled.CallSplit,
                    label = "Split",
                    onClick = onSplitClip,
                    tag = "timeline_split_button"
                )
                Spacer(modifier = Modifier.width(5.dp))
                TimelineToolButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy",
                    onClick = onDuplicateClip,
                    tag = "timeline_duplicate_button"
                )
                Spacer(modifier = Modifier.width(5.dp))
                TimelineToolButton(
                    icon = Icons.Default.Speed,
                    label = "Speed",
                    onClick = onSpeedClick,
                    tag = "timeline_speed_button"
                )
                Spacer(modifier = Modifier.width(5.dp))
                TimelineToolButton(
                    icon = Icons.Default.Transform,
                    label = "Cut FX",
                    onClick = onTransitionsClick,
                    tag = "timeline_transition_button"
                )
                Spacer(modifier = Modifier.width(5.dp))
                TimelineToolButton(
                    icon = Icons.Default.Delete,
                    label = "Del",
                    onClick = onDeleteClip,
                    tag = "timeline_delete_button",
                    tint = NeonPink
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Track 1: Video Clips Track
        Text(
            text = "VIDEO TRACK (${clips.size} CLIPS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            clips.forEachIndexed { index, clip ->
                val isSelected = index == selectedClipIndex
                ClipTimelineCard(
                    clip = clip,
                    index = index,
                    isSelected = isSelected,
                    onClick = { onSelectClip(index) }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Add clip button card
            Surface(
                modifier = Modifier
                    .height(68.dp)
                    .width(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAddClipClick)
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                color = Color(0xFF19203F)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Clip",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Add", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Track 2: Audio & Voiceover Track
        if (audioTracks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AUDIO & VOICE (${audioTracks.size} TRACKS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                audioTracks.forEach { track ->
                    Surface(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (track.isVoiceover) NeonPink else NeonPurple, RoundedCornerShape(8.dp)),
                        color = Color(0xFF1E1738)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (track.isVoiceover) Icons.Default.Mic else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (track.isVoiceover) NeonPink else NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = track.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // Track 3: Captions Track preview
        if (captions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ANIME CAPTIONS (${captions.size} CUES)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                captions.take(8).forEach { cap ->
                    Surface(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(0.8.dp, NeonYellow.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                        color = Color(0xFF262312)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = null,
                                tint = NeonYellow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cap.text,
                                color = Color(0xFFFEF08A),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
private fun ClipTimelineCard(
    clip: VideoClipItem,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) NeonCyan else Color(0xFF2E3258)
    val widthDp = (clip.trimmedDurationMs / 60).coerceIn(90L, 160L).toInt().dp

    Surface(
        modifier = Modifier
            .height(68.dp)
            .width(widthDp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(10.dp)),
        color = if (isSelected) Color(0xFF1B2247) else Color(0xFF13162F)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}. ${clip.title}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (clip.speed != 1.0f) {
                    Text(
                        text = "${clip.speed}x",
                        color = NeonPink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${clip.trimmedDurationMs / 1000}s",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
                if (clip.filter != com.example.data.models.AnimeFilter.NONE) {
                    Text(
                        text = clip.filter.label.take(6),
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String,
    tint: Color = Color(0xFFCBD5E1)
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .border(0.8.dp, Color(0xFF2C325A), RoundedCornerShape(8.dp)),
        color = Color(0xFF181B38)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
