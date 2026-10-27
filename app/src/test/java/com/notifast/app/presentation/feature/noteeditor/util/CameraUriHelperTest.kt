package com.notifast.app.presentation.feature.noteeditor.util

import android.content.Context
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.core.content.FileProvider
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CameraUriHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @After
    fun tearDown() {
        runCatching {
            val field = FileProvider::class.java.getDeclaredField("sCache")
            field.isAccessible = true
            (field.get(null) as? MutableMap<*, *>)?.clear()
        }
    }

    @Test
    fun `createTempPictureFile creates temp file in camera_captures and returns valid content uri`() {
        val (file, uri) = CameraUriHelper.createTempPictureFile(context)

        assertNotNull(file)
        assertNotNull(uri)
        assertTrue(file.exists())
        assertEquals(context.cacheDir.resolve("camera_captures").absolutePath, file.parentFile?.absolutePath)
        assertTrue(file.name.startsWith("note_camera_"))
        assertTrue(file.name.endsWith(".jpg"))
        assertEquals("content", uri.scheme)
        assertTrue(uri.toString().contains("fileprovider"))

        // Cleanup
        file.delete()
    }

    @Test
    fun `cleanupEmptyFile deletes empty file`() {
        val cacheFolder = File(context.cacheDir, "camera_captures").apply { mkdirs() }
        val emptyFile = File.createTempFile("test_empty_", ".jpg", cacheFolder)
        assertTrue(emptyFile.exists())
        assertEquals(0L, emptyFile.length())

        CameraUriHelper.cleanupEmptyFile(emptyFile)

        assertFalse(emptyFile.exists())
    }

    @Test
    fun `cleanupEmptyFile does not delete non-empty file`() {
        val cacheFolder = File(context.cacheDir, "camera_captures").apply { mkdirs() }
        val contentFile = File.createTempFile("test_content_", ".jpg", cacheFolder)
        contentFile.writeText("sample image content")
        assertTrue(contentFile.length() > 0)

        CameraUriHelper.cleanupEmptyFile(contentFile)

        assertTrue(contentFile.exists())

        // Cleanup
        contentFile.delete()
    }

    @Test
    fun `cleanupEmptyFile handles null or non-existent file gracefully`() {
        CameraUriHelper.cleanupEmptyFile(null)
        val nonExistent = File(context.cacheDir, "does_not_exist.jpg")
        CameraUriHelper.cleanupEmptyFile(nonExistent)
        // No exception thrown
    }
}
