package com.kyvo.app.feature.onboarding

import com.kyvo.app.feature.onboarding.presentation.TRAINING_DAYS_LABEL
import com.kyvo.app.feature.onboarding.presentation.components.wheelInitialIndex
import org.junit.Assert.assertEquals
import org.junit.Test

class WheelPickerMappingTest {
    @Test fun ageVisualValuesAndIndicesCoverZeroThroughOneHundredFifty() {
        val values = (0..150).toList()
        assertEquals(0, wheelInitialIndex(values, 0))
        assertEquals(18, wheelInitialIndex(values, 18))
        assertEquals(150, wheelInitialIndex(values, 150))
    }

    @Test fun heightIndicesMatchTheSelectableRange() {
        val values = (100..300).toList()
        assertEquals(0, wheelInitialIndex(values, 100))
        assertEquals(50, wheelInitialIndex(values, 150))
        assertEquals(200, wheelInitialIndex(values, 300))
    }

    @Test fun trainingDaysLabelDoesNotDuplicateTheBadgeNumber() {
        assertEquals("días a la semana", TRAINING_DAYS_LABEL)
    }
}
