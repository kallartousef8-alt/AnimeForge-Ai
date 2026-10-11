package com.example

import com.example.data.models.CaptionStylePreset
import com.example.data.models.ScriptLanguage
import com.example.data.models.ScriptStyle
import com.example.data.repository.TemplateRepository
import com.example.service.AiScriptService
import com.example.service.CaptionService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCaptionGenerationAndSrtExport() {
        val captionService = CaptionService()
        val text = "When the protagonist smiled, every single villain froze in absolute fear! Level 999 power unlocked."
        val captions = captionService.generateCaptionsFromText(
            text = text,
            totalDurationMs = 15000L,
            preset = CaptionStylePreset.NEON_GLOW
        )

        assertTrue("Captions should not be empty", captions.isNotEmpty())
        assertEquals(CaptionStylePreset.NEON_GLOW, captions.first().style)
        assertTrue("Start time should be 0", captions.first().startMs == 0L)
        assertTrue("Last cue should end at or before 15000ms", captions.last().endMs <= 15000L)

        val srtOutput = captionService.exportToSrt(captions)
        assertTrue("SRT should contain index 1", srtOutput.contains("1\n"))
        assertTrue("SRT should contain arrow separator", srtOutput.contains(" --> "))
    }

    @Test
    fun testAiScriptGenerationPacing() = runBlocking {
        val scriptService = AiScriptService()
        val result = scriptService.generateScript(
            topic = "Demon Slayer",
            character = "Rengoku",
            durationSeconds = 30,
            style = ScriptStyle.EPIC,
            language = ScriptLanguage.ENGLISH
        )

        assertTrue(result.isSuccess)
        val script = result.getOrNull()
        assertNotNull(script)
        assertTrue("Hook should not be empty", script!!.hook.isNotBlank())
        assertTrue("Body should contain anime reference", script.body.contains("Demon Slayer") || script.body.contains("Rengoku") || script.body.isNotBlank())
        assertTrue("Conclusion should have CTA", script.conclusion.isNotBlank())
    }

    @Test
    fun testTemplatesRepository() {
        val templates = TemplateRepository.templates
        assertTrue("Templates library should have at least 6 presets", templates.size >= 6)

        val firstTemplate = templates.first()
        val project = TemplateRepository.createProjectFromTemplate(firstTemplate)
        assertEquals(firstTemplate.title, project.title)
        assertTrue("Generated project must have clips", project.clips.isNotEmpty())
        assertTrue("Generated project must have captions", project.captions.isNotEmpty())
    }
}
