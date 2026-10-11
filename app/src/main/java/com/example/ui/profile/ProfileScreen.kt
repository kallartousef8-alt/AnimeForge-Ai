package com.example.ui.profile

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.AnimeCard
import com.example.ui.components.AnimeHeader
import com.example.ui.components.GlowBadge
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cacheCleared by remember { mutableStateOf(false) }

    val hasGeminiApiKey = !BuildConfig.GEMINI_API_KEY.isNullOrBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    Scaffold(
        containerColor = AnimeBackgroundDark,
        topBar = {
            AnimeHeader(
                title = "Creator Studio & Settings",
                subtitle = "Preferences, AI integrations & copyright guide"
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
            // Creator Profile Card
            AnimeCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonPurple
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26184C))
                            .border(1.5.dp, NeonPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Anime Creator",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            GlowBadge(text = "Guest Mode", color = NeonCyan)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Offline First • Zero sign-in required to edit & export",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Status & Provider Card
            Text(
                text = "AI ENGINE STATUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimeCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Gemini AI Scriptwriter", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (hasGeminiApiKey) "Connected via AI Studio Secrets" else "Built-in Smart Anime Engine Active",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                        GlowBadge(
                            text = if (hasGeminiApiKey) "Gemini Cloud" else "Smart Engine",
                            color = if (hasGeminiApiKey) NeonGreen else NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Voice Synthesizer (TTS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("High performance on-device neural voice engine", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        GlowBadge(text = "Active", color = NeonGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage & Cache Management
            Text(
                text = "STORAGE & CACHE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimeCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Temporary Export Cache", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Cached renders & temporary preview files", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val exports = File(context.cacheDir, "exports")
                                    val voices = File(context.cacheDir, "generated_voices")
                                    exports.deleteRecursively()
                                    voices.deleteRecursively()
                                    cacheCleared = true
                                    Toast.makeText(context, "Export cache cleared!", Toast.LENGTH_SHORT).show()
                                }
                                .border(1.dp, NeonPink, RoundedCornerShape(8.dp)),
                            color = Color(0xFF281335)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, tint = NeonPink, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", color = NeonPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Copyright & Fair Use Guide
            Text(
                text = "COPYRIGHT & LEGAL FAIR USE GUIDE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonYellow
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimeCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = Color(0xFF3B381C)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Copyright, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guidelines for Anime Content Creators",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Fair Use & Commentary: Using anime clips for analysis, review, critique, or transformative parody generally qualifies as Fair Use under copyright law.\n\n" +
                                "• Avoid Reuploading Raw Clips: Always add original value—such as your generated AI voiceover, commentary, custom anime color filters, speed ramps, and animated captions.\n\n" +
                                "• Background Music: Always use royalty-free or Creative Commons licensed music (such as YouTube Audio Library or CC-BY tracks) to avoid demonetization.\n\n" +
                                "• Notice: Using copyrighted anime clips without transformative editing or licenses may result in platform claims or restrictions on YouTube and TikTok.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Guarantee
            AnimeCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("100% On-Device Privacy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Your personal videos and project edits never leave your phone. All media decoding and rendering is performed locally.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
