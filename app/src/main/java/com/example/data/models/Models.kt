package com.example.data.models

import java.util.UUID

enum class AspectRatio(val label: String, val ratio: Float, val width: Int, val height: Int) {
    PORTRAIT_9_16("9:16 (Shorts/Reels)", 9f / 16f, 1080, 1920),
    LANDSCAPE_16_9("16:9 (YouTube)", 16f / 9f, 1920, 1080),
    SQUARE_1_1("1:1 (Square)", 1f, 1080, 1080)
}

enum class ExportResolution(val label: String, val height: Int) {
    RES_720P("720p HD", 720),
    RES_1080P("1080p Full HD", 1080)
}

enum class ExportFps(val fps: Int, val label: String) {
    FPS_30(30, "30 FPS (Standard)"),
    FPS_60(60, "60 FPS (Ultra Smooth)")
}

enum class AnimeFilter(val label: String, val description: String) {
    NONE("Original", "No color modification"),
    VIBRANT_SHONEN("Vibrant Shonen", "High saturation, punchy contrasts"),
    CYBERPUNK_NEON("Cyberpunk Neon", "Electric blue and magenta glow"),
    SAKURA_DUSK("Sakura Dusk", "Warm dreamy pastel pink highlights"),
    MOONLIGHT_SHADOW("Moonlight Shadow", "Deep cool midnight blue tones"),
    MANGA_NOIR("Manga Noir", "High contrast black & white manga style"),
    GOLDEN_AURA("Golden Aura", "Warm amber anime power glow")
}

enum class AnimeTransition(val label: String) {
    CUT("Cut"),
    FLASH_WHITE("Flash White"),
    ZOOM_IN("Zoom In"),
    GLITCH("Glitch Impact"),
    FADE_BLACK("Fade Black")
}

enum class AnimeEffectType(val label: String, val description: String) {
    NONE("No Effect", "Standard playback"),
    SHAKE("Impact Shake", "Intense camera shake on action impact"),
    ZOOM_IN("Dynamic Zoom In", "Dramatic camera push toward subject"),
    ZOOM_OUT("Dramatic Zoom Out", "Expansive reveal zoom"),
    FLASH("White Flash", "Bright impact transition flash"),
    MOTION_BLUR("Motion Blur", "High-velocity anime speed lines"),
    GLOW("Neon Anime Glow", "Pulsing cybernetic aura bloom"),
    SPEED_RAMP("Speed Ramp", "Fast-to-slow motion tempo shift"),
    CINEMATIC_BARS("Cinematic Bars", "Letterbox film aspect ratio bars"),
    BEAT_SYNC("Beat-Sync Pulse", "Audio-reactive rhythmic scale bounce")
}

enum class AudioType(val label: String) {
    VOICEOVER("AI Narration"),
    MUSIC("Background Music"),
    SFX("Sound Effect")
}

enum class CaptionStylePreset(
    val label: String,
    val primaryColorHex: String,
    val outlineColorHex: String,
    val shadowColorHex: String,
    val isUppercase: Boolean
) {
    NEON_GLOW("Neon Cyber", "#00F0FF", "#7E22CE", "#00F0FF", true),
    BOLD_IMPACT("Bold Shonen", "#FFE600", "#000000", "#FF0055", true),
    MINIMAL_WHITE("Minimal Clean", "#FFFFFF", "#1E293B", "#0F172A", false),
    CINEMATIC_SUB("Cinematic", "#F8FAFC", "#000000", "#38BDF8", false),
    FIRE_FLAME("Flame Burst", "#FF3D00", "#FFD600", "#B71C1C", true)
}

enum class ScriptCategory(val label: String, val badge: String) {
    BATTLES("Anime Battles & Clashes", "Adrenaline"),
    CHARACTER_FACTS("Character Facts & Lore", "Factual"),
    EMOTIONAL_STORIES("Emotional Stories & Sacrifices", "Heartbreak"),
    POWER_RANKINGS("Power Rankings & Tier Lists", "Debate"),
    THEORIES("Mind-Bending Theories", "Mystery"),
    EPISODE_EXPLANATIONS("Episode Breakdown & Climax", "Breakdown")
}

