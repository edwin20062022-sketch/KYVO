package com.kyvo.app.feature.food.data

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodServing
import com.kyvo.app.feature.food.domain.model.FoodType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable

/** Calls KYVO's Supabase Edge Functions; provider credentials never reach Android. */
class SupabaseFoodSearchEdgeGateway(private val client: SupabaseClient) : FoodSearchEdgeGateway {
    override suspend fun searchFoods(query: String): List<FoodSearchResult> =
        client.functions.invoke(
            function = "food-search",
            body = FoodSearchRequest(query),
            headers = jsonHeaders(),
        ).body<FoodSearchResponse>().foods.map(EdgeFood::searchResult)

    override suspend fun getFood(id: String, type: String): FoodDetail? = runCatching {
        client.functions.invoke(
            function = "food-detail",
            body = FoodDetailRequest(id, type),
            headers = jsonHeaders(),
        ).body<FoodDetailResponse>().food?.detail()
    }.getOrNull()

    private fun jsonHeaders() = Headers.build { append(HttpHeaders.ContentType, ContentType.Application.Json.toString()) }
}

@Serializable private data class FoodSearchRequest(val query: String)
@Serializable private data class FoodDetailRequest(val id: String, val type: String)
@Serializable private data class FoodSearchResponse(val foods: List<EdgeFood> = emptyList())
@Serializable private data class FoodDetailResponse(val food: EdgeFood? = null)
@Serializable private data class EdgeFood(
    val id: String,
    val type: String,
    val name: String,
    val variant: String? = null,
    val brand: String? = null,
    val imageUrl: String? = null,
    val nutrients: EdgeNutrients,
    val servings: List<EdgeServing> = emptyList(),
) {
    fun searchResult() = FoodSearchResult(
        id = id,
        type = type.toFoodType(),
        name = name,
        subtitle = variant,
        brand = brand,
        imageKey = null,
        nutrients = nutrients.model(),
        servingLabel = servings.firstOrNull()?.label ?: "100 g",
    )

    fun detail() = FoodDetail(id, type.toFoodType(), name, variant, null, nutrients.model(), servings.map { FoodServing(it.label, it.gramEquivalent, it.source) })
}
@Serializable private data class EdgeNutrients(val caloriesPer100g: Double? = null, val proteinPer100g: Double? = null, val carbohydratesPer100g: Double? = null, val fatPer100g: Double? = null, val fiberPer100g: Double? = null, val sugarPer100g: Double? = null, val sodiumMgPer100g: Double? = null) { fun model() = FoodNutrients(caloriesPer100g, proteinPer100g, carbohydratesPer100g, fatPer100g, fiberPer100g, sugarPer100g, sodiumMgPer100g) }
@Serializable private data class EdgeServing(val label: String, val gramEquivalent: Double, val source: String)
private fun String.toFoodType() = if (this == "commercial") FoodType.Commercial else FoodType.Generic
