package com.kyvo.app.feature.onboarding.domain.restriction

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DietaryRestrictionMatcherTest {

    @Test
    fun identicalStrings() {
        assertTrue(DietaryRestrictionMatcher.isLikelySameRestriction("sin gluten", "sin gluten"))
    }

    @Test
    fun caseDifference() {
        assertTrue(DietaryRestrictionMatcher.isLikelySameRestriction("Sin Gluten", "SIN GLUTEN"))
    }

    @Test
    fun whitespaceDifference() {
        assertTrue(DietaryRestrictionMatcher.isLikelySameRestriction("sin gluten", "  sin  gluten  "))
    }

    @Test
    fun accentDifference() {
        assertTrue(DietaryRestrictionMatcher.isLikelySameRestriction("Sin Lácteos", "Sin Lacteos"))
    }

    @Test
    fun similarPhrases_positive() {
        assertTrue(DietaryRestrictionMatcher.isLikelySameRestriction("intolerancia lactosa", "intolerancia a la lactosa"))
    }

    @Test
    fun similarPhrases_smallVariation() {
        assertTrue(DietaryRestrictionMatcher.isLikelySameRestriction("alergia nuez", "alergia a la nuez"))
    }

    @Test
    fun differentRestrictions_negative() {
        assertFalse(DietaryRestrictionMatcher.isLikelySameRestriction("sin leche", "sin carne"))
    }

    @Test
    fun completelyDifferent_negative() {
        assertFalse(DietaryRestrictionMatcher.isLikelySameRestriction("alergia nuez", "alergia pescado"))
    }

    @Test
    fun sharedPrefixNotEnough() {
        assertFalse(DietaryRestrictionMatcher.isLikelySameRestriction("sin gluten", "sin soya"))
    }

    @Test
    fun emptyStrings() {
        assertFalse(DietaryRestrictionMatcher.isLikelySameRestriction("", "sin gluten"))
    }

    @Test
    fun bothEmpty() {
        assertTrue(DietaryRestrictionMatcher.isLikelySameRestriction("", ""))
    }

    @Test
    fun levenshteinDistance_identical() {
        assertEquals(0, DietaryRestrictionMatcher.levenshteinDistance("abc", "abc"))
    }

    @Test
    fun levenshteinDistance_oneEdit() {
        assertEquals(1, DietaryRestrictionMatcher.levenshteinDistance("abc", "ac"))
    }

    @Test
    fun levenshteinDistance_completelyDifferent() {
        assertEquals(3, DietaryRestrictionMatcher.levenshteinDistance("abc", "xyz"))
    }

    private fun assertEquals(expected: Int, actual: Int) {
        org.junit.Assert.assertEquals(expected.toLong(), actual.toLong())
    }
}
