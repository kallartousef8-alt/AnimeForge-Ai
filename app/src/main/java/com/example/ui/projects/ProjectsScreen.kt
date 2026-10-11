package com.example.ui.projects

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.AnimeProject
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateRepository
import com.example.ui.components.AnimeButton
import com.example.ui.components.AnimeCard
import com.example.ui.components.AnimeHeader
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.GlowBadge
import com.example.ui.theme.AnimeBackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    onOpenEditorForProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(context) }
    val projects by repository.allProjects.collectAsStateWithLifecycle(initialValue = emptyList())

    var projectToDelete by remember { mutableStateOf<AnimeProject?>(null) }
    var projectToRename by remember { mutableStateOf<AnimeProject?>(null) }
    var renameInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = AnimeBackgroundDark,
        topBar = {
            AnimeHeader(
                title = "My Anime Projects",
                subtitle = "${projects.size} Saved Projects"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val newProj = TemplateRepository.createProjectFromTemplate(TemplateRepository.templates.first())
                        val id = repository.saveProject(newProj.copy(title = "New Anime Short #${projects.size + 1}"))
                        onOpenEditorForProject(id)
                    }
                },
                containerColor = NeonPink,
                contentColor = Color.White,
                modifier = Modifier.testTag("projects_new_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Project")
            }
        }
    ) { paddingValues ->
        if (projects.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Anime Projects Yet",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start by creating an anime video short or use a trending template.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimeButton(
                        text = "Create First Project",
                        onClick = {
                            scope.launch {
                                val template = TemplateRepository.templates.first()
                                val project = TemplateRepository.createProjectFromTemplate(template)
                                val id = repository.saveProject(project)
                                onOpenEditorForProject(id)
                            }
                        },
                        icon = Icons.Default.Add
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCardItem(
                        project = project,
                        onClick = { onOpenEditorForProject(project.id) },
                        onRenameClick = {
                            projectToRename = project
                            renameInput = project.title
                        },
                        onDuplicateClick = {
                            scope.launch {
                                val dupId = repository.duplicateProject(project.id)
                                Toast.makeText(context, "Project duplicated", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeleteClick = {
                            projectToDelete = project
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Rename Dialog
    val renameProj = projectToRename
    if (renameProj != null) {
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Project", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Project Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            },
            containerColor = Color(0xFF141733),
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.saveProject(renameProj.copy(title = renameInput.ifBlank { "Untitled Project" }))
                            projectToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    val delProj = projectToDelete
    if (delProj != null) {
        ConfirmationDialog(
            title = "Delete Project?",
            message = "Are you sure you want to delete '${delProj.title}'? This will permanently remove the timeline draft from local device storage.",
            confirmText = "Delete",
            onConfirm = {
                scope.launch {
                    repository.deleteProject(delProj.id)
                    projectToDelete = null
                    Toast.makeText(context, "Project deleted", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { projectToDelete = null }
        )
    }
}

@Composable
private fun ProjectCardItem(
    project: AnimeProject,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormatted = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(project.updatedAt))

    AnimeCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Last edited $dateFormatted • ${project.durationMs / 1000}s",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                GlowBadge(
                    text = project.aspectRatio.label.split(" ").first(),
                    color = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${project.clips.size} clips • ${project.captions.size} captions • ${project.resolution.label}",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRenameClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDuplicateClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonPink, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
