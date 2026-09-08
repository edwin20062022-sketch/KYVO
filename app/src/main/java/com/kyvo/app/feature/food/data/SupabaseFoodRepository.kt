package com.kyvo.app.feature.food.data

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
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

    private suspend fun genericRows(): List<CatalogGenericRow> = client.from("food_variants").select(columns = Columns.raw("id, name_original, variant_name_es, calories_100g, protein_100g, carbohydrates_100g, fat_100g, fiber_100g, sugar_100g, sodium_mg_100g, serving_data, canonical_foods(id, name_es, image_key, food_aliases(alias))")).decodeList<FoodVariantRow>().map { row ->
        val canonical = row.canonical
        CatalogGenericRow(row.id, canonical.id, canonical.nameEs, row.variantNameEs ?: row.nameOriginal, canonical.imageKey, row.nutrients(), servingsFromJson(row.servingData), canonical.aliases.map(FoodAliasRow::alias))
    }
    private suspend fun commercialRows(): List<CatalogCommercialRow> = client.from("commercial_products").select().decodeList<CommercialProductRow>().map { CatalogCommercialRow(it.id, it.nameEs, it.brand, it.imageUrl, it.nutrients(), servingsFromJson(it.servingData)) }
}

class FoodRepositoryException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Serializable private data class CanonicalFoodRow(val id: String, @SerialName("name_es") val nameEs: String, @SerialName("image_key") val imageKey: String? = null)
@Serializable private data class CanonicalWithAliasesRow(val id: String, @SerialName("name_es") val nameEs: String, @SerialName("image_key") val imageKey: String? = null, @SerialName("food_aliases") val aliases: List<FoodAliasRow> = emptyList())
@Serializable private data class FoodAliasRow(val alias: String)
@Serializable private data class FoodVariantRow(val id: String, @SerialName("name_original") val nameOriginal: String, @SerialName("variant_name_es") val variantNameEs: String? = null, @SerialName("calories_100g") val calories: Double? = null, @SerialName("protein_100g") val protein: Double? = null, @SerialName("carbohydrates_100g") val carbohydrates: Double? = null, @SerialName("fat_100g") val fat: Double? = null, @SerialName("fiber_100g") val fiber: Double? = null, @SerialName("sugar_100g") val sugar: Double? = null, @SerialName("sodium_mg_100g") val sodium: Double? = null, @SerialName("serving_data") val servingData: JsonElement? = null, @SerialName("canonical_foods") val canonical: CanonicalWithAliasesRow) { fun nutrients() = FoodNutrients(calories, protein, carbohydrates, fat, fiber, sugar, sodium) }
@Serializable private data class CommercialProductRow(val id: String, @SerialName("name_es") val nameEs: String, val brand: String? = null, @SerialName("image_url") val imageUrl: String? = null, @SerialName("calories_100g") val calories: Double? = null, @SerialName("protein_100g") val protein: Double? = null, @SerialName("carbohydrates_100g") val carbohydrates: Double? = null, @SerialName("fat_100g") val fat: Double? = null, @SerialName("fiber_100g") val fiber: Double? = null, @SerialName("sugar_100g") val sugar: Double? = null, @SerialName("sodium_mg_100g") val sodium: Double? = null, @SerialName("serving_data") val servingData: JsonElement? = null) { fun nutrients() = FoodNutrients(calories, protein, carbohydrates, fat, fiber, sugar, sodium) }
