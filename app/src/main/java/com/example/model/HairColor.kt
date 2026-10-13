package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class HairColor(
    val id: String,
    val displayName: String,
    val swatchColor: Color,
    val hexCode: String,
    val description: String,
    val toneCategory: String
) {
    NATURAL_BLACK("natural_black", "Natural Black", HairBlack, "#1A1A1A", "Deep, rich obsidian with natural high-gloss shine.", "Neutral / Cool"),
    DARK_BROWN("dark_brown", "Dark Brown", HairDarkBrown, "#3D2314", "Deep espresso brunette with subtle warm undertones.", "Warm / Neutral"),
    LIGHT_BROWN("light_brown", "Light Brown", HairLightBrown, "#7A5230", "Chestnut honey caramel tones, soft and luminous.", "Warm"),
    ASH_BROWN("ash_brown", "Ash Brown", HairAshBrown, "#5C544E", "Cool-toned matte smokey taupe with zero brassiness.", "Cool"),
    BLONDE("blonde", "Golden Blonde", HairBlonde, "#D8B573", "Warm radiant sunlight blonde with sun-kissed dimension.", "Warm"),
    PLATINUM_BLONDE("platinum_blonde", "Platinum Blonde", HairPlatinum, "#E4DFD7", "Ultra-light icy Nordic blonde, high fashion editorial.", "Cool"),
    BURGUNDY("burgundy", "Burgundy Wine", HairBurgundy, "#631828", "Vibrant deep velvet cherry red with plum highlights.", "Rich / Dramatic"),
    COPPER("copper", "Vibrant Copper", HairCopper, "#B8532F", "Fiery radiant amber cinnamon with lustrous warmth.", "Warm / Bold"),
    SILVER_GREY("silver_grey", "Silver Grey", HairSilverGrey, "#A1A1AA", "Modern futuristic metallic chrome silver titanium.", "Cool / Chic")
}
