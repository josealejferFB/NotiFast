package com.notifast.app.presentation.feature.notes.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.robolectric.RuntimeEnvironment
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NoteShareHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `collectCandidatePaths extracts and deduplicates cover image and block images`() {
        val note = Note(
            id = "note-1",
            imageUrl = "images/notes/cover.jpg",
            blocks = listOf(
                NoteBlock.Heading(id = "h1", text = "Header", level = 1),
                NoteBlock.Image(id = "img1", path = "images/notes/cover.jpg", caption = "Cover duplicate"),
                NoteBlock.Image(id = "img2", path = "images/notes/diagram.jpg", caption = "Diagram")
            )
        )

        val paths = NoteShareHelper.collectCandidatePaths(note)
        assertEquals(2, paths.size)
        assertEquals("images/notes/cover.jpg", paths[0])
        assertEquals("images/notes/diagram.jpg", paths[1])
    }

    @Test
    fun `createShareIntent without images produces text plain send intent`() {
        val note = Note(id = "note-1", title = "Apunte de Química")
        val shareText = "*Apunte de Química*\nContenido"

        val intent = NoteShareHelper.createShareIntent(
            context = context,
            note = note,
            shareText = shareText,
            imageUris = emptyList()
        )

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("Apunte de Química", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(shareText, intent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun `createShareIntent with 1 image produces image send intent with stream and read permission`() {
        val note = Note(id = "note-1", title = "Apunte con Imagen")
        val shareText = "*Apunte con Imagen*\nContenido"
        val imageUri = Uri.parse("content://com.notifast.app.fileprovider/notes/img1.jpg")

        val intent = NoteShareHelper.createShareIntent(
            context = context,
            note = note,
            shareText = shareText,
            imageUris = listOf(imageUri)
        )

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("image/*", intent.type)
        assertEquals(imageUri, intent.getParcelableExtra(Intent.EXTRA_STREAM))
        assertEquals("Apunte con Imagen", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(shareText, intent.getStringExtra(Intent.EXTRA_TEXT))
        assertTrue((intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
        assertNotNull(intent.clipData)
        assertEquals(imageUri, intent.clipData?.getItemAt(0)?.uri)
    }

    @Test
    fun `createShareIntent with multiple images produces send multiple intent`() {
        val note = Note(id = "note-1", title = "Apunte con Varias Imágenes")
        val shareText = "*Apunte con Varias Imágenes*\nContenido"
        val uri1 = Uri.parse("content://com.notifast.app.fileprovider/notes/img1.jpg")
        val uri2 = Uri.parse("content://com.notifast.app.fileprovider/notes/img2.jpg")

        val intent = NoteShareHelper.createShareIntent(
            context = context,
            note = note,
            shareText = shareText,
            imageUris = listOf(uri1, uri2)
        )

        assertEquals(Intent.ACTION_SEND_MULTIPLE, intent.action)
        assertEquals("image/*", intent.type)
        val streamList = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        assertEquals(2, streamList?.size)
        assertEquals(uri1, streamList?.get(0))
        assertEquals(uri2, streamList?.get(1))
        assertEquals("Apunte con Varias Imágenes", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(shareText, intent.getStringExtra(Intent.EXTRA_TEXT))
        assertTrue((intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
        assertEquals(2, intent.clipData?.itemCount)
    }

    @Test
    fun `resolveImageUri returns content uri directly when path starts with content`() {
        val rawPath = "content://media/external/images/media/42"
        val resolved = NoteShareHelper.resolveImageUri(context, rawPath)
        assertEquals(Uri.parse(rawPath), resolved)
    }

    @Test
    fun `resolveImageUri handles existing file and FileProvider`() {
        val noteDir = File(context.filesDir, "images/notes").apply { mkdirs() }
        val testImageFile = File(noteDir, "sample.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }

        val resolved = NoteShareHelper.resolveImageUri(context, "images/notes/sample.jpg")
        assertNotNull(resolved)
        assertTrue(resolved.toString().startsWith("content://"))
    }
}
