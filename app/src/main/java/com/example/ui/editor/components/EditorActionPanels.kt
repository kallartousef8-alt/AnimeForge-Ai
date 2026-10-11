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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AnimeFilter
import com.example.data.models.AnimeTransition
import com.example.data.models.AspectRatio
import com.example.data.models.VideoClipItem
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple

@Composable
fun ClipTrimPanel(
    clip: VideoClipItem,
    onTrimChanged: (startMs: Long, endMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember(clip.id) {
        mutableStateOf(clip.startTrimMs.toFloat()..clip.endTrimMs.toFloat())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF13162E))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Trim: ${clip.title}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(sliderPosition.start / 1000).toInt()}s - ${(sliderPosition.endInclusive / 1000).toInt()}s (${(clip.trimmedDurationMs / 1000)}s total)",
                color = NeonCyan,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        RangeSlider(
            value = sliderPosition,
            onValueChange = { range ->
                sliderPosition = range
            },
            onValueChangeFinished = {
                onTrimChanged(sliderPosition.start.toLong(), sliderPosition.endInclusive.toLong())
            },
            valueRange = 0f..clip.durationMs.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = NeonPink,
                activeTrackColor = NeonPurple,
                inactiveTrackColor = Color(0xFF2C325A)
            )
        )
    }
}

@Composable
fun SpeedDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Clip Speed", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Select playback speed for the active anime clip:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    speeds.take(3).forEach { spd ->
                        SpeedChip(speed = spd, isSelected = currentSpeed == spd) {
                            onSpeedSelected(spd)
                            onDismiss()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    speeds.drop(3).forEach { spd ->
                        SpeedChip(speed = spd, isSelected = currentSpeed == spd) {
                            onSpeedSelected(spd)
                            onDismiss()
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF141733),
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun SpeedChip(
    speed: Float,
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
        color = if (isSelected) Color(0xFF1C274E) else Color(0xFF191D3C)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${speed}x",
                color = if (isSelected) NeonCyan else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun TransitionDialog(
    currentTransition: AnimeTransition,
    onTransitionSelected: (AnimeTransition) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clip Transition FX", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                AnimeTransition.entries.forEach { tr ->
                    val isSelected = tr == currentTransition
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onTransitionSelected(tr)
                                onDismiss()
                            }
                            .border(
                                1.dp,
                                if (isSelected) NeonPink else Color(0xFF2A2F57),
                                RoundedCornerShape(10.dp)
                            ),
                        color = if (isSelected) Color(0xFF2D1637) else Color(0xFF181B3C)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr.label,
                                color = if (isSelected) NeonPink else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF141733),
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun FiltersPanel(
    activeFilter: AnimeFilter,
    onFilterSelected: (AnimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF13162E))
            .padding(12.dp)
    ) {
        Text(
            text = "ANIME COLOR FILTERS",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimeFilter.entries.forEach { filter ->
                val isSelected = filter == activeFilter
                Surface(
                    modifier = Modifier
                        .width(108.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onFilterSelected(filter) }
                        .border(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) NeonPurple else Color(0xFF2E3460),
                            RoundedCornerShape(12.dp)
                        ),
                    color = if (isSelected) Color(0xFF261D47) else Color(0xFF171A38)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (filter) {
                                        AnimeFilter.CYBERPUNK_NEON -> NeonCyan
                                        AnimeFilter.SAKURA_DUSK -> NeonPink
                                        AnimeFilter.MOONLIGHT_SHADOW -> Color(0xFF38BDF8)
                                        AnimeFilter.MANGA_NOIR -> Color.White
                                        AnimeFilter.GOLDEN_AURA -> Color(0xFFFFD600)
                                        AnimeFilter.VIBRANT_SHONEN -> NeonPurple
                                        else -> Color.Gray
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = filter.label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EffectsPanel(
    activeEffect: com.example.data.models.AnimeEffectType,
    intensity: Float,
    durationMs: Long,
    onEffectSelected: (com.example.data.models.AnimeEffectType) -> Unit,
    onIntensityChanged: (Float) -> Unit,
    onDurationChanged: (Long) -> Unit,
    onApplyToAllToggle: (Boolean) -> Unit,
    applyToAll: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF13162E))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ANIME EFFECTS LIBRARY",
                color = NeonPink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("All Clips", color = Color(0xFF94A3B8), fontSize = 11.sp)
                androidx.compose.material3.Switch(
                    checked = applyToAll,
                    onCheckedChange = onApplyToAllToggle,
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = NeonPink,
                        checkedTrackColor = Color(0xFF38153B)
                    ),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Effects Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            com.example.data.models.AnimeEffectType.entries.forEach { effect ->
                val isSelected = effect == activeEffect
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEffectSelected(effect) }
                        .border(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) NeonPink else Color(0xFF2E3460),
                            RoundedCornerShape(12.dp)
                        ),
                    color = if (isSelected) Color(0xFF2C1638) else Color(0xFF171A38)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = effect.label,
                            color = if (isSelected) NeonPink else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (activeEffect != com.example.data.models.AnimeEffectType.NONE) {
            Spacer(modifier = Modifier.height(12.dp))

            // Intensity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Effect Intensity", color = Color.White, fontSize = 12.sp)
                Text(String.format("%.0f%%", intensity * 100), color = NeonCyan, fontSize = 11.sp)
            }
            androidx.compose.material3.Slider(
                value = intensity,
                onValueChange = onIntensityChanged,
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurple)
            )

            // Duration Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Effect Window", color = Color.White, fontSize = 12.sp)
                Text("${durationMs / 1000f}s", color = NeonPink, fontSize = 11.sp)
            }
            androidx.compose.material3.Slider(
                value = durationMs.toFloat(),
                onValueChange = { onDurationChanged(it.toLong()) },
                valueRange = 500f..5000f,
                colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPurple)
            )
        }
    }
}

