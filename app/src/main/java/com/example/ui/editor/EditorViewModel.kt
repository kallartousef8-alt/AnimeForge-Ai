package com.example.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.AnimeFilter
import com.example.data.models.AnimeTransition
import com.example.data.models.AspectRatio
import com.example.data.models.AudioTrackItem
import com.example.data.models.CaptionItem
import com.example.data.models.VideoClipItem
import com.example.data.repository.AnimeProject
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorTab(val label: String) {
    CLIPS("Clips"),
    AUDIO("Audio & Voice"),
    CAPTIONS("Captions"),
    EFFECTS("Effects"),
    FILTERS("Filters"),
    CANVAS("Canvas")
}

data class EditorUiState(
    val project: AnimeProject? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val selectedClipIndex: Int = 0,
    val selectedCaptionId: String? = null,
    val activeTab: EditorTab = EditorTab.CLIPS,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val statusMessage: String? = null
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProjectRepository(application)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<AnimeProject>()
    private val redoStack = mutableListOf<AnimeProject>()

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val existing = if (projectId > 0) repository.getProject(projectId) else null
            val project = existing ?: createDefaultProject()
            _uiState.update {
                it.copy(
                    project = project,
                    isLoading = false,
                    currentPositionMs = 0L,
                    selectedClipIndex = 0,
                    canUndo = false,
                    canRedo = false
                )
            }
        }
    }

    private fun createDefaultProject(): AnimeProject {
        // Return pre-configured project from the Epic Character Awakening template
        return TemplateRepository.createProjectFromTemplate(TemplateRepository.templates.first())
    }

    private fun pushUndoState(current: AnimeProject) {
        undoStack.add(current)
        if (undoStack.size > 20) undoStack.removeAt(0)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun undo() {
        val current = _uiState.value.project ?: return
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(current)
            _uiState.update {
                it.copy(
                    project = previous,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true,
                    statusMessage = "Action undone"
                )
            }
            saveProjectInternal(previous)
        }
    }

    fun redo() {
        val current = _uiState.value.project ?: return
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(current)
            _uiState.update {
                it.copy(
                    project = next,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty(),
                    statusMessage = "Action redone"
                )
            }
            saveProjectInternal(next)
        }
    }

    fun setPlaying(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }

    fun seekTo(positionMs: Long) {
        val total = _uiState.value.project?.durationMs ?: 15000L
        _uiState.update { it.copy(currentPositionMs = positionMs.coerceIn(0L, total)) }
    }

    fun selectTab(tab: EditorTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectClip(index: Int) {
        _uiState.update { it.copy(selectedClipIndex = index) }
    }

    fun addClips(uris: List<String>) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)

        val newClips = uris.mapIndexed { idx, uri ->
            VideoClipItem(
                id = UUID.randomUUID().toString(),
                uriString = uri,
                title = "Clip ${current.clips.size + idx + 1}",
                durationMs = 5000L,
                startTrimMs = 0L,
                endTrimMs = 5000L,
                orderIndex = current.clips.size + idx
            )
        }

        val updatedClips = current.clips + newClips
        val newDuration = updatedClips.sumOf { it.trimmedDurationMs }
        val updated = current.copy(clips = updatedClips, durationMs = newDuration)
        updateProject(updated, "Added ${uris.size} clips")
    }

    fun trimClip(clipIndex: Int, startMs: Long, endMs: Long) {
        val current = _uiState.value.project ?: return
        if (clipIndex !in current.clips.indices) return
        pushUndoState(current)

        val updatedClips = current.clips.toMutableList()
        val clip = updatedClips[clipIndex]
        updatedClips[clipIndex] = clip.copy(
            startTrimMs = startMs.coerceAtLeast(0L),
            endTrimMs = endMs.coerceAtMost(clip.durationMs)
        )
        val newDuration = updatedClips.sumOf { it.trimmedDurationMs }
        val updated = current.copy(clips = updatedClips, durationMs = newDuration)
        updateProject(updated, "Trimmed clip")
    }

    fun splitClip(clipIndex: Int) {
        val current = _uiState.value.project ?: return
        if (clipIndex !in current.clips.indices) return
        pushUndoState(current)

        val clip = current.clips[clipIndex]
        val mid = (clip.startTrimMs + clip.endTrimMs) / 2
        val firstHalf = clip.copy(id = UUID.randomUUID().toString(), endTrimMs = mid)
        val secondHalf = clip.copy(id = UUID.randomUUID().toString(), startTrimMs = mid)

        val updatedClips = current.clips.toMutableList()
        updatedClips.removeAt(clipIndex)
        updatedClips.add(clipIndex, firstHalf)
        updatedClips.add(clipIndex + 1, secondHalf)

        val updated = current.copy(clips = updatedClips)
        updateProject(updated, "Split clip into two")
    }

    fun duplicateClip(clipIndex: Int) {
        val current = _uiState.value.project ?: return
        if (clipIndex !in current.clips.indices) return
        pushUndoState(current)

        val clip = current.clips[clipIndex]
        val copy = clip.copy(id = UUID.randomUUID().toString(), title = "${clip.title} (Copy)")
        val updatedClips = current.clips.toMutableList()
        updatedClips.add(clipIndex + 1, copy)

        val newDuration = updatedClips.sumOf { it.trimmedDurationMs }
        val updated = current.copy(clips = updatedClips, durationMs = newDuration)
        updateProject(updated, "Duplicated clip")
    }

    fun deleteClip(clipIndex: Int) {
        val current = _uiState.value.project ?: return
        if (current.clips.size <= 1) {
            _uiState.update { it.copy(statusMessage = "Cannot delete the last remaining clip") }
            return
        }
        if (clipIndex !in current.clips.indices) return
        pushUndoState(current)

        val updatedClips = current.clips.toMutableList()
        updatedClips.removeAt(clipIndex)
        val newDuration = updatedClips.sumOf { it.trimmedDurationMs }
        val newIndex = (clipIndex - 1).coerceAtLeast(0)
        val updated = current.copy(clips = updatedClips, durationMs = newDuration)
        _uiState.update { it.copy(selectedClipIndex = newIndex) }
        updateProject(updated, "Deleted clip")
    }

    fun setClipSpeed(clipIndex: Int, speed: Float) {
        val current = _uiState.value.project ?: return
        if (clipIndex !in current.clips.indices) return
        pushUndoState(current)

        val updatedClips = current.clips.toMutableList()
        val clip = updatedClips[clipIndex]
        updatedClips[clipIndex] = clip.copy(speed = speed)
        val newDuration = updatedClips.sumOf { it.trimmedDurationMs }
        val updated = current.copy(clips = updatedClips, durationMs = newDuration)
        updateProject(updated, "Speed changed to ${speed}x")
    }

    fun setClipTransition(clipIndex: Int, transition: AnimeTransition) {
        val current = _uiState.value.project ?: return
        if (clipIndex !in current.clips.indices) return
        pushUndoState(current)

        val updatedClips = current.clips.toMutableList()
        updatedClips[clipIndex] = updatedClips[clipIndex].copy(transition = transition)
        val updated = current.copy(clips = updatedClips)
        updateProject(updated, "Transition set to ${transition.label}")
    }

    fun setClipEffect(
        clipIndex: Int,
        effectType: com.example.data.models.AnimeEffectType,
        intensity: Float = 0.6f,
        durationMs: Long = 2000L,
        applyToAll: Boolean = false
    ) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)

        val updatedClips = current.clips.mapIndexed { idx, clip ->
            if (applyToAll || idx == clipIndex) {
                clip.copy(
                    effectType = effectType,
                    effectIntensity = intensity,
                    effectDurationMs = durationMs
                )
            } else clip
        }
        val updated = current.copy(clips = updatedClips)
        updateProject(updated, "Effect: ${effectType.label}")
    }

    fun moveClip(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.project ?: return
        if (fromIndex !in current.clips.indices || toIndex !in current.clips.indices) return
        pushUndoState(current)

        val list = current.clips.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        val updated = current.copy(clips = list)
        _uiState.update { it.copy(selectedClipIndex = toIndex) }
        updateProject(updated, "Reordered clips")
    }

    fun setProjectFilter(filter: AnimeFilter) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)
        val updated = current.copy(activeFilter = filter)
        updateProject(updated, "Filter: ${filter.label}")
    }

    fun setAspectRatio(aspect: AspectRatio) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)
        val updated = current.copy(aspectRatio = aspect)
        updateProject(updated, "Canvas: ${aspect.label}")
    }

    fun addAudioTrack(track: AudioTrackItem) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)
        val updated = current.copy(audioTracks = current.audioTracks + track)
        updateProject(updated, "Audio track added")
    }

    fun deleteAudioTrack(trackId: String) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)
        val updated = current.copy(audioTracks = current.audioTracks.filterNot { it.id == trackId })
        updateProject(updated, "Audio track removed")
    }

    fun setAudioVolume(trackId: String, volume: Float) {
        val current = _uiState.value.project ?: return
        val updatedTracks = current.audioTracks.map {
            if (it.id == trackId) it.copy(volume = volume) else it
        }
        val updated = current.copy(audioTracks = updatedTracks)
        updateProject(updated, null)
    }

    fun setAudioFades(trackId: String, fadeInMs: Long, fadeOutMs: Long) {
        val current = _uiState.value.project ?: return
        val updatedTracks = current.audioTracks.map {
            if (it.id == trackId) it.copy(fadeInMs = fadeInMs, fadeOutMs = fadeOutMs) else it
        }
        val updated = current.copy(audioTracks = updatedTracks)
        updateProject(updated, null)
    }

    fun setCaptions(captions: List<CaptionItem>) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)
        val updated = current.copy(captions = captions)
        updateProject(updated, "Applied ${captions.size} captions")
    }

    fun addCaption(caption: CaptionItem) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)
        val updated = current.copy(captions = current.captions + caption)
        updateProject(updated, "Caption added")
    }

    fun deleteCaption(captionId: String) {
        val current = _uiState.value.project ?: return
        pushUndoState(current)
        val updated = current.copy(captions = current.captions.filterNot { it.id == captionId })
        updateProject(updated, "Caption deleted")
    }

    fun updateProjectTitle(title: String) {
        val current = _uiState.value.project ?: return
        val updated = current.copy(title = title)
        updateProject(updated, null)
    }

    private fun updateProject(project: AnimeProject, message: String?) {
        _uiState.update { it.copy(project = project, statusMessage = message) }
        saveProjectInternal(project)
    }

    private fun saveProjectInternal(project: AnimeProject) {
        viewModelScope.launch {
            val id = repository.saveProject(project)
            if (project.id == 0L) {
                _uiState.update { it.copy(project = project.copy(id = id)) }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