enum class ScriptStyle(val label: String, val tone: String) {
    EPIC("Epic", "High stakes, legendary battles, heart-pumping adrenaline"),
    EMOTIONAL("Emotional", "Deep melancholy, sacrifice, tear-jerking anime lore"),
    MYSTERIOUS("Mysterious", "Unsolved anime secrets, dark theories, chilling twists"),
    INFORMATIVE("Informative", "Factual breakdowns, character power analysis, hidden details"),
    HYPE("Hype", "Fast-paced viral energy, power rankings, mind-blowing feats")
}

enum class ScriptLanguage(val label: String, val code: String) {
    ENGLISH("English", "en"),
    HINDI("Hindi (हिन्दी)", "hi"),
    HINGLISH("Hinglish", "hi-en"),
    HINGLISH_TO_ENGLISH("Hinglish Idea -> English Narration", "hinglish_en")
}

enum class AnimeVoiceStyle(
    val title: String,
    val subtitle: String,
    val pitch: Float,
    val rate: Float
) {
    EPIC_NARRATOR("Epic Anime Narrator", "Deep, legendary battlefield storytelling", 0.75f, 0.95f),
    DEEP_CINEMATIC("Deep Cinematic", "Rich, resonant cinematic trailer narration", 0.68f, 0.90f),
    ENERGETIC_HYPE("Energetic Hype", "High octane shonen viral short-form delivery", 1.25f, 1.22f),
    EMOTIONAL_STORYTELLER("Emotional Storyteller", "Melancholic, poignant tribute pacing", 0.92f, 0.88f),
    CALM_EXPLAINER("Calm Explainer", "Clear, engaging anime lore breakdown", 1.00f, 1.05f)
}

data class VideoClipItem(
    val id: String = UUID.randomUUID().toString(),
    val uriString: String,
    val title: String,
    val durationMs: Long = 5000L,
    val startTrimMs: Long = 0L,
    val endTrimMs: Long = 5000L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val filter: AnimeFilter = AnimeFilter.NONE,
    val transition: AnimeTransition = AnimeTransition.CUT,
    val effectType: AnimeEffectType = AnimeEffectType.NONE,
    val effectIntensity: Float = 0.6f,
    val effectDurationMs: Long = 2000L,
    val orderIndex: Int = 0
) {
    val trimmedDurationMs: Long
        get() = ((endTrimMs - startTrimMs) / speed).toLong().coerceAtLeast(100L)
}

data class AudioTrackItem(
    val id: String = UUID.randomUUID().toString(),
    val uriString: String,
    val title: String,
    val startTimelineMs: Long = 0L,
    val durationMs: Long = 10000L,
    val volume: Float = 0.8f,
    val isVoiceover: Boolean = false,
    val speechVoice: String? = null,
    val audioType: AudioType = if (isVoiceover) AudioType.VOICEOVER else AudioType.MUSIC,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L
)

data class CaptionItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val style: CaptionStylePreset = CaptionStylePreset.NEON_GLOW,
    val fontSizeSp: Float = 22f,
    val yPositionRatio: Float = 0.78f, // Safe zone for Shorts (near bottom center)
    val highlightKeywords: Boolean = true,
    val highlightColorHex: String = "#FF2E93",
    val outlineWidthSp: Float = 3.5f,
    val shadowRadius: Float = 5.0f
)

data class GeneratedScript(
    val topic: String,
    val character: String,
    val durationSeconds: Int,
    val category: ScriptCategory = ScriptCategory.BATTLES,
    val style: ScriptStyle,
    val language: ScriptLanguage,
    val hook: String,
    val body: String,
    val conclusion: String,
    val suggestedTitle: String = "",
    val suggestedDescription: String = "",
    val hashtags: List<String> = emptyList()
) {
    val fullNarration: String
        get() = "$hook\n\n$body\n\n$conclusion"
}

data class ShortsReadiness(
    val isDurationShortsReady: Boolean,
    val hasCaptions: Boolean,
    val hasAudio: Boolean,
    val isResolutionHd: Boolean,
    val isPortraitRatio: Boolean
) {
    val isFullyReady: Boolean
        get() = isDurationShortsReady && hasCaptions && hasAudio && isResolutionHd && isPortraitRatio

    val scorePercent: Int
        get() {
            var count = 0
            if (isDurationShortsReady) count++
            if (hasCaptions) count++
            if (hasAudio) count++
            if (isResolutionHd) count++
            if (isPortraitRatio) count++
            return (count * 100) / 5
        }
}
