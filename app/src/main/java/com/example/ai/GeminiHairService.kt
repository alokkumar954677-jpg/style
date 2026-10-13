package com.example.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiHairService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Multimodal face diagnostic using gemini-3.5-flash
     */
    suspend fun analyzeFaceMultimodal(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) return@withContext null

        try {
            val base64Image = bitmap.toBase64()
            val prompt = """
                You are an elite master hair stylist and facial aesthetics consultant.
                Analyze this front-facing portrait photo:
                1. Identify the exact face shape (Oval, Round, Square, Oblong, Heart, Diamond).
                2. Evaluate forehead proportions, cheekbone prominence, and jawline angles.
                3. Recommend top flattering haircuts and hair colors that accentuate best features.
                4. Provide specific professional salon maintenance tips.
                Keep your answer structured, elegant, concise, and professional.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val candidates = json.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                return@withContext text
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    /**
     * AI image hairstyle transformation using gemini-2.5-flash-image
     */
    suspend fun transformHairstyleImage(
        originalBitmap: Bitmap,
        styleName: String,
        colorName: String
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) return@withContext null

        try {
            val base64Image = originalBitmap.toBase64()
            val prompt = """
                A photorealistic high-end hair studio transformation.
                Keep this exact person's face, facial features, skin tone, eyes, and natural identity 100% identical.
                Change their hairstyle to a modern $styleName haircut with $colorName hair color.
                Ensure ultra-realistic hair strands, natural hairline blending, realistic lighting, and premium salon finish.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", "1:1")
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val candidates = json.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val data = inlineData.optString("data")
                            if (!data.isNullOrBlank()) {
                                val bytes = Base64.decode(data, Base64.DEFAULT)
                                return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize if too huge for fast network payload
        val maxDim = 800
        val scale = if (width > maxDim || height > maxDim) {
            maxDim.toFloat() / maxOf(width, height)
        } else 1.0f

        val scaled = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
        } else this

        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
