package com.example.ui.captions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CaptionItem
import com.example.data.models.CaptionStylePreset
import com.example.service.CaptionService
import com.example.ui.components.AnimeButton
import com.example.ui.components.AnimeCard
import com.example.ui.components.AnimeHeader
import com.example.ui.components.GlowBadge
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import java.io.File
import java.util.UUID

@Composable
fun CaptionsScreen(
    initialCaptions: List<CaptionItem>,
    projectDurationMs: Long,
    onNavigateBack: () -> Unit,
    onApplyCaptions: (List<CaptionItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val captionService = remember { CaptionService() }

    var captionsList by remember { mutableStateOf(initialCaptions) }
    var selectedPreset by remember { mutableStateOf(initialCaptions.firstOrNull()?.style ?: CaptionStylePreset.NEON_GLOW) }
    var fontSize by remember { mutableFloatStateOf(initialCaptions.firstOrNull()?.fontSizeSp ?: 22f) }
    var positionRatio by remember { mutableFloatStateOf(initialCaptions.firstOrNull()?.yPositionRatio ?: 0.78f) }
    var outlineWidth by remember { mutableFloatStateOf(initialCaptions.firstOrNull()?.outlineWidthSp ?: 3.5f) }
    var shadowRadius by remember { mutableFloatStateOf(initialCaptions.firstOrNull()?.shadowRadius ?: 5.0f) }
    var highlightKeywords by remember { mutableStateOf(initialCaptions.firstOrNull()?.highlightKeywords ?: true) }

    var autoGenText by remember { mutableStateOf("") }
    var showAutoGenDialog by remember { mutableStateOf(false) }

    // Speech-to-Text Recognition Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
            val newCues = captionService.generateCaptionsFromText(
                text = spoken,
                totalDurationMs = projectDurationMs,
                preset = selectedPreset,
                fontSizeSp = fontSize
            ).map {
                it.copy(
                    yPositionRatio = positionRatio,
                    outlineWidthSp = outlineWidth,
                    shadowRadius = shadowRadius,
                    highlightKeywords = highlightKeywords
                )
            }
            captionsList = newCues
            Toast.makeText(context, "Transcribed ${newCues.size} synchronized caption cues!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = AnimeBackgroundDark,
        topBar = {
            AnimeHeader(
                title = "Anime Captions Studio",
                subtitle = "${captionsList.size} Subtitle Cues • Synchronized Timing",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Style Presets Carousel
            Text(
                text = "SUBTITLE STYLING PRESET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CaptionStylePreset.entries.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedPreset = preset
                                captionsList = captionsList.map { it.copy(style = preset) }
                            }
                            .border(
                                1.dp,
                                if (isSelected) NeonPink else Color(0xFF2C325A),
                                RoundedCornerShape(10.dp)
                            ),
                        color = if (isSelected) Color(0xFF241635) else Color(0xFF141733)
                    ) {
                        Text(
                            text = preset.label,
                            color = if (isSelected) NeonPink else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle Fine-Tuning Controls (Font Size, Position, Outline, Highlight)
            AnimeCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonPurple
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Font Size: ${fontSize.toInt()}sp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (positionRatio > 0.7f) "Bottom Safe Zone" else if (positionRatio < 0.4f) "Top Zone" else "Center",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                    Slider(
                        value = fontSize,
                        onValueChange = {
                            fontSize = it
                            captionsList = captionsList.map { c -> c.copy(fontSizeSp = it) }
                        },
                        valueRange = 14f..36f,
                        colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPurple)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vertical Position", color = Color.White, fontSize = 12.sp)
                        Text(String.format("%.0f%%", positionRatio * 100), color = NeonYellow, fontSize = 11.sp)
                    }
                    Slider(
                        value = positionRatio,
                        onValueChange = {
                            positionRatio = it
                            captionsList = captionsList.map { c -> c.copy(yPositionRatio = it) }
                        },
                        valueRange = 0.2f..0.88f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurple)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Highlight Viral Words", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            GlowBadge(text = "ANIMATED", color = NeonPink)
                        }
                        Switch(
                            checked = highlightKeywords,
                            onCheckedChange = {
                                highlightKeywords = it
                                captionsList = captionsList.map { c -> c.copy(highlightKeywords = it) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonPink,
                                checkedTrackColor = Color(0xFF38153B)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle Cues Quick Actions (Speech Recognition, Auto-Gen from text, Add Cue, Export SRT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIMED CUES (${captionsList.size})",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Speech Recognition
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak anime script to transcribe captions...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (_: Exception) {
                                    showAutoGenDialog = true
                                }
                            }
                            .border(0.8.dp, NeonPink, RoundedCornerShape(8.dp)),
                        color = Color(0xFF2D1432)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = NeonPink, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Speech", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Auto-Gen Text dialog
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAutoGenDialog = true }
                            .border(0.8.dp, NeonCyan, RoundedCornerShape(8.dp)),
                        color = Color(0xFF14243B)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Transcribe", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Export SRT
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val srt = captionService.exportToSrt(captionsList)
                                val srtFile = File(context.cacheDir, "AnimeForge_Captions.srt")
                                srtFile.writeText(srt)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("SRT Subtitles", srt))
                                Toast.makeText(context, "Exported SRT & copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .border(0.8.dp, Color(0xFF2C325A), RoundedCornerShape(8.dp)),
                        color = Color(0xFF161A3B)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SRT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Add Cue
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val lastEnd = captionsList.lastOrNull()?.endMs ?: 0L
                                val newCue = CaptionItem(
                                    id = UUID.randomUUID().toString(),
                                    text = "NEW ANIME CUE",
                                    startMs = lastEnd,
                                    endMs = (lastEnd + 2500L).coerceAtMost(projectDurationMs),
                                    style = selectedPreset,
                                    fontSizeSp = fontSize,
                                    yPositionRatio = positionRatio,
                                    outlineWidthSp = outlineWidth,
                                    shadowRadius = shadowRadius,
                                    highlightKeywords = highlightKeywords
                                )
                                captionsList = captionsList + newCue
                            }
                            .border(0.8.dp, NeonPurple, RoundedCornerShape(8.dp)),
                        color = Color(0xFF22173B)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Captions List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(captionsList, key = { _, item -> item.id }) { index, item ->
                    CaptionItemCard(
                        item = item,
                        index = index,
                        onTextChanged = { newText ->
                            captionsList = captionsList.toMutableList().also {
                                it[index] = it[index].copy(text = newText)
                            }
                        },
                        onDelete = {
                            captionsList = captionsList.filterIndexed { i, _ -> i != index }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save and Apply to Timeline CTA
            AnimeButton(
                text = "Apply Captions to Video Timeline",
                onClick = {
                    onApplyCaptions(captionsList)
                    Toast.makeText(context, "Applied ${captionsList.size} captions to timeline!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                icon = Icons.Default.Subtitles,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                testTag = "save_captions_timeline_button"
            )
        }

        if (showAutoGenDialog) {
            AlertDialog(
                onDismissRequest = { showAutoGenDialog = false },
                title = { Text("Transcribe Narration Text", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Paste your narration or script text. Captions will be split into high-impact timed cues synchronized across the video.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = autoGenText,
                            onValueChange = { autoGenText = it },
                            placeholder = { Text("Paste anime narration script here...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                containerColor = Color(0xFF141733),
                confirmButton = {
                    Button(
                        onClick = {
                            if (autoGenText.isNotBlank()) {
                                val generated = captionService.generateCaptionsFromText(
                                    text = autoGenText,
                                    totalDurationMs = projectDurationMs,
                                    preset = selectedPreset,
                                    fontSizeSp = fontSize
                                ).map {
                                    it.copy(
                                        yPositionRatio = positionRatio,
                                        outlineWidthSp = outlineWidth,
                                        shadowRadius = shadowRadius,
                                        highlightKeywords = highlightKeywords
                                    )
                                }
                                captionsList = generated
                                showAutoGenDialog = false
                                autoGenText = ""
                                Toast.makeText(context, "Generated ${generated.size} subtitle cues!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("Synchronize", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAutoGenDialog = false }) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}

@Composable
private fun CaptionItemCard(
    item: CaptionItem,
    index: Int,
    onTextChanged: (String) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF292E56), RoundedCornerShape(12.dp)),
        color = Color(0xFF141733)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(62.dp)) {
                Text("#${index + 1}", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${item.startMs / 1000}s - ${item.endMs / 1000}s",
                    color = NeonCyan,
                    fontSize = 10.sp
                )
            }

            OutlinedTextField(
                value = item.text,
                onValueChange = onTextChanged,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = Color(0xFF2E3258),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Cue",
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}
