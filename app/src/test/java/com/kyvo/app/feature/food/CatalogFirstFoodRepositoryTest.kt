package com.kyvo.app.feature.food

import com.kyvo.app.feature.food.data.CatalogFirstFoodRepository
import com.kyvo.app.feature.food.data.FoodSearchEdgeGateway
import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogFirstFoodRepositoryTest {
    @Test fun `uses catalog without invoking edge when it has enough results`() = runTest {
        val edge = FakeEdge(listOf(result("edge")))
        val repository = CatalogFirstFoodRepository(FakeCatalog((1..5).map { result("catalog-$it") }), edge)

        assertEquals(5, repository.searchFoods("tomate").size)
        assertEquals(0, edge.calls)
    }

    @Test fun `uses edge fallback when catalog has too few results`() = runTest {
        val edge = FakeEdge(listOf(result("usda-1"), result("off-1", FoodType.Commercial)))
        val repository = CatalogFirstFoodRepository(FakeCatalog(listOf(result("catalog-1"))), edge)

        assertEquals(3, repository.searchFoods("tomate").size)
        assertEquals(1, edge.calls)
    }

    private fun result(id: String, type: FoodType = FoodType.Generic) = FoodSearchResult(id, type, "Tomate", null, null, null, FoodNutrients(18.0, 0.9, 3.9, 0.2), "100 g")

    private class FakeCatalog(private val foods: List<FoodSearchResult>) : FoodRepository {
        override suspend fun searchFoods(query: String) = foods
        override suspend fun getFood(id: String): FoodDetail? = null
    }

    private class FakeEdge(private val foods: List<FoodSearchResult>) : FoodSearchEdgeGateway {
        var calls = 0
        override suspend fun searchFoods(query: String): List<FoodSearchResult> { calls++; return foods }
        override suspend fun getFood(id: String, type: String): FoodDetail? = null
    }
}
