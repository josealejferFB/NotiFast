package com.notifast.app.data.backup

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.roundToInt

class ImageOptimizer @Inject constructor() {

    companion object {
        const val MAX_SIDE = 1600
        const val JPEG_QUALITY = 80
    }

    fun optimize(source: File, target: File): Result<File> {
        return try {
            if (!isSupportedImage(source)) {
                return Result.failure(IllegalArgumentException("File is not a supported image: ${source.name}"))
            }

            // 1. Decode bounds
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(source.absolutePath, boundsOptions)
            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) {
                return Result.failure(IllegalArgumentException("File is not a valid decodable image: ${source.name}"))
            }

            // 2. Compute inSampleSize
            var inSampleSize = 1
            val maxDim = maxOf(srcWidth, srcHeight)
            if (maxDim > MAX_SIDE) {
                var halfDim = maxDim / 2
                while (halfDim / inSampleSize >= MAX_SIDE) {
                    inSampleSize *= 2
                }
            }

            // 3. Decode bitmap with inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val sampledBitmap = BitmapFactory.decodeFile(source.absolutePath, decodeOptions)
                ?: return Result.failure(IllegalStateException("Failed to decode image bitmap: ${source.name}"))

            // 4. Handle EXIF orientation
            val orientedBitmap = applyExifOrientation(source, sampledBitmap)

            // 5. Scale to exact max 1600 if still larger
            val finalBitmap = scaleIfNeeded(orientedBitmap, MAX_SIDE)

            // 6. Compress to JPEG 80 in memory
            val byteOut = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteOut)
            val compressedBytes = byteOut.toByteArray()

            if (finalBitmap != orientedBitmap && !orientedBitmap.isRecycled) {
                orientedBitmap.recycle()
            }
            if (finalBitmap != sampledBitmap && !sampledBitmap.isRecycled) {
                sampledBitmap.recycle()
            }
            if (!finalBitmap.isRecycled) {
                finalBitmap.recycle()
            }

            // 7. If original is already within bounds and recompressed size is not smaller, keep original
            val isOriginalWithinBounds = srcWidth <= MAX_SIDE && srcHeight <= MAX_SIDE
            if (isOriginalWithinBounds && compressedBytes.size >= source.length()) {
                source.copyTo(target, overwrite = true)
            } else {
                target.parentFile?.mkdirs()
                FileOutputStream(target).use { fos ->
                    fos.write(compressedBytes)
                }
            }

            Result.success(target)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun scaleIfNeeded(bitmap: Bitmap, maxSide: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val maxDim = maxOf(w, h)
        if (maxDim <= maxSide) {
            return bitmap
        }

        val scale = maxSide.toFloat() / maxDim
        val targetW = (w * scale).roundToInt().coerceAtLeast(1)
        val targetH = (h * scale).roundToInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun applyExifOrientation(source: File, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(source.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }

            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun isSupportedImage(file: File): Boolean {
        if (!file.exists() || !file.isFile || file.length() < 4) return false
        val header = ByteArray(12)
        val read = file.inputStream().use { it.read(header) }
        if (read < 3) return false

        // JPEG: FF D8 FF
        if (header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()) {
            return true
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (read >= 8 &&
            header[0] == 0x89.toByte() && header[1] == 0x50.toByte() &&
            header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()
        ) {
            return true
        }
        // GIF: GIF8
        if (read >= 4 &&
            header[0] == 0x47.toByte() && header[1] == 0x49.toByte() &&
            header[2] == 0x46.toByte() && header[3] == 0x38.toByte()
        ) {
            return true
        }
        // BMP: BM
        if (header[0] == 0x42.toByte() && header[1] == 0x4D.toByte()) {
            return true
        }
        // WEBP: RIFF....WEBP
        if (read >= 12 &&
            header[0] == 0x52.toByte() && header[1] == 0x49.toByte() &&
            header[2] == 0x46.toByte() && header[3] == 0x46.toByte() &&
            header[8] == 0x57.toByte() && header[9] == 0x45.toByte() &&
            header[10] == 0x42.toByte() && header[11] == 0x50.toByte()
        ) {
            return true
        }
        return false
    }
}
