package com.example.service

import com.example.data.models.CaptionItem
import com.example.data.models.CaptionStylePreset
import java.io.File
import java.util.Locale
import java.util.UUID

class CaptionService {

    private val viralAnimeKeywords = setOf(
        "GOD", "POWER", "LIMITER", "AWAKEN", "AWAKENING", "TITAN", "DOMAIN", "DEMON",
        "LEVEL", "SOUL", "LEGEND", "LEGENDARY", "KILL", "STRIKE", "BEAT", "CHILLS",
        "IMMORTAL", "OVERPOWERED", "UNSTOPPABLE", "SACRIFICE", "CLASH", "WARRIOR",
        "DEATH", "SECRET", "GOOSEBUMPS", "ULTIMATE", "BEAST", "RAGE", "INFINITY"
    )

    fun containsHighImpactKeyword(text: String): Boolean {
        val upper = text.uppercase()
        return viralAnimeKeywords.any { upper.contains(it) }
    }

    /**
     * Splits narration text into dynamic high-retention anime shorts captions (1 to 4 words per cue).
     * Distributes evenly across [totalDurationMs], synchronized to audio.
     */
    fun generateCaptionsFromText(
        text: String,
        totalDurationMs: Long,
        preset: CaptionStylePreset = CaptionStylePreset.NEON_GLOW,
        fontSizeSp: Float = 22f,
        highlightColorHex: String = "#FF2E93"
    ): List<CaptionItem> {
        val clean = text.replace("\n", " ").trim()
        if (clean.isBlank() || totalDurationMs <= 0) return emptyList()

        val words = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        // Chunk words into punchy 2-4 word phrases (Shorts / Reels fast caption style)
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()
        var currentCount = 0

        for (word in words) {
            if (currentCount > 0) currentChunk.append(" ")
            currentChunk.append(word)
            currentCount++

            // Break on punctuation or after 3 words
            if (word.endsWith(".") || word.endsWith("!") || word.endsWith("?") || word.endsWith(",") || currentCount >= 3) {
                chunks.add(currentChunk.toString().trim())
                currentChunk = StringBuilder()
                currentCount = 0
            }
        }
        if (currentChunk.isNotBlank()) {
            chunks.add(currentChunk.toString().trim())
        }

        val totalChunks = chunks.size
        val chunkDuration = (totalDurationMs / totalChunks).coerceAtLeast(500L)
        val result = mutableListOf<CaptionItem>()

        for (i in 0 until totalChunks) {
            val startMs = (i * chunkDuration).coerceAtMost(totalDurationMs)
            val endMs = ((i + 1) * chunkDuration).coerceAtMost(totalDurationMs)
            val chunkText = chunks[i]
            val hasKeyword = containsHighImpactKeyword(chunkText)

            result.add(
                CaptionItem(
                    id = UUID.randomUUID().toString(),
                    text = chunkText,
                    startMs = startMs,
                    endMs = endMs,
                    style = preset,
                    fontSizeSp = fontSizeSp,
                    yPositionRatio = 0.78f, // Safe zone for YouTube Shorts & TikTok
                    highlightKeywords = hasKeyword,
                    highlightColorHex = highlightColorHex
                )
            )
        }

        return result
    }

    /**
     * Generates a standard .SRT subtitle file format string.
     */
    fun exportToSrt(captions: List<CaptionItem>): String {
        val sb = StringBuilder()
        val sorted = captions.sortedBy { it.startMs }

        for ((index, item) in sorted.withIndex()) {
            sb.append("${index + 1}\n")
            sb.append("${formatSrtTime(item.startMs)} --> ${formatSrtTime(item.endMs)}\n")
            sb.append("${item.text}\n\n")
        }

        return sb.toString()
    }

    private fun formatSrtTime(millis: Long): String {
        val hrs = millis / 3600000
        val mins = (millis % 3600000) / 60000
        val secs = (millis % 60000) / 1000
        val ms = millis % 1000
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hrs, mins, secs, ms)
    }

    fun saveSrtToFile(file: File, captions: List<CaptionItem>) {
        val srtContent = exportToSrt(captions)
        file.writeText(srtContent)
    }
}
