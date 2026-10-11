package com.example.ui.templates

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AnimeTemplate
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateRepository
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
fun TemplatesScreen(
    onNavigateBack: (() -> Unit)? = null,
    onOpenEditorForProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(context) }

    var searchQuery by remember { mutableStateOf("") }

    val filteredTemplates = remember(searchQuery) {
        if (searchQuery.isBlank()) TemplateRepository.templates
        else TemplateRepository.templates.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = AnimeBackgroundDark,
        topBar = {
            AnimeHeader(
                title = "Anime Shorts Templates",
                subtitle = "Viral pacing, audio & caption presets",
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("templates_search_input"),
                placeholder = { Text("Search templates (Fight, Power-Up, Lore...)", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = Color(0xFF2C325A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Templates list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTemplates, key = { it.id }) { template ->
                    TemplateItemCard(
                        template = template,
                        onUseTemplate = {
                            scope.launch {
                                val project = TemplateRepository.createProjectFromTemplate(template)
                                val projectId = repository.saveProject(project)
                                onOpenEditorForProject(projectId)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TemplateItemCard(
    template: AnimeTemplate,
    onUseTemplate: () -> Unit
) {
    AnimeCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = Color(0xFF2E3462)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.category.uppercase(),
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GlowBadge(text = "${template.durationSeconds}s", color = NeonCyan)
                    GlowBadge(text = template.tag, color = NeonYellow)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = template.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = template.description,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Music & Filter tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = template.musicName,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Use Template CTA
            AnimeButton(
                text = "Use Template (${template.durationSeconds}s)",
                onClick = onUseTemplate,
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier.fillMaxWidth(),
                testTag = "use_template_${template.id}_button"
            )
        }
    }
}
