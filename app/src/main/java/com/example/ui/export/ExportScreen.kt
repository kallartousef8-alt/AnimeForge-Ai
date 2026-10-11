package com.example.ui.export

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.models.AspectRatio
import com.example.data.models.ExportFps
import com.example.data.models.ExportResolution
import com.example.data.repository.AnimeProject
import com.example.data.repository.ProjectRepository
import com.example.engine.ExportResult
import com.example.engine.VideoExporter
import com.example.ui.components.AnimeButton
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

@Composable
fun ExportScreen(
    projectId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(context) }
    val exporter = remember { VideoExporter(context) }

    var project by remember { mutableStateOf<AnimeProject?>(null) }
    var selectedRes by remember { mutableStateOf(ExportResolution.RES_1080P) }
    var selectedAspect by remember { mutableStateOf(AspectRatio.PORTRAIT_9_16) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_30) }

    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var exportResult by remember { mutableStateOf<ExportResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(projectId) {
        val p = repository.getProject(projectId)
        project = p
        if (p != null) {
            selectedRes = p.resolution
            selectedAspect = p.aspectRatio
            selectedFps = p.fps
        }
    }

    Scaffold(
        containerColor = AnimeBackgroundDark,
        topBar = {
            AnimeHeader(
                title = "Export Video",
                subtitle = "Render MP4 for YouTube Shorts & Reels",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        val currentProject = project
        if (currentProject == null) {
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
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Project Summary Card
                AnimeCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentProject.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentProject.clips.size} clips • ${currentProject.captions.size} captions • ${currentProject.durationMs / 1000}s duration",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }

                            if (currentProject.durationMs <= 60000L) {
                                GlowBadge(text = "Shorts Ready (≤60s)", color = NeonGreen)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // If export is in progress
                if (isExporting) {
                    AnimeCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonCyan
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "RENDERING ANIME VIDEO",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { exportProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = NeonPink,
                                trackColor = Color(0xFF232854)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(exportProgress * 100).toInt()}% • Encoding H.264 & burning neon subtitles...",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // If export succeeded
                val res = exportResult
                if (res != null) {
                    AnimeCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonGreen
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Export Completed Successfully!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${res.width}x${res.height} • ${res.durationSeconds}s • ${res.fileSizeFormatted}",
                                color = NeonCyan,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Saved to Movies/AnimeForge & App Storage",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Share Action Button
                            AnimeButton(
                                text = "Share to YouTube Shorts / Apps",
                                onClick = {
                                    val shareUri = exporter.getShareUri(res.file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Anime Short"))
                                },
                                icon = Icons.Default.Share,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "export_share_button"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (errorMessage != null) {
                    Text(
                        text = "Export Error: $errorMessage",
                        color = NeonPink,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Export Settings (Resolution, Aspect Ratio, FPS)
                if (!isExporting) {
                    Text(
                        text = "EXPORT CONFIGURATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Resolution
                    Text("Resolution", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExportResolution.entries.forEach { r ->
                            val isSelected = r == selectedRes
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedRes = r }
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonCyan else Color(0xFF2C325A),
                                        RoundedCornerShape(10.dp)
                                    ),
                                color = if (isSelected) Color(0xFF1B264B) else Color(0xFF141733)
                            ) {
                                Text(
                                    text = r.label,
                                    color = if (isSelected) NeonCyan else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aspect Ratio
                    Text("Aspect Ratio", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AspectRatio.entries.forEach { a ->
                            val isSelected = a == selectedAspect
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedAspect = a }
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonPink else Color(0xFF2C325A),
                                        RoundedCornerShape(10.dp)
                                    ),
                                color = if (isSelected) Color(0xFF271536) else Color(0xFF141733)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = a.label.split(" ").first(),
                                        color = if (isSelected) NeonPink else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Framerate
                    Text("Framerate", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExportFps.entries.forEach { f ->
                            val isSelected = f == selectedFps
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedFps = f }
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonPurple else Color(0xFF2C325A),
                                        RoundedCornerShape(10.dp)
                                    ),
                                color = if (isSelected) Color(0xFF241A45) else Color(0xFF141733)
                            ) {
                                Text(
                                    text = f.label,
                                    color = if (isSelected) NeonPurple else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Start Export Button
                    AnimeButton(
                        text = "Export High-Quality MP4 Video",
                        onClick = {
                            scope.launch {
                                isExporting = true
                                exportProgress = 0f
                                errorMessage = null
                                exportResult = null
                                val result = exporter.exportProjectToMp4(
                                    project = currentProject,
                                    resolution = selectedRes,
                                    aspectRatio = selectedAspect,
                                    fps = selectedFps,
                                    onProgress = { p -> exportProgress = p }
                                )
                                isExporting = false
                                result.onSuccess { r ->
                                    exportResult = r
                                    Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "Export failed"
                                    Toast.makeText(context, "Export failed: ${err.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        icon = Icons.Default.Movie,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "start_export_mp4_button"
                    )
                }
            }
        }
    }
}
