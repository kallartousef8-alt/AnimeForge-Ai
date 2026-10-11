package com.example.data.repository

import android.content.Context
import com.example.data.database.AnimeForgeDatabase
import com.example.data.database.ProjectEntity
import com.example.data.models.AnimeEffectType
import com.example.data.models.AnimeFilter
import com.example.data.models.AnimeTransition
import com.example.data.models.AspectRatio
import com.example.data.models.AudioTrackItem
import com.example.data.models.AudioType
import com.example.data.models.CaptionItem
import com.example.data.models.CaptionStylePreset
import com.example.data.models.ExportFps
import com.example.data.models.ExportResolution
import com.example.data.models.VideoClipItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

enum class ProjectSortOrder(val label: String) {
    RECENT("Recently Edited"),
    TITLE("Alphabetical (A-Z)"),
    DURATION("Duration (Shortest First)")
}

data class AnimeProject(
    val id: Long = 0,
    val title: String,
    val durationMs: Long = 15000L,
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT_9_16,
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val fps: ExportFps = ExportFps.FPS_30,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val thumbnailUri: String? = null,
    val clips: List<VideoClipItem> = emptyList(),
    val audioTracks: List<AudioTrackItem> = emptyList(),
    val captions: List<CaptionItem> = emptyList(),
    val activeFilter: AnimeFilter = AnimeFilter.NONE
) {
    val totalClipsDurationMs: Long
        get() = clips.sumOf { it.trimmedDurationMs }
}

class ProjectRepository(context: Context) {
    private val projectDao = AnimeForgeDatabase.getDatabase(context).projectDao()

    val allProjects: Flow<List<AnimeProject>> = projectDao.getAllProjects().map { list ->
        list.map { entityToProject(it) }
    }

    suspend fun getProject(id: Long): AnimeProject? {
        val entity = projectDao.getProjectById(id) ?: return null
        return entityToProject(entity)
    }

    suspend fun saveProject(project: AnimeProject): Long {
        val entity = projectToEntity(project)
        return if (project.id == 0L) {
            projectDao.insertProject(entity)
        } else {
            projectDao.updateProject(entity)
            project.id
        }
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteProjectById(id)
    }

    suspend fun duplicateProject(originalId: Long): Long? {
        val original = getProject(originalId) ?: return null
        val copy = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return saveProject(copy)
    }

    private fun entityToProject(entity: ProjectEntity): AnimeProject {
        val clips = parseClipsJson(entity.clipsJson)
        val audioTracks = parseAudioJson(entity.audioJson)
        val captions = parseCaptionsJson(entity.captionsJson)
        val aspect = try {
            AspectRatio.valueOf(entity.aspectRatio)
        } catch (_: Exception) {
            AspectRatio.PORTRAIT_9_16
        }
        val res = try {
            ExportResolution.valueOf(entity.resolution)
        } catch (_: Exception) {
            ExportResolution.RES_1080P
        }
        val fps = if (entity.fps >= 60) ExportFps.FPS_60 else ExportFps.FPS_30
        val filter = try {
            AnimeFilter.valueOf(entity.activeFilter)
        } catch (_: Exception) {
            AnimeFilter.NONE
        }

        return AnimeProject(
            id = entity.id,
            title = entity.title,
            durationMs = entity.durationMs,
            aspectRatio = aspect,
            resolution = res,
            fps = fps,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            thumbnailUri = entity.thumbnailUri,
            clips = clips,
            audioTracks = audioTracks,
            captions = captions,
            activeFilter = filter
        )
    }

    private fun projectToEntity(project: AnimeProject): ProjectEntity {
        return ProjectEntity(
            id = project.id,
            title = project.title,
            durationMs = project.durationMs,
            aspectRatio = project.aspectRatio.name,
            resolution = project.resolution.name,
            fps = project.fps.fps,
            createdAt = project.createdAt,
            updatedAt = System.currentTimeMillis(),
            thumbnailUri = project.thumbnailUri,
            clipsJson = clipsToJson(project.clips),
            audioJson = audioToJson(project.audioTracks),
            captionsJson = captionsToJson(project.captions),
            activeFilter = project.activeFilter.name
        )
    }

