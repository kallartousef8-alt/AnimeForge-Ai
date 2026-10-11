package com.example.ui.script

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.material.icons.filled.ContentCut
import com.example.data.models.GeneratedScript
import com.example.data.models.ScriptCategory
import com.example.data.models.ScriptLanguage
import com.example.data.models.ScriptStyle
import com.example.service.AiScriptService
import com.example.ui.components.AnimeButton
import com.example.ui.components.AnimeCard
import com.example.ui.components.AnimeChip
import com.example.ui.components.AnimeHeader
import com.example.ui.components.GlowBadge
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import kotlinx.coroutines.launch

@Composable
fun AiScriptScreen(
    projectId: Long?,
    onNavigateBack: () -> Unit,
    onSendToVoice: (scriptText: String, lang: String) -> Unit,
    onApplyCaptions: ((scriptText: String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scriptService = remember { AiScriptService() }

    var topic by remember { mutableStateOf("Jujutsu Kaisen") }
    var character by remember { mutableStateOf("Satoru Gojo") }
    var selectedCategory by remember { mutableStateOf(ScriptCategory.BATTLES) }
    var selectedDuration by remember { mutableIntStateOf(30) }
    var selectedStyle by remember { mutableStateOf(ScriptStyle.EPIC) }
    var selectedLanguage by remember { mutableStateOf(ScriptLanguage.ENGLISH) }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedScript by remember { mutableStateOf<GeneratedScript?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editedHook by remember { mutableStateOf("") }
    var editedBody by remember { mutableStateOf("") }
    var editedConclusion by remember { mutableStateOf("") }

    val durations = listOf(15, 30, 45, 60)

    fun doGenerate() {
        scope.launch {
            isGenerating = true
            val result = scriptService.generateScript(
                topic = topic,
                character = character,
                durationSeconds = selectedDuration,
                category = selectedCategory,
                style = selectedStyle,
                language = selectedLanguage
            )
            isGenerating = false
            result.onSuccess { script ->
                generatedScript = script
                editedHook = script.hook
                editedBody = script.body
                editedConclusion = script.conclusion
            }
        }
    }

    Scaffold(
        containerColor = AnimeBackgroundDark,
        topBar = {
            AnimeHeader(
                title = "AI Anime Scriptwriter",
                subtitle = "Generate viral Shorts narration arcs",
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
            // Input Card
            AnimeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ANIME TOPIC & CHARACTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Anime Name or Lore Topic") },
                        modifier = Modifier.fillMaxWidth().testTag("script_topic_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = Color(0xFF2E3258),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = character,
                        onValueChange = { character = it },
                        label = { Text("Key Character (Optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("script_character_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = Color(0xFF2E3258),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Anime Category
                    Text(
                        text = "ANIME CONTENT CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonYellow
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScriptCategory.entries.forEach { cat ->
                            AnimeChip(
                                text = cat.label,
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Duration Chips
                    Text(
                        text = "SHORTS DURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durations.forEach { dur ->
                            AnimeChip(
                                text = "${dur}s",
                                selected = selectedDuration == dur,
                                onClick = { selectedDuration = dur },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Style Chips
                    Text(
                        text = "NARRATION STYLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScriptStyle.entries.forEach { st ->
                            AnimeChip(
                                text = st.label,
                                selected = selectedStyle == st,
                                onClick = { selectedStyle = st }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Language Chips
                    Text(
                        text = "OUTPUT LANGUAGE & HINGLISH TRANSLATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScriptLanguage.entries.forEach { lang ->
                            AnimeChip(
                                text = lang.label,
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Generate Button
                    AnimeButton(
                        text = if (isGenerating) "Summoning Script..." else "Generate Anime Script",
                        onClick = { doGenerate() },
                        icon = Icons.Default.AutoAwesome,
                        enabled = !isGenerating && topic.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "generate_script_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading state
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NeonPurple)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing anime lore & pacing hooks...",
                            color = NeonCyan,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Generated Result Card
            val script = generatedScript
            if (script != null && !isGenerating) {
                AnimeCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonPurple
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GlowBadge(text = "${script.durationSeconds}s ${script.style.label}", color = NeonPink)
                                Spacer(modifier = Modifier.width(8.dp))
                                GlowBadge(text = script.language.label, color = NeonCyan)
                            }
                            IconButton(
                                onClick = { isEditing = !isEditing },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Script",
                                    tint = if (isEditing) NeonCyan else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Opening Hook
                        Text("1. OPENING HOOK (0 - 3s)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonYellow)
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedHook,
                                onValueChange = { editedHook = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        } else {
                            Text(
                                text = editedHook,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Main Story
                        Text("2. STORYLINE BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedBody,
                                onValueChange = { editedBody = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        } else {
                            Text(
                                text = editedBody,
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Conclusion / CTA
                        Text("3. CONCLUSION & CTA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonPink)
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedConclusion,
                                onValueChange = { editedConclusion = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        } else {
                            Text(
                                text = editedConclusion,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Micro Actions: Regenerate, Shorten, Expand, Copy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ScriptMicroButton(
                                icon = Icons.Default.Refresh,
                                label = "Regen",
                                onClick = { doGenerate() }
                            )
                            ScriptMicroButton(
                                icon = Icons.Default.ContentCut,
                                label = "Shorten",
                                onClick = {
                                    scope.launch {
                                        val shortened = scriptService.shortenScript(script)
                                        generatedScript = shortened
                                        editedBody = shortened.body
                                    }
                                }
                            )
                            ScriptMicroButton(
                                icon = Icons.Default.Expand,
                                label = "Expand",
                                onClick = {
                                    scope.launch {
                                        val expanded = scriptService.expandScript(script)
                                        generatedScript = expanded
                                        editedBody = expanded.body
                                    }
                                }
                            )
                            ScriptMicroButton(
                                icon = Icons.Default.ContentCopy,
                                label = "Copy",
                                onClick = {
                                    val full = "$editedHook\n\n$editedBody\n\n$editedConclusion"
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Anime Script", full))
                                    Toast.makeText(context, "Script copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Primary Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AnimeButton(
                                text = "Send to Voice",
                                onClick = {
                                    val full = "$editedHook $editedBody $editedConclusion"
                                    onSendToVoice(full, script.language.code)
                                },
                                icon = Icons.Default.Mic,
                                modifier = Modifier.weight(1f),
                                testTag = "script_send_to_voice_button"
                            )

                            if (onApplyCaptions != null) {
                                AnimeButton(
                                    text = "To Captions",
                                    onClick = {
                                        val full = "$editedHook $editedBody $editedConclusion"
                                        onApplyCaptions(full)
                                    },
                                    icon = Icons.Default.Subtitles,
                                    modifier = Modifier.weight(1f),
                                    testTag = "script_apply_captions_button"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScriptMicroButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .border(0.8.dp, Color(0xFF2C325A), RoundedCornerShape(8.dp)),
        color = Color(0xFF191D3C)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
