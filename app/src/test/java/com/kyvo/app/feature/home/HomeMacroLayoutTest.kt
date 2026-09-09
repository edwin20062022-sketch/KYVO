package com.kyvo.app.feature.home

import com.kyvo.app.feature.home.presentation.MACRO_ICON_PERCENTAGE_SPACING
import com.kyvo.app.feature.home.presentation.MACRO_ICON_SIZE
import com.kyvo.app.feature.home.presentation.MACRO_RING_DIAMETER
import com.kyvo.app.feature.home.presentation.MACRO_RING_SPACING
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeMacroLayoutTest {
    @Test
    fun macroIndicatorLayoutUsesCompactResponsiveMeasurements() {
        assertEquals(84f, MACRO_RING_DIAMETER.value, 0f)
        assertEquals(8f, MACRO_RING_SPACING.value, 0f)
        assertEquals(22f, MACRO_ICON_SIZE.value, 0f)
        assertEquals(2f, MACRO_ICON_PERCENTAGE_SPACING.value, 0f)
    }
}
