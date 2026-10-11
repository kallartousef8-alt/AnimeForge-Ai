package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.models.AspectRatio
import com.example.data.repository.AnimeProject
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AnimeForge AI", appName)
    }

    @Test
    fun `test room database project insertion and retrieval`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = ProjectRepository(context)

        val project = AnimeProject(
            title = "Test Anime Short",
            durationMs = 15000L,
            aspectRatio = AspectRatio.PORTRAIT_9_16
        )

        val id = repository.saveProject(project)
        val loaded = repository.getProject(id)

        assertNotNull(loaded)
        assertEquals("Test Anime Short", loaded?.title)
        assertEquals(AspectRatio.PORTRAIT_9_16, loaded?.aspectRatio)
    }
}
