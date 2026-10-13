package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.InputStream
import kotlin.math.max

object ImageUtils {

    private const val TAG = "ImageUtils"
    private const val MAX_DIMENSION = 1280

    /**
     * Decodes and corrects orientation for a captured photo file.
     * Optionally mirrors the image if captured with front-facing camera.
     */
    fun decodeAndCorrectOrientation(filePath: String, isFrontCamera: Boolean = false): Bitmap? {
        return try {
            val file = File(filePath)
            if (!file.exists() || file.length() == 0L) return null

            // 1. Get image dimensions first without decoding full image
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)

            // 2. Compute inSampleSize to avoid OOM
            options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            val rawBitmap = BitmapFactory.decodeFile(filePath, options) ?: return null

            // 3. Read EXIF orientation
            val exif = try {
                ExifInterface(filePath)
            } catch (e: Exception) {
                null
            }

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            // If front camera and not already flipped, mirror horizontally for natural selfie view
            if (isFrontCamera && orientation != ExifInterface.ORIENTATION_FLIP_HORIZONTAL) {
                matrix.postScale(-1f, 1f, rawBitmap.width / 2f, rawBitmap.height / 2f)
            }

            if (!matrix.isIdentity) {
                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            } else {
                rawBitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding file: $filePath", e)
            null
        }
    }

    /**
     * Decodes and corrects orientation for an image Uri (from Photo Picker / Gallery).
     */
    fun decodeUriAndCorrect(context: Context, uri: Uri): Bitmap? {
        return try {
            // First decode bounds
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            inputStream = context.contentResolver.openInputStream(uri)
            val rawBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            if (rawBitmap == null) return null

            // Read EXIF from stream if Android N+
            val matrix = Matrix()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { exifStream ->
                        val exif = ExifInterface(exifStream)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not read EXIF from stream: ${e.message}")
                }
            }

            if (!matrix.isIdentity) {
                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            } else {
                rawBitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Uri: $uri", e)
            null
        }
    }

    /**
     * Mirrors bitmap horizontally for front camera selfie preview if needed.
     */
    fun mirrorHorizontally(bitmap: Bitmap): Bitmap {
        return try {
            val matrix = Matrix().apply {
                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }
}
