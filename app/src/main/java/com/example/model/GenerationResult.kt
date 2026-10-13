package com.example.model

import android.graphics.Bitmap

data class GeneratedLook(
    val id: String,
    val title: String,
    val hairstyleName: String,
    val hairColor: HairColor,
    val faceShape: FaceShape,
    val badge: String,
    val matchPercentage: Int,
    val stylistAnalysis: String,
    val shortDescription: String = "",
    val hairLength: String = "Medium",
    val maintenanceLevel: String = "Medium",
    val generatedBitmap: Bitmap? = null,
    val generatedImageBase64: String? = null,
    val localUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class FaceAnalysisResult(
    val detectedShape: FaceShape,
    val confidence: Int, // 85% - 99%
    val foreheadWidth: String,
    val cheekboneWidth: String,
    val jawlineStructure: String,
    val facialRatio: String,
    val bestStyleRecommendation: String,
    val stylistSummary: String
)
