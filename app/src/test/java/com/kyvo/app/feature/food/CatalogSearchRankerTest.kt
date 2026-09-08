package com.kyvo.app.feature.food

import com.kyvo.app.feature.food.data.CatalogCommercialRow
import com.kyvo.app.feature.food.data.CatalogGenericRow
import com.kyvo.app.feature.food.data.rankCatalogResults
import com.kyvo.app.feature.food.data.searchResult
import com.kyvo.app.feature.food.data.servingsFromJson
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogSearchRankerTest {
    private val nutrients = FoodNutrients(165.0, 31.0, 0.0, 3.6, null, null, null)
    private val chicken = CatalogGenericRow("v1", "c1", "Pechuga de pollo", "A la plancha", "chicken.webp", nutrients, emptyList(), listOf("pollo", "pechuga"))
    private val commercial = CatalogCommercialRow("p1", "Pollo listo", "KYVO Foods", null, nutrients, emptyList())

    @Test fun `empty query returns no catalogue result`() { assertTrue(rankCatalogResults("   ", listOf(chicken.searchResult() to chicken.aliases)).isEmpty()) }
    @Test fun `exact canonical name ranks before partial`() { val found = rankCatalogResults("pechuga de pollo", listOf(chicken.searchResult() to chicken.aliases, commercial.searchResult() to emptyList())); assertEquals("v1", found.first().id) }
    @Test fun `alias matches generic food`() { assertEquals("v1", rankCatalogResults("pollo", listOf(chicken.searchResult() to chicken.aliases)).single().id) }
    @Test fun `partial match is supported`() { assertEquals("v1", rankCatalogResults("plancha", listOf(chicken.searchResult() to chicken.aliases)).single().id) }
    @Test fun `commercial mapping preserves nullable nutrition`() { val result = CatalogCommercialRow("p2", "Producto", "Marca", null, FoodNutrients(null, null, null, null), emptyList()).searchResult(); assertEquals(null, result.nutrients.caloriesPer100g) }
    @Test fun `serving data retains known conversion`() { val servings = servingsFromJson(Json.parseToJsonElement("[{\"label\":\"1 pieza\",\"gramEquivalent\":120,\"source\":\"usda\"}]")); assertEquals(120.0, servings.single().gramEquivalent, 0.0) }
}
