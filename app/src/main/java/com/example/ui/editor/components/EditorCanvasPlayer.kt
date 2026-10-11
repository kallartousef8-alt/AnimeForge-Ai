package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.AnimeEffectType
import com.example.data.models.AnimeFilter
import com.example.data.models.AspectRatio
import com.example.data.models.CaptionItem
import com.example.data.models.VideoClipItem
import com.example.ui.components.GlowBadge
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EditorCanvasPlayer(
    aspectRatio: AspectRatio,
    filter: AnimeFilter,
    captions: List<CaptionItem>,
    currentPositionMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    activeClip: VideoClipItem? = null,
    modifier: Modifier = Modifier
) {
    // Timer ticker when playing
    LaunchedEffect(isPlaying, totalDurationMs) {
        if (isPlaying) {
            var pos = currentPositionMs
            while (true) {
                delay(50)
                pos += 50
                if (pos >= totalDurationMs) {
                    onSeek(0L)
                    onPlayPauseToggle()
                    break
                } else {
                    onSeek(pos)
                }
            }
        }
    }

    var showControls by remember { mutableStateOf(true) }
    var previewEffectsEnabled by remember { mutableStateOf(true) }

    val effect = if (previewEffectsEnabled) activeClip?.effectType ?: AnimeEffectType.NONE else AnimeEffectType.NONE
    val intensity = activeClip?.effectIntensity ?: 0.6f
    val timeSec = currentPositionMs / 1000f

    // Calculate dynamic effect transform modifiers
    val shakeOffsetX = if (isPlaying && effect == AnimeEffectType.SHAKE) (sin(timeSec.toDouble() * 35.0) * intensity * 14).toFloat().dp else 0.dp
    val shakeOffsetY = if (isPlaying && effect == AnimeEffectType.SHAKE) (cos(timeSec.toDouble() * 30.0) * intensity * 14).toFloat().dp else 0.dp

    val scaleMultiplier = when {
        effect == AnimeEffectType.ZOOM_IN -> 1.0f + (intensity * 0.25f)
        effect == AnimeEffectType.ZOOM_OUT -> 1.0f + ((1f - intensity) * 0.25f)
        effect == AnimeEffectType.BEAT_SYNC && isPlaying -> 1.0f + (sin(timeSec.toDouble() * 10.0).toFloat() * 0.5f + 0.5f) * intensity * 0.12f
        else -> 1.0f
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video Preview Screen Canvas
        Box(
            modifier = Modifier
                .height(290.dp)
                .aspectRatio(aspectRatio.ratio)
                .clip(RoundedCornerShape(16.dp))
                .background(AnimeBackgroundDark)
                .border(
                    if (effect == AnimeEffectType.GLOW) 2.5.dp else 1.5.dp,
                    if (effect == AnimeEffectType.GLOW) NeonCyan else NeonPurple.copy(alpha = 0.7f),
                    RoundedCornerShape(16.dp)
                )
                .clickable { showControls = !showControls }
                .testTag("preview_player_canvas"),
            contentAlignment = Alignment.Center
        ) {
            // Media Container Box with transform effects
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scaleMultiplier)
                    .offset(shakeOffsetX, shakeOffsetY),
                contentAlignment = Alignment.Center
            ) {
                // If clip has actual media URI, render AsyncImage from Coil
                val mediaUri = activeClip?.uriString
                if (!mediaUri.isNullOrBlank() && (mediaUri.startsWith("content://") || mediaUri.startsWith("file://") || mediaUri.startsWith("http"))) {
                    AsyncImage(
                        model = mediaUri,
                        contentDescription = "Selected media clip",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Stylized anime canvas background gradient depending on filter
                    val gradientColors = when (filter) {
                        AnimeFilter.CYBERPUNK_NEON -> listOf(Color(0xFF0F084B), Color(0xFF26408B), Color(0xFF0D0221))
                        AnimeFilter.SAKURA_DUSK -> listOf(Color(0xFF2B0938), Color(0xFF541348), Color(0xFF87255B))
                        AnimeFilter.MOONLIGHT_SHADOW -> listOf(Color(0xFF030A1C), Color(0xFF061838), Color(0xFF0B2559))
                        AnimeFilter.MANGA_NOIR -> listOf(Color(0xFF111111), Color(0xFF222222), Color(0xFF000000))
                        AnimeFilter.GOLDEN_AURA -> listOf(Color(0xFF241202), Color(0xFF4A2503), Color(0xFF1E0E00))
                        else -> listOf(Color(0xFF141838), Color(0xFF201B4B), Color(0xFF0A0B1A))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(gradientColors))
                    )

                    // Dynamic anime energy circle in canvas
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Overlay Anime Flash effect
                if (isPlaying && effect == AnimeEffectType.FLASH) {
                    val flashAlpha = ((sin(timeSec.toDouble() * 8.0).toFloat() * 0.5f + 0.5f) * intensity * 0.8f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = flashAlpha))
                    )
                }

                // Overlay Cinematic Bars
                if (effect == AnimeEffectType.CINEMATIC_BARS) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(26.dp).background(Color.Black))
                        Box(modifier = Modifier.fillMaxWidth().height(26.dp).background(Color.Black))
                    }
                }
            }

            // Top Badges (Aspect, Filter, FX & Before/After Toggle)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    GlowBadge(text = aspectRatio.label.split(" ").first(), color = NeonCyan)
                    if (filter != AnimeFilter.NONE) {
                        GlowBadge(text = filter.label, color = NeonPink)
                    }
                    if (effect != AnimeEffectType.NONE) {
                        GlowBadge(text = effect.label, color = NeonPurple)
                    }
                }

                // Before/After FX toggle
                if (activeClip?.effectType != null && activeClip.effectType != AnimeEffectType.NONE) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { previewEffectsEnabled = !previewEffectsEnabled }
                            .border(0.8.dp, if (previewEffectsEnabled) NeonCyan else Color.Gray, RoundedCornerShape(6.dp)),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = if (previewEffectsEnabled) "FX: ON" else "FX: OFF",
                            color = if (previewEffectsEnabled) NeonCyan else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Active Anime Subtitle Overlay
            val activeCaption = captions.find { currentPositionMs in it.startMs..it.endMs }
            if (activeCaption != null && activeCaption.text.isNotBlank()) {
                val capText = if (activeCaption.style.isUppercase) activeCaption.text.uppercase() else activeCaption.text
                val yOffsetRatio = activeCaption.yPositionRatio.coerceIn(0.1f, 0.9f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = when {
                        yOffsetRatio < 0.35f -> Alignment.TopCenter
                        yOffsetRatio > 0.65f -> Alignment.BottomCenter
                        else -> Alignment.Center
                    }
                ) {
                    Text(
                        text = capText,
                        color = try { Color(android.graphics.Color.parseColor(activeCaption.style.primaryColorHex)) } catch (_: Exception) { Color.Yellow },
                        fontSize = activeCaption.fontSizeSp.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = if (yOffsetRatio > 0.65f) 20.dp else 0.dp, top = if (yOffsetRatio < 0.35f) 35.dp else 0.dp)
                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Center Play/Pause button on touch
            if (showControls || !isPlaying) {
                IconButton(
                    onClick = onPlayPauseToggle,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .testTag("player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Playback Scrub Slider & Time Counters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(currentPositionMs),
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Slider(
                value = if (totalDurationMs > 0) (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                onValueChange = { frac ->
                    onSeek((frac * totalDurationMs).toLong())
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("timeline_seekbar"),
                colors = SliderDefaults.colors(
                    thumbColor = NeonPink,
                    activeTrackColor = NeonPurple,
                    inactiveTrackColor = Color(0xFF2A2E54)
                )
            )

            Text(
                text = formatDuration(totalDurationMs),
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
