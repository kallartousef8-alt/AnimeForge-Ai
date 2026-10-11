package com.example.data.repository

import com.example.data.models.AnimeFilter
import com.example.data.models.AnimeTransition
import com.example.data.models.AspectRatio
import com.example.data.models.CaptionItem
import com.example.data.models.CaptionStylePreset
import com.example.data.models.ExportFps
import com.example.data.models.ExportResolution
import com.example.data.models.VideoClipItem

data class AnimeTemplate(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val durationSeconds: Int,
    val tag: String,
    val filter: AnimeFilter,
    val sampleCaptions: List<Pair<String, Pair<Long, Long>>>,
    val musicName: String
)

object TemplateRepository {
    val templates = listOf(
        AnimeTemplate(
            id = "template_epic_intro",
            title = "Epic Character Awakening",
            category = "Power & Intro",
            description = "High-energy character introduction with electric impact cuts, neon captions, and dramatic crescendo.",
            durationSeconds = 15,
            tag = "Viral Shorts",
            filter = AnimeFilter.VIBRANT_SHONEN,
            sampleCaptions = listOf(
                "THEY THOUGHT HE WAS WEAK..." to (0L to 3000L),
                "UNTIL THE SEAL BROKE!" to (3200L to 6500L),
                "WITNESS TRUE GOD TIER POWER!" to (6800L to 11000L),
                "SUBSCRIBE FOR PART 2!" to (11500L to 14800L)
            ),
            musicName = "Cyber Shonen Phonk (Royalty-Free)"
        ),
        AnimeTemplate(
            id = "template_power_up",
            title = "Unstoppable Power Surge",
            category = "Action & Hype",
            description = "Fast-cut transformation edit featuring shake effects, glowing power filters, and bass drop sync.",
            durationSeconds = 30,
            tag = "Trending Audio",
            filter = AnimeFilter.CYBERPUNK_NEON,
            sampleCaptions = listOf(
                "WHEN THE PROTAGONIST SMILES..." to (0L to 4000L),
                "EVERY VILLAIN FREEZES IN FEAR." to (4500L to 9000L),
                "LEVEL 999 MANIFESTED!" to (9500L to 16000L),
                "WHO HAS THE BEST TRANSFORMATION?" to (16500L to 24000L),
                "DROP YOUR FAVORITE BELOW!" to (24500L to 29500L)
            ),
            musicName = "Hyper Synth Surge (CC-BY)"
        ),
        AnimeTemplate(
            id = "template_fight_recap",
            title = "Legendary Battle Breakdown",
            category = "Fight Recap",
            description = "Paced recap of the most intense clash with slow-mo impact beats and bold anime captions.",
            durationSeconds = 45,
            tag = "Battle Arc",
            filter = AnimeFilter.GOLDEN_AURA,
            sampleCaptions = listOf(
                "THE DEADLIEST CLASH IN ANIME HISTORY!" to (0L to 5000L),
                "ROUND 1: PURE MARTIAL WILL." to (5500L to 14000L),
                "THEN CAME THE DOMAIN EXPANSION!" to (14500L to 26000L),
                "DID THE RIGHT WARRIOR WIN?" to (26500L to 38000L),
                "LEAVE A LIKE IF YOU WERE HYPED!" to (38500L to 44500L)
            ),
            musicName = "Orchestral Battle Anthem"
        ),
        AnimeTemplate(
            id = "template_emotional_story",
            title = "Heartbreaking Sacrifice Arc",
            category = "Emotional Story",
            description = "Tear-jerking character tribute with gentle sakura dusk filters, deep emotional narrative pacing.",
            durationSeconds = 30,
            tag = "Emotional",
            filter = AnimeFilter.SAKURA_DUSK,
            sampleCaptions = listOf(
                "HE SMILED SO THEY COULD LIVE..." to (0L to 5000L),
                "THE PROMISE THAT BROKE EVERY FAN'S HEART." to (5500L to 13000L),
                "YOU WILL NEVER BE FORGOTTEN, LEGEND." to (13500L to 23000L),
                "TAG SOMEONE WHO CRIED AT THIS SCENE." to (23500L to 29500L)
            ),
            musicName = "Piano & Strings Melancholy"
        ),
        AnimeTemplate(
            id = "template_top5_list",
            title = "Top 5 Overpowered Characters",
            category = "Rankings & Lists",
            description = "Countdown structure optimized for high retention YouTube Shorts and Reels.",
            durationSeconds = 60,
            tag = "High Retention",
            filter = AnimeFilter.VIBRANT_SHONEN,
            sampleCaptions = listOf(
                "TOP 5 MOST OVERPOWERED ANIME CHARACTERS!" to (0L to 4500L),
                "#5: THE TIME MANIPULATOR" to (5000L to 14000L),
                "#4: THE VOID EMPEROR" to (14500L to 25000L),
                "#3: THE UNBEATABLE SENSEI" to (25500L to 37000L),
                "#2: THE CELESTIAL GODDESS" to (37500L to 48000L),
                "#1 IS BANNED IN 12 UNIVERSES!" to (48500L to 59500L)
            ),
            musicName = "Countdown Trap Beat"
        ),
        AnimeTemplate(
            id = "template_facts_theories",
            title = "Mind-Blowing Anime Theory",
            category = "Theories & Lore",
            description = "Dark mystery ambiance with Manga Noir filter, uncovering secrets nobody noticed.",
            durationSeconds = 30,
            tag = "Anime Lore",
            filter = AnimeFilter.MANGA_NOIR,
            sampleCaptions = listOf(
                "DID YOU NOTICE THIS CLUE IN EPISODE 1?" to (0L to 5500L),
                "THE SHADOW WAS NEVER THE ENEMY..." to (6000L to 14000L),
                "THE ENTIRE WORLD IS AN ILLUSION!" to (14500L to 23500L),
                "WHAT DO YOU THINK OF THIS THEORY?" to (24000L to 29500L)
            ),
            musicName = "Dark Ambient Cyberpulse"
        )
    )

