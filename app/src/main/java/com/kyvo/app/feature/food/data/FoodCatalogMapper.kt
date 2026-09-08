package com.kyvo.app.feature.food.data

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodServing
import com.kyvo.app.feature.food.domain.model.FoodType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

internal data class CatalogGenericRow(val variantId: String, val canonicalId: String, val name: String, val variant: String?, val imageKey: String?, val nutrients: FoodNutrients, val servings: List<FoodServing>, val aliases: List<String>)
internal data class CatalogCommercialRow(val id: String, val name: String, val brand: String?, val imageUrl: String?, val nutrients: FoodNutrients, val servings: List<FoodServing>)

internal fun CatalogGenericRow.searchResult() = FoodSearchResult(variantId, FoodType.Generic, name, variant, null, imageKey, nutrients, servings.firstOrNull()?.label)
internal fun CatalogCommercialRow.searchResult() = FoodSearchResult(id, FoodType.Commercial, name, null, brand, null, nutrients, servings.firstOrNull()?.label, imageUrl)
internal fun CatalogGenericRow.detail() = FoodDetail(variantId, FoodType.Generic, name, variant, imageKey, nutrients, servings)
internal fun CatalogCommercialRow.detail() = FoodDetail(id, FoodType.Commercial, name, brand, null, nutrients, servings, imageUrl)

internal fun servingsFromJson(value: JsonElement?): List<FoodServing> = (value as? JsonArray).orEmpty().mapNotNull { item ->
    val objectValue = item as? JsonObject ?: return@mapNotNull null
    val label = objectValue["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
    val grams = objectValue["gramEquivalent"]?.jsonPrimitive?.doubleOrNull ?: objectValue["gram_equivalent"]?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null
    FoodServing(label, grams, objectValue["source"]?.jsonPrimitive?.contentOrNull.orEmpty())
}
