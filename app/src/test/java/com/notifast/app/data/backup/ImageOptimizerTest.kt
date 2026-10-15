package com.notifast.app.data.backup

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageOptimizerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var optimizer: ImageOptimizer

    @Before
    fun setUp() {
        optimizer = ImageOptimizer()
    }

    @Test
    fun large_image_is_scaled_down_to_max_1600_with_aspect_ratio() {
        val srcFile = tempFolder.newFile("large.jpg")
        val bmp = Bitmap.createBitmap(4000, 3000, Bitmap.Config.ARGB_8888)
        FileOutputStream(srcFile).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        val targetFile = tempFolder.newFile("optimized_large.jpg")
        val result = optimizer.optimize(srcFile, targetFile)

        assertTrue(result.isSuccess)
        val optFile = result.getOrThrow()
        assertTrue(optFile.exists())

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(optFile.absolutePath, options)
        assertEquals(1600, options.outWidth)
        assertEquals(1200, options.outHeight)
    }

    @Test
    fun already_small_image_keeps_original_if_recompression_is_not_smaller() {
        // Create a small, low-entropy image with low quality compression
        val srcFile = tempFolder.newFile("small.jpg")
        val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        FileOutputStream(srcFile).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 20, out) // highly compressed already
        }
        val originalBytes = srcFile.readBytes()

        val targetFile = tempFolder.newFile("optimized_small.jpg")
        val result = optimizer.optimize(srcFile, targetFile)

        assertTrue(result.isSuccess)
        val optFile = result.getOrThrow()
        // If recompression is not smaller, it should preserve the original file/bytes
        assertTrue(optFile.length() <= originalBytes.size)
    }

    @Test
    fun non_image_file_returns_failure() {
        val textFile = tempFolder.newFile("not_an_image.txt").apply {
            writeText("Hello, this is just plain text, not a valid image!")
        }
        val targetFile = tempFolder.newFile("output.jpg")

        val result = optimizer.optimize(textFile, targetFile)
        assertTrue(result.isFailure)
    }
}
