package com.example.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.repository.AnimeProject
import com.example.data.repository.AnimeTemplate
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateRepository
import com.example.ui.components.AnimeButton
import com.example.ui.components.AnimeCard
import com.example.ui.components.GlowBadge
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onCreateProjectClick: () -> Unit,
    onOpenEditorForProject: (Long) -> Unit,
    onNavigateToAiVoice: () -> Unit,
    onNavigateToAiScript: () -> Unit,
    onNavigateToCaptions: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(context) }
    val recentProjects by repository.allProjects.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        containerColor = AnimeBackgroundDark
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // App Header & Branding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26194C))
                            .border(1.2.dp, NeonPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_fg),
                            contentDescription = "Logo",
                            modifier = Modifier.size(34.dp).clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AnimeForge",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }
                        Text(
                            text = "Next-Gen Anime Video Maker",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButtonWithBadge(
                    onClick = onNavigateToProfile,
                    tag = "home_profile_avatar"
                )
            }

            // Hero Anime Banner with Large "Create Anime Short" button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, NeonPurple.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner_anime_hero),
                    contentDescription = "Anime Hero Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for readable text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xCC090A1A),
                                    Color(0xF0090A1A)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    GlowBadge(text = "9:16 Shorts Ready", color = NeonCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Craft Viral Anime Shorts",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "AI Narration • Neon Captions • Anime Impact Cuts",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AnimeButton(
                        text = "Create Anime Short",
                        onClick = onCreateProjectClick,
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "home_create_anime_short_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "AI Script",
                    subtitle = "Story generator",
                    icon = Icons.Default.AutoAwesome,
                    color = NeonPurple,
                    onClick = onNavigateToAiScript,
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    title = "AI Voice",
                    subtitle = "Anime narrator",
                    icon = Icons.Default.Mic,
                    color = NeonPink,
                    onClick = onNavigateToAiVoice,
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    title = "Captions",
                    subtitle = "Neon subtitles",
                    icon = Icons.Default.Subtitles,
                    color = NeonCyan,
                    onClick = onNavigateToCaptions,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Projects
            SectionHeader(
                title = "Recent Projects",
                actionLabel = "View All",
                onActionClick = { onNavigateToTemplates() }
            )

            if (recentProjects.isEmpty()) {
                AnimeCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = onCreateProjectClick
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E2850)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("No projects yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap here to start editing your first anime short", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                ) {
                    items(recentProjects.take(6), key = { it.id }) { proj ->
                        RecentProjectCard(
                            project = proj,
                            onClick = { onOpenEditorForProject(proj.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Trending Templates Carousel
            SectionHeader(
                title = "Trending Editing Styles",
                actionLabel = "Explore",
                onActionClick = onNavigateToTemplates
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(TemplateRepository.templates.take(4), key = { it.id }) { template ->
                    TrendingTemplateCard(
                        template = template,
                        onUseTemplate = {
                            scope.launch {
                                val proj = TemplateRepository.createProjectFromTemplate(template)
                                val id = repository.saveProject(proj)
                                onOpenEditorForProject(id)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Beginner Quick Start Guide
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Beginner Quick Start Guide",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OnboardingStepCard(
                    stepNumber = "1",
                    title = "Generate AI Narration",
                    description = "Choose an anime topic, character, and tone (Epic, Hype, Emotional). AI writes a viral hook and synthesizes the voiceover.",
                    color = NeonPurple
                )
                Spacer(modifier = Modifier.height(8.dp))
                OnboardingStepCard(
                    stepNumber = "2",
                    title = "Import Anime Clips",
                    description = "Add video clips from your gallery or use bundled templates. Trim and split beats on the 9:16 vertical canvas.",
                    color = NeonPink
                )
                Spacer(modifier = Modifier.height(8.dp))
                OnboardingStepCard(
                    stepNumber = "3",
                    title = "Apply Neon Animated Subtitles",
                    description = "Choose Neon Cyber Glow or Bold Shonen captions. Subtitles automatically burn into the final MP4 video.",
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                OnboardingStepCard(
                    stepNumber = "4",
                    title = "Export & Share to Shorts",
                    description = "One-tap export in 1080p Full HD with native Android Share Sheet ready for YouTube Shorts, Reels, and TikTok.",
                    color = NeonGreen
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
        color = Color(0xFF141731)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            text = actionLabel,
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onActionClick)
        )
    }
}

@Composable
private fun RecentProjectCard(
    project: AnimeProject,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF2B325E), RoundedCornerShape(14.dp)),
        color = Color(0xFF13162F)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1D2248)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                GlowBadge(
                    text = "${project.durationMs / 1000}s",
                    color = NeonPink,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = project.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = "${project.clips.size} clips • ${project.captions.size} captions",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TrendingTemplateCard(
    template: AnimeTemplate,
    onUseTemplate: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onUseTemplate)
            .border(1.dp, NeonPurple.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        color = Color(0xFF151938)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowBadge(text = "${template.durationSeconds}s Shorts", color = NeonCyan)
                GlowBadge(text = template.tag, color = NeonYellow)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                maxLines = 2,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            AnimeButton(
                text = "Use Template",
                onClick = onUseTemplate,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OnboardingStepCard(
    stepNumber: String,
    title: String,
    description: String,
    color: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(0.8.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        color = Color(0xFF12152E)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(stepNumber, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun IconButtonWithBadge(
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E2448))
            .border(1.dp, NeonCyan.copy(alpha = 0.6f), CircleShape)
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = "Profile", tint = NeonCyan, modifier = Modifier.size(20.dp))
    }
}