@Composable
fun AudioControlPanel(
    tracks: List<com.example.data.models.AudioTrackItem>,
    onVolumeChanged: (trackId: String, volume: Float) -> Unit,
    onFadesChanged: (trackId: String, fadeInMs: Long, fadeOutMs: Long) -> Unit,
    onDeleteTrack: (trackId: String) -> Unit,
    onOpenAiVoice: () -> Unit,
    onImportAudioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF13162E))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AUDIO & VOICE MIXER (${tracks.size})",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onImportAudioClick)
                        .border(1.dp, NeonCyan, RoundedCornerShape(8.dp)),
                    color = Color(0xFF142542)
                ) {
                    Text("+ Music/SFX", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenAiVoice)
                        .border(1.dp, NeonPink, RoundedCornerShape(8.dp)),
                    color = Color(0xFF2C1638)
                ) {
                    Text("+ AI Voice", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (tracks.isEmpty()) {
            Text(
                text = "No audio tracks attached. Generate AI narration or import royalty-free music.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            tracks.forEach { track ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF292E56), RoundedCornerShape(10.dp)),
                    color = Color(0xFF171A3B)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = track.title,
                                color = if (track.isVoiceover) NeonPink else NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.material3.IconButton(
                                onClick = { onDeleteTrack(track.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Remove",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Volume slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Volume", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(String.format("%.0f%%", track.volume * 100), color = Color.White, fontSize = 11.sp)
                        }
                        androidx.compose.material3.Slider(
                            value = track.volume,
                            onValueChange = { onVolumeChanged(track.id, it) },
                            valueRange = 0f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurple)
                        )

                        // Fade in / out quick chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fade In: ${track.fadeInMs / 1000f}s • Fade Out: ${track.fadeOutMs / 1000f}s",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val newIn = if (track.fadeInMs == 0L) 1000L else 0L
                                            onFadesChanged(track.id, newIn, track.fadeOutMs)
                                        }
                                        .border(0.8.dp, Color(0xFF3B4372), RoundedCornerShape(6.dp)),
                                    color = if (track.fadeInMs > 0) Color(0xFF1D2852) else Color(0xFF131733)
                                ) {
                                    Text("Fade In", color = if (track.fadeInMs > 0) NeonCyan else Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val newOut = if (track.fadeOutMs == 0L) 1000L else 0L
                                            onFadesChanged(track.id, track.fadeInMs, newOut)
                                        }
                                        .border(0.8.dp, Color(0xFF3B4372), RoundedCornerShape(6.dp)),
                                    color = if (track.fadeOutMs > 0) Color(0xFF2E193C) else Color(0xFF131733)
                                ) {
                                    Text("Fade Out", color = if (track.fadeOutMs > 0) NeonPink else Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
