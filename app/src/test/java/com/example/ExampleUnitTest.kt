package com.example

import com.example.data.HairstyleRepository
import com.example.model.FaceShape
import com.example.model.HairColor
import com.example.model.HairGender
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun hairstylesRepository_containsAll28Styles() {
        val styles = HairstyleRepository.allHairstyles
        assertTrue("Should contain at least 28 styles", styles.size >= 28)

        // Verify Men's cuts
        val menStyles = styles.filter { it.gender == HairGender.MEN }
        assertTrue("Should contain men cuts", menStyles.isNotEmpty())
        assertNotNull(menStyles.find { it.name == "Textured Crop" })
        assertNotNull(menStyles.find { it.name == "Low Fade" })
        assertNotNull(menStyles.find { it.name == "Pompadour" })
        assertNotNull(menStyles.find { it.name == "Curtains" })

        // Verify Women's cuts
        val womenStyles = styles.filter { it.gender == HairGender.WOMEN }
        assertTrue("Should contain women cuts", womenStyles.isNotEmpty())
        assertNotNull(womenStyles.find { it.name == "Butterfly Cut" })
        assertNotNull(womenStyles.find { it.name == "Bob Cut" })
        assertNotNull(womenStyles.find { it.name == "Curtain Bangs" })
        assertNotNull(womenStyles.find { it.name == "Beach Waves" })
    }

    @Test
    fun faceShapeMatching_returnsValidRecommendations() {
        for (shape in FaceShape.values()) {
            val recs = HairstyleRepository.getRecommendedForFace(shape)
            assertTrue("Recommendations for ${shape.name} should not be empty", recs.isNotEmpty())
        }
    }

    @Test
    fun hairColors_containsAllRequestedShades() {
        val colorIds = HairColor.values().map { it.id }
        assertTrue(colorIds.contains("natural_black"))
        assertTrue(colorIds.contains("dark_brown"))
        assertTrue(colorIds.contains("light_brown"))
        assertTrue(colorIds.contains("ash_brown"))
        assertTrue(colorIds.contains("blonde"))
        assertTrue(colorIds.contains("platinum_blonde"))
        assertTrue(colorIds.contains("burgundy"))
        assertTrue(colorIds.contains("copper"))
        assertTrue(colorIds.contains("silver_grey"))
    }
}
