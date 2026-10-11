package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateRepository
import com.example.service.CaptionService
import com.example.ui.captions.CaptionsScreen
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.export.ExportScreen
import com.example.ui.home.HomeScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.projects.ProjectsScreen
import com.example.ui.script.AiScriptScreen
import com.example.ui.templates.TemplatesScreen
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.voice.AiVoiceScreen
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data object Projects : Screen()
    data object Templates : Screen()
    data object Profile : Screen()
    data class Editor(val projectId: Long) : Screen()
    data class Export(val projectId: Long) : Screen()
    data class AiScript(val projectId: Long? = null) : Screen()
    data class AiVoice(val text: String = "", val lang: String = "en", val projectId: Long? = null) : Screen()
    data class Captions(val projectId: Long) : Screen()
}

enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    PROJECTS("Projects", Icons.Default.VideoLibrary),
    TEMPLATES("Templates", Icons.Default.AutoAwesome),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(context) }
    val captionService = remember { CaptionService() }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    val editorViewModel: EditorViewModel = viewModel()

    val isSubScreen = currentScreen !is Screen.Home &&
            currentScreen !is Screen.Projects &&
            currentScreen !is Screen.Templates &&
            currentScreen !is Screen.Profile

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AnimeBackgroundDark,
        bottomBar = {
            if (!isSubScreen) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = Color(0xFF0F122B),
                    tonalElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E234B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val activeColor = if (tab == BottomTab.HOME) NeonCyan
                            else if (tab == BottomTab.TEMPLATES) NeonPink
                            else NeonPurple

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedTab = tab
                                        currentScreen = when (tab) {
                                            BottomTab.HOME -> Screen.Home
                                            BottomTab.PROJECTS -> Screen.Projects
                                            BottomTab.TEMPLATES -> Screen.Templates
                                            BottomTab.PROFILE -> Screen.Profile
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                                    .testTag("nav_${tab.name.lowercase()}")
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) activeColor else Color(0xFF64748B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        onCreateProjectClick = {
                            scope.launch {
                                val template = TemplateRepository.templates.first()
                                val project = TemplateRepository.createProjectFromTemplate(template)
                                val id = repository.saveProject(project)
                                currentScreen = Screen.Editor(id)
                            }
                        },
                        onOpenEditorForProject = { currentScreen = Screen.Editor(it) },
                        onNavigateToAiVoice = { currentScreen = Screen.AiVoice() },
                        onNavigateToAiScript = { currentScreen = Screen.AiScript() },
                        onNavigateToCaptions = {
                            scope.launch {
                                val template = TemplateRepository.templates.first()
                                val project = TemplateRepository.createProjectFromTemplate(template)
                                val id = repository.saveProject(project)
                                currentScreen = Screen.Captions(id)
                            }
                        },
                        onNavigateToTemplates = {
                            selectedTab = BottomTab.TEMPLATES
                            currentScreen = Screen.Templates
                        },
                        onNavigateToProfile = {
                            selectedTab = BottomTab.PROFILE
                            currentScreen = Screen.Profile
                        }
                    )
                }

                is Screen.Projects -> {
                    ProjectsScreen(
                        onOpenEditorForProject = { currentScreen = Screen.Editor(it) }
                    )
                }

                is Screen.Templates -> {
                    TemplatesScreen(
                        onOpenEditorForProject = { currentScreen = Screen.Editor(it) }
                    )
                }

                is Screen.Profile -> {
                    ProfileScreen()
                }

                is Screen.Editor -> {
                    EditorScreen(
                        projectId = screen.projectId,
                        viewModel = editorViewModel,
                        onNavigateBack = {
                            currentScreen = when (selectedTab) {
                                BottomTab.HOME -> Screen.Home
                                BottomTab.PROJECTS -> Screen.Projects
                                BottomTab.TEMPLATES -> Screen.Templates
                                BottomTab.PROFILE -> Screen.Profile
                            }
                        },
                        onNavigateToExport = { currentScreen = Screen.Export(it) },
                        onNavigateToAiVoice = { currentScreen = Screen.AiVoice(projectId = it) },
                        onNavigateToAiScript = { currentScreen = Screen.AiScript(projectId = it) },
                        onNavigateToCaptions = { currentScreen = Screen.Captions(it) }
                    )
                }

                is Screen.Export -> {
                    ExportScreen(
                        projectId = screen.projectId,
                        onNavigateBack = { currentScreen = Screen.Editor(screen.projectId) }
                    )
                }

                is Screen.AiScript -> {
                    AiScriptScreen(
                        projectId = screen.projectId,
                        onNavigateBack = {
                            if (screen.projectId != null) currentScreen = Screen.Editor(screen.projectId)
                            else currentScreen = Screen.Home
                        },
                        onSendToVoice = { scriptText, lang ->
                            currentScreen = Screen.AiVoice(text = scriptText, lang = lang, projectId = screen.projectId)
                        },
                        onApplyCaptions = if (screen.projectId != null) {
                            { scriptText ->
                                scope.launch {
                                    val project = repository.getProject(screen.projectId)
                                    if (project != null) {
                                        val newCaptions = captionService.generateCaptionsFromText(
                                            text = scriptText,
                                            totalDurationMs = project.durationMs
                                        )
                                        editorViewModel.setCaptions(newCaptions)
                                        currentScreen = Screen.Editor(screen.projectId)
                                    }
                                }
                            }
                        } else null
                    )
                }

                is Screen.AiVoice -> {
                    AiVoiceScreen(
                        initialText = screen.text,
                        initialLang = screen.lang,
                        projectId = screen.projectId,
                        onNavigateBack = {
                            if (screen.projectId != null) currentScreen = Screen.Editor(screen.projectId)
                            else currentScreen = Screen.Home
                        },
                        onAudioTrackCreated = if (screen.projectId != null) {
                            { track ->
                                editorViewModel.addAudioTrack(track)
                            }
                        } else null
                    )
                }

                is Screen.Captions -> {
                    val activeProject = editorViewModel.uiState.value.project
                    val initialCaps = activeProject?.captions ?: emptyList()
                    val totalDuration = activeProject?.durationMs ?: 15000L
                    CaptionsScreen(
                        initialCaptions = initialCaps,
                        projectDurationMs = totalDuration,
                        onNavigateBack = { currentScreen = Screen.Editor(screen.projectId) },
                        onApplyCaptions = { caps ->
                            editorViewModel.setCaptions(caps)
                        }
                    )
                }
            }
        }
    }
}
