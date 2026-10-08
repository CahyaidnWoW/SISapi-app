package com.latihan.si_sapimobile.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageHelper {
    private const val MAX_SIDE = 1280          // sisi terpanjang maksimal (px)
    private const val MAX_BYTES = 4_500_000L   // batas server 5 MB, sisakan ruang

    suspend fun compress(raw: File, outDir: File): File? = withContext(Dispatchers.IO) {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(raw.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

            // Foto kamera bisa 50 MP. Pakai sampling agresif supaya hemat memori.
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE) sample *= 2

            var bmp = BitmapFactory.decodeFile(
                raw.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample }
            ) ?: return@withContext null

            val degrees = when (ExifInterface(raw.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (degrees != 0f) {
                val rotated = Bitmap.createBitmap(
                    bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(degrees) }, true
                )
                bmp.recycle()
                bmp = rotated
            }

            outDir.mkdirs()
            val out = File(outDir, "report_${System.currentTimeMillis()}.jpg")
            var quality = 80
            do {
                FileOutputStream(out).use { bmp.compress(Bitmap.CompressFormat.JPEG, quality, it) }
                quality -= 20
            } while (out.length() > MAX_BYTES && quality >= 20)
            bmp.recycle()
            out
        } catch (e: Exception) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }
    }
}
