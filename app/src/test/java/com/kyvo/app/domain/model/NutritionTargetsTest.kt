package com.kyvo.app.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test

class NutritionTargetsTest {
    @Test
    fun `calories must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            NutritionTargets(0, 160, 225, 70)
        }
    }

    @Test
    fun `macros cannot be negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            NutritionTargets(2_300, -1, 225, 70)
        }
    }
}

