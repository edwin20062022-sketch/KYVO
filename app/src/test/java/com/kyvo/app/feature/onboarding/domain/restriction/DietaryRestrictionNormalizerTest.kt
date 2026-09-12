package com.kyvo.app.feature.onboarding.domain.restriction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DietaryRestrictionNormalizerTest {

    @Test
    fun trimSpaces() {
        assertEquals("sin gluten", DietaryRestrictionNormalizer.normalize("  sin gluten  "))
    }

    @Test
    fun lowercase() {
        assertEquals("sin gluten", DietaryRestrictionNormalizer.normalize("SIN GLUTEN"))
    }

    @Test
    fun mixedCase() {
        assertEquals("sin lacteos", DietaryRestrictionNormalizer.normalize("Sin LÁCTEOS"))
    }

    @Test
    fun removeAccents() {
        assertEquals("sin lacteos", DietaryRestrictionNormalizer.normalize("Sin Lácteos"))
    }

    @Test
    fun collapseMultipleSpaces() {
        assertEquals("intolerancia a la lactosa", DietaryRestrictionNormalizer.normalize("intolerancia   a   la   lactosa"))
    }

    @Test
    fun removePunctuation() {
        assertEquals("cacahuate", DietaryRestrictionNormalizer.normalize("CACAHUATE!!!"))
    }

    @Test
    fun removePunctuationMixed() {
        assertEquals("sin trigo y centeno", DietaryRestrictionNormalizer.normalize("sin trigo, y centeno."))
    }

    @Test
    fun emptyString() {
        assertEquals("", DietaryRestrictionNormalizer.normalize(""))
    }

    @Test
    fun blankString() {
        assertEquals("", DietaryRestrictionNormalizer.normalize("   "))
    }

    @Test
    fun preserveMeaningfulWords() {
        val result = DietaryRestrictionNormalizer.normalize("Sin Gluten")
        assertTrue(result.contains("sin"))
        assertTrue(result.contains("gluten"))
    }

    @Test
    fun complexRestriction() {
        assertEquals(
            "evito alimentos que contengan proteina de leche de vaca",
            DietaryRestrictionNormalizer.normalize("Evito alimentos que contengan proteína de leche de vaca"),
        )
    }

    @Test
    fun accentsAndPunctuation() {
        assertEquals("no consumo camarones ni gambas", DietaryRestrictionNormalizer.normalize("No consumo camarones, ni gambas!"))
    }
}