    fun createProjectFromTemplate(template: AnimeTemplate): AnimeProject {
        val captions = template.sampleCaptions.mapIndexed { idx, (text, range) ->
            CaptionItem(
                id = "cap_${template.id}_$idx",
                text = text,
                startMs = range.first,
                endMs = range.second,
                style = if (template.filter == AnimeFilter.MANGA_NOIR) CaptionStylePreset.MINIMAL_WHITE
                else if (template.filter == AnimeFilter.VIBRANT_SHONEN) CaptionStylePreset.BOLD_IMPACT
                else CaptionStylePreset.NEON_GLOW
            )
        }

        // Demo sample clips representing anime shots
        val sampleClips = listOf(
            VideoClipItem(
                id = "clip_1",
                uriString = "sample://anime_scene_1",
                title = "Scene 1: Dramatic Hook",
                durationMs = 5000L,
                startTrimMs = 0L,
                endTrimMs = 5000L,
                filter = template.filter,
                transition = AnimeTransition.FLASH_WHITE,
                orderIndex = 0
            ),
            VideoClipItem(
                id = "clip_2",
                uriString = "sample://anime_scene_2",
                title = "Scene 2: Energy Surge",
                durationMs = 6000L,
                startTrimMs = 0L,
                endTrimMs = 6000L,
                filter = template.filter,
                transition = AnimeTransition.ZOOM_IN,
                orderIndex = 1
            ),
            VideoClipItem(
                id = "clip_3",
                uriString = "sample://anime_scene_3",
                title = "Scene 3: Climax & CTA",
                durationMs = (template.durationSeconds * 1000L - 11000L).coerceAtLeast(4000L),
                startTrimMs = 0L,
                endTrimMs = (template.durationSeconds * 1000L - 11000L).coerceAtLeast(4000L),
                filter = template.filter,
                transition = AnimeTransition.CUT,
                orderIndex = 2
            )
        )

        return AnimeProject(
            id = 0,
            title = template.title,
            durationMs = template.durationSeconds * 1000L,
            aspectRatio = AspectRatio.PORTRAIT_9_16,
            resolution = ExportResolution.RES_1080P,
            fps = ExportFps.FPS_30,
            clips = sampleClips,
            captions = captions,
            activeFilter = template.filter
        )
    }
}
