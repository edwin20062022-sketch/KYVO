package com.kyvo.app.feature.food.data

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.food.domain.FrequentFoodCandidate
import com.kyvo.app.feature.food.domain.rankFrequentFoods
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

class SupabaseFoodRepository(private val client: SupabaseClient) : FoodRepository {
    override suspend fun searchFoods(query: String): List<FoodSearchResult> = runCatching {
        val generic = genericRows()
        val commercial = commercialRows()
        rankCatalogResults(query, generic.map { it.searchResult() to it.aliases } + commercial.map { it.searchResult() to emptyList() })
    }.getOrElse { throw FoodRepositoryException("No pudimos consultar el catálogo.", it) }

    override suspend fun getFood(id: String): FoodDetail? = runCatching {
        genericRows().firstOrNull { it.variantId == id }?.detail() ?: commercialRows().firstOrNull { it.id == id }?.detail()
    }.getOrElse { throw FoodRepositoryException("No pudimos cargar el alimento.", it) }

    override suspend fun getFavoriteFoods(): List<FoodSearchResult> = runCatching {
        val favorites = client.from("user_food_favorites").select().decodeList<FavoriteRow>()
        val generic = genericRows().associateBy { it.variantId }
        val commercial = commercialRows().associateBy { it.id }
        favorites.mapNotNull { favorite ->
            when (favorite.foodType) {
                "variant" -> generic[favorite.foodId]?.searchResult()
                "commercial" -> commercial[favorite.foodId]?.searchResult()
                else -> null
            }
        }
    }.getOrElse { throw FoodRepositoryException("No pudimos cargar tus favoritos.", it) }

    override suspend fun isFavorite(foodId: String, type: FoodType): Boolean = runCatching {
        client.from("user_food_favorites").select {
            filter { eq("food_id", foodId); eq("food_type", type.databaseValue) }
            limit(1)
        }.decodeList<FavoriteRow>().isNotEmpty()
    }.getOrElse { throw FoodRepositoryException("No pudimos consultar el favorito.", it) }

    override suspend fun setFavorite(foodId: String, type: FoodType, favorite: Boolean): Unit {
        runCatching {
            val userId = requireNotNull(client.auth.currentUserOrNull()?.id) { "No hay una sesión autenticada." }
            if (favorite) {
                client.from("user_food_favorites").upsert(FavoriteInsert(userId, foodId, type.databaseValue))
            } else {
                client.from("user_food_favorites").delete { filter { eq("user_id", userId); eq("food_id", foodId); eq("food_type", type.databaseValue) } }
            }
        }.getOrElse { throw FoodRepositoryException("No pudimos actualizar el favorito.", it) }
    }

    override suspend fun getFrequentFoods(): List<FoodSearchResult> = runCatching {
        val usage = client.postgrest.rpc("get_frequent_foods", buildJsonObject { put("limit_count", 50) }).decodeList<FrequentFoodRow>()
        val generic = genericRows().associateBy { it.variantId }
        val commercial = commercialRows().associateBy { it.id }
        val candidates = usage.mapNotNull { row ->
            val food = when (row.foodType) {
                "variant" -> generic[row.foodId]?.searchResult()
                "commercial" -> commercial[row.foodId]?.searchResult()
                else -> null
            }
            food?.let { FrequentFoodCandidate(it, row.uses.toInt(), OffsetDateTime.parse(row.lastUsedAt).toLocalDate()) }
        }
        rankFrequentFoods(candidates, LocalDate.now())
    }.getOrElse { throw FoodRepositoryException("No pudimos cargar tus alimentos frecuentes.", it) }

    private suspend fun genericRows(): List<CatalogGenericRow> = client.from("food_variants").select(columns = Columns.raw("id, name_original, variant_name_es, calories_100g, protein_100g, carbohydrates_100g, fat_100g, fiber_100g, sugar_100g, sodium_mg_100g, serving_data, canonical_foods(id, name_es, image_key, food_aliases(alias))")).decodeList<FoodVariantRow>().map { row ->
        val canonical = row.canonical
        CatalogGenericRow(row.id, canonical.id, canonical.nameEs, row.variantNameEs ?: row.nameOriginal, canonical.imageKey, row.nutrients(), servingsFromJson(row.servingData), canonical.aliases.map(FoodAliasRow::alias))
    }
    private suspend fun commercialRows(): List<CatalogCommercialRow> = client.from("commercial_products").select().decodeList<CommercialProductRow>().map { CatalogCommercialRow(it.id, it.nameEs, it.brand, it.imageUrl, it.nutrients(), servingsFromJson(it.servingData)) }
}

private val FoodType.databaseValue get() = if (this == FoodType.Generic) "variant" else "commercial"

@Serializable private data class FavoriteRow(@SerialName("food_id") val foodId: String, @SerialName("food_type") val foodType: String)
@Serializable private data class FavoriteInsert(@SerialName("user_id") val userId: String, @SerialName("food_id") val foodId: String, @SerialName("food_type") val foodType: String)
@Serializable private data class FrequentFoodRow(@SerialName("food_reference") val foodId: String, @SerialName("food_type") val foodType: String, @SerialName("usage_count") val uses: Long, @SerialName("last_used_at") val lastUsedAt: String)

class FoodRepositoryException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Serializable private data class CanonicalFoodRow(val id: String, @SerialName("name_es") val nameEs: String, @SerialName("image_key") val imageKey: String? = null)
@Serializable private data class CanonicalWithAliasesRow(val id: String, @SerialName("name_es") val nameEs: String, @SerialName("image_key") val imageKey: String? = null, @SerialName("food_aliases") val aliases: List<FoodAliasRow> = emptyList())
@Serializable private data class FoodAliasRow(val alias: String)
@Serializable private data class FoodVariantRow(val id: String, @SerialName("name_original") val nameOriginal: String, @SerialName("variant_name_es") val variantNameEs: String? = null, @SerialName("calories_100g") val calories: Double? = null, @SerialName("protein_100g") val protein: Double? = null, @SerialName("carbohydrates_100g") val carbohydrates: Double? = null, @SerialName("fat_100g") val fat: Double? = null, @SerialName("fiber_100g") val fiber: Double? = null, @SerialName("sugar_100g") val sugar: Double? = null, @SerialName("sodium_mg_100g") val sodium: Double? = null, @SerialName("serving_data") val servingData: JsonElement? = null, @SerialName("canonical_foods") val canonical: CanonicalWithAliasesRow) { fun nutrients() = FoodNutrients(calories, protein, carbohydrates, fat, fiber, sugar, sodium) }
@Serializable private data class CommercialProductRow(val id: String, @SerialName("name_es") val nameEs: String, val brand: String? = null, @SerialName("image_url") val imageUrl: String? = null, @SerialName("calories_100g") val calories: Double? = null, @SerialName("protein_100g") val protein: Double? = null, @SerialName("carbohydrates_100g") val carbohydrates: Double? = null, @SerialName("fat_100g") val fat: Double? = null, @SerialName("fiber_100g") val fiber: Double? = null, @SerialName("sugar_100g") val sugar: Double? = null, @SerialName("sodium_mg_100g") val sodium: Double? = null, @SerialName("serving_data") val servingData: JsonElement? = null) { fun nutrients() = FoodNutrients(calories, protein, carbohydrates, fat, fiber, sugar, sodium) }
