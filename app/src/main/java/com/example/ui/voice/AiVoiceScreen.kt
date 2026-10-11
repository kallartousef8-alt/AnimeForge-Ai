package com.example.ui.voice

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.models.AnimeVoiceStyle
import com.example.data.models.AudioTrackItem
import com.example.service.AiVoiceService
import com.example.ui.components.AnimeButton
import com.example.ui.components.AnimeCard
import com.example.ui.components.AnimeHeader
import com.example.ui.components.GlowBadge
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import kotlinx.coroutines.launch

@Composable
fun AiVoiceScreen(
    initialText: String = "",
    initialLang: String = "en",
    projectId: Long?,
    onNavigateBack: () -> Unit,
    onAudioTrackCreated: ((AudioTrackItem) -> Unit)?,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val voiceService = remember { AiVoiceService(context) }

    DisposableEffect(Unit) {
        onDispose {
            voiceService.stopPlayback()
            voiceService.shutdown()
        }
    }

    var narrationText by remember {
        mutableStateOf(
            if (initialText.isNotBlank()) initialText
            else "When the seal broke, everyone realized they were looking at a living god. Is this the most unstoppable awakening in anime history?"
        )
    }

    var selectedVoice by remember { mutableStateOf(AnimeVoiceStyle.ENERGETIC_HYPE) }
    var pitchSlider by remember { mutableFloatStateOf(1.0f) }
    var speedSlider by remember { mutableFloatStateOf(1.0f) }

    var isPreviewing by remember { mutableStateOf(false) }
    var isSynthesizing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AnimeBackgroundDark,
        topBar = {
            AnimeHeader(
                title = "AI Anime Voiceover",
                subtitle = "Studio quality text-to-speech engine",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Engine Provider Status Notice
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF2E3466), RoundedCornerShape(10.dp)),
                color = Color(0xFF131738)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlowBadge(text = "TTS Ready", color = NeonCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Device Neural Speech Engine active. Audio will be saved directly to your project timeline.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // Text Input Card
            AnimeCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NARRATION SCRIPT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = narrationText,
                        onValueChange = { narrationText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("voice_script_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = Color(0xFF2E3258),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text("Enter or paste anime voiceover script...") }
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${narrationText.trim().split("\\s+".toRegex()).size} words • ~${(narrationText.trim().split("\\s+".toRegex()).size / 2.5f).toInt()}s spoken duration",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Narrator Character Selection
            Text(
                text = "CHOOSE ANIME NARRATOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimeVoiceStyle.entries.forEach { style ->
                val isSelected = style == selectedVoice
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selectedVoice = style
                            voiceService.stopPlayback()
                            isPreviewing = false
                        }
                        .border(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) NeonPink else Color(0xFF282C52),
                            RoundedCornerShape(12.dp)
                        ),
                    color = if (isSelected) Color(0xFF251633) else Color(0xFF141731)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = style.title,
                                color = if (isSelected) NeonPink else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = style.subtitle,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        if (isSelected) {
                            GlowBadge(text = "Active", color = NeonPink)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Fine-Tuning Sliders (Pitch & Speed)
            AnimeCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VOICE FINE-TUNING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonYellow
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Pitch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", color = Color.White, fontSize = 13.sp)
                        Text(String.format("%.2fx", pitchSlider), color = NeonCyan, fontSize = 12.sp)
                    }
                    Slider(
                        value = pitchSlider,
                        onValueChange = { pitchSlider = it },
                        valueRange = 0.6f..1.6f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurple)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate (Speed)", color = Color.White, fontSize = 13.sp)
                        Text(String.format("%.2fx", speedSlider), color = NeonPink, fontSize = 12.sp)
                    }
                    Slider(
                        value = speedSlider,
                        onValueChange = { speedSlider = it },
                        valueRange = 0.6f..1.8f,
                        colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPurple)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Preview & Generate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preview Voice Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (isPreviewing) {
                                voiceService.stopPlayback()
                                isPreviewing = false
                            } else {
                                isPreviewing = true
                                voiceService.previewVoice(
                                    text = narrationText,
                                    voiceStyle = selectedVoice,
                                    pitchMultiplier = pitchSlider,
                                    speedMultiplier = speedSlider,
                                    languageCode = initialLang
                                )
                            }
                        }
                        .border(1.dp, NeonCyan, RoundedCornerShape(14.dp)),
                    color = Color(0xFF13223D)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPreviewing) "Stop Audio" else "Preview Voice",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Generate & Add to Timeline
                AnimeButton(
                    text = if (isSynthesizing) "Generating..." else "Add to Timeline",
                    onClick = {
                        scope.launch {
                            isSynthesizing = true
                            voiceService.stopPlayback()
                            val result = voiceService.synthesizeToAudioTrack(
                                text = narrationText,
                                voiceStyle = selectedVoice,
                                pitchMultiplier = pitchSlider,
                                speedMultiplier = speedSlider,
                                languageCode = initialLang
                            )
                            isSynthesizing = false
                            result.onSuccess { track ->
                                Toast.makeText(context, "Voiceover generated and attached to timeline!", Toast.LENGTH_SHORT).show()
                                onAudioTrackCreated?.invoke(track)
                                onNavigateBack()
                            }.onFailure { err ->
                                Toast.makeText(context, "Voice generation notice: ${err.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    icon = Icons.Default.Add,
                    enabled = !isSynthesizing && narrationText.isNotBlank(),
                    modifier = Modifier.weight(1.3f),
                    testTag = "voice_generate_timeline_button"
                )
            }
        }
    }
}