    private fun parseClipsJson(json: String): List<VideoClipItem> {
        val list = mutableListOf<VideoClipItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    VideoClipItem(
                        id = obj.optString("id"),
                        uriString = obj.optString("uriString"),
                        title = obj.optString("title"),
                        durationMs = obj.optLong("durationMs", 5000L),
                        startTrimMs = obj.optLong("startTrimMs", 0L),
                        endTrimMs = obj.optLong("endTrimMs", 5000L),
                        speed = obj.optDouble("speed", 1.0).toFloat(),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        filter = try {
                            AnimeFilter.valueOf(obj.optString("filter", "NONE"))
                        } catch (_: Exception) {
                            AnimeFilter.NONE
                        },
                        transition = try {
                            AnimeTransition.valueOf(obj.optString("transition", "CUT"))
                        } catch (_: Exception) {
                            AnimeTransition.CUT
                        },
                        effectType = try {
                            AnimeEffectType.valueOf(obj.optString("effectType", "NONE"))
                        } catch (_: Exception) {
                            AnimeEffectType.NONE
                        },
                        effectIntensity = obj.optDouble("effectIntensity", 0.6).toFloat(),
                        effectDurationMs = obj.optLong("effectDurationMs", 2000L),
                        orderIndex = obj.optInt("orderIndex", i)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun clipsToJson(clips: List<VideoClipItem>): String {
        val array = JSONArray()
        for (c in clips) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("uriString", c.uriString)
            obj.put("title", c.title)
            obj.put("durationMs", c.durationMs)
            obj.put("startTrimMs", c.startTrimMs)
            obj.put("endTrimMs", c.endTrimMs)
            obj.put("speed", c.speed.toDouble())
            obj.put("volume", c.volume.toDouble())
            obj.put("filter", c.filter.name)
            obj.put("transition", c.transition.name)
            obj.put("effectType", c.effectType.name)
            obj.put("effectIntensity", c.effectIntensity.toDouble())
            obj.put("effectDurationMs", c.effectDurationMs)
            obj.put("orderIndex", c.orderIndex)
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseAudioJson(json: String): List<AudioTrackItem> {
        val list = mutableListOf<AudioTrackItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val isVoice = obj.optBoolean("isVoiceover", false)
                list.add(
                    AudioTrackItem(
                        id = obj.optString("id"),
                        uriString = obj.optString("uriString"),
                        title = obj.optString("title"),
                        startTimelineMs = obj.optLong("startTimelineMs", 0L),
                        durationMs = obj.optLong("durationMs", 5000L),
                        volume = obj.optDouble("volume", 0.8).toFloat(),
                        isVoiceover = isVoice,
                        speechVoice = if (obj.has("speechVoice") && !obj.isNull("speechVoice")) obj.getString("speechVoice") else null,
                        audioType = try {
                            AudioType.valueOf(obj.optString("audioType", if (isVoice) "VOICEOVER" else "MUSIC"))
                        } catch (_: Exception) {
                            if (isVoice) AudioType.VOICEOVER else AudioType.MUSIC
                        },
                        fadeInMs = obj.optLong("fadeInMs", 0L),
                        fadeOutMs = obj.optLong("fadeOutMs", 0L)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun audioToJson(tracks: List<AudioTrackItem>): String {
        val array = JSONArray()
        for (a in tracks) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("uriString", a.uriString)
            obj.put("title", a.title)
            obj.put("startTimelineMs", a.startTimelineMs)
            obj.put("durationMs", a.durationMs)
            obj.put("volume", a.volume.toDouble())
            obj.put("isVoiceover", a.isVoiceover)
            if (a.speechVoice != null) obj.put("speechVoice", a.speechVoice)
            obj.put("audioType", a.audioType.name)
            obj.put("fadeInMs", a.fadeInMs)
            obj.put("fadeOutMs", a.fadeOutMs)
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseCaptionsJson(json: String): List<CaptionItem> {
        val list = mutableListOf<CaptionItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CaptionItem(
                        id = obj.optString("id"),
                        text = obj.optString("text"),
                        startMs = obj.optLong("startMs"),
                        endMs = obj.optLong("endMs"),
                        style = try {
                            CaptionStylePreset.valueOf(obj.optString("style", "NEON_GLOW"))
                        } catch (_: Exception) {
                            CaptionStylePreset.NEON_GLOW
                        },
                        fontSizeSp = obj.optDouble("fontSizeSp", 22.0).toFloat(),
                        yPositionRatio = obj.optDouble("yPositionRatio", 0.78).toFloat(),
                        highlightKeywords = obj.optBoolean("highlightKeywords", true),
                        highlightColorHex = obj.optString("highlightColorHex", "#FF2E93"),
                        outlineWidthSp = obj.optDouble("outlineWidthSp", 3.5).toFloat(),
                        shadowRadius = obj.optDouble("shadowRadius", 5.0).toFloat()
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun captionsToJson(captions: List<CaptionItem>): String {
        val array = JSONArray()
        for (c in captions) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("text", c.text)
            obj.put("startMs", c.startMs)
            obj.put("endMs", c.endMs)
            obj.put("style", c.style.name)
            obj.put("fontSizeSp", c.fontSizeSp.toDouble())
            obj.put("yPositionRatio", c.yPositionRatio.toDouble())
            obj.put("highlightKeywords", c.highlightKeywords)
            obj.put("highlightColorHex", c.highlightColorHex)
            obj.put("outlineWidthSp", c.outlineWidthSp.toDouble())
            obj.put("shadowRadius", c.shadowRadius.toDouble())
            array.put(obj)
        }
        return array.toString()
    }
}
