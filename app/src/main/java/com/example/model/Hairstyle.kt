package com.example.model

enum class HairGender {
    MEN, WOMEN, UNISEX
}

enum class HairLength(val displayName: String) {
    SHORT("Short"),
    MEDIUM("Medium"),
    LONG("Long")
}

enum class HairTexture(val displayName: String) {
    STRAIGHT("Straight"),
    WAVY("Wavy"),
    CURLY("Curly"),
    COILY("Coily")
}

enum class HairVibe(val displayName: String) {
    MODERN("Modern"),
    CLASSIC("Classic"),
    EDGY("Edgy"),
    ELEGANT("Elegant"),
    CASUAL("Casual")
}

data class Hairstyle(
    val id: String,
    val name: String,
    val gender: HairGender,
    val length: HairLength,
    val texture: HairTexture,
    val vibe: HairVibe,
    val description: String,
    val stylingTip: String,
    val maintenance: String, // "Low", "Medium", "High"
    val recommendedFaceShapes: List<FaceShape>,
    val imageResId: Int? = null,
    val isTrending: Boolean = false,
    val popularityScore: Int = 95
)
