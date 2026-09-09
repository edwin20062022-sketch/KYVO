package com.kyvo.app.feature.home

import com.kyvo.app.feature.home.presentation.MACRO_ICON_SIZE
import com.kyvo.app.feature.home.presentation.MACRO_LABEL_FONT_SIZE
import com.kyvo.app.feature.home.presentation.MACRO_RING_DIAMETER
import com.kyvo.app.feature.home.presentation.MACRO_RING_SPACING
import com.kyvo.app.feature.home.presentation.MACRO_CIRCLE_SHOWS_PERCENTAGE
import com.kyvo.app.feature.home.presentation.DASHBOARD_MACRO_LABELS
import com.kyvo.app.feature.home.presentation.macroLabelFontSizeFor
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeMacroLayoutTest {
    @Test
    fun macroIndicatorLayoutUsesCompactResponsiveMeasurementsAndExternalPercentage() {
        assertEquals(84f, MACRO_RING_DIAMETER.value, 0f)
        assertEquals(8f, MACRO_RING_SPACING.value, 0f)
        assertEquals(22f, MACRO_ICON_SIZE.value, 0f)
        assertEquals(10f, MACRO_LABEL_FONT_SIZE.value, 0f)
        assertEquals(9f, macroLabelFontSizeFor(79.dp).value, 0f)
        assertEquals(10f, macroLabelFontSizeFor(80.dp).value, 0f)
        assertEquals(listOf("Proteína", "Carbohidratos", "Grasas"), DASHBOARD_MACRO_LABELS)
        assertEquals(false, MACRO_CIRCLE_SHOWS_PERCENTAGE)
    }
}
