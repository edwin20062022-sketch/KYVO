package com.kyvo.app.feature.dishes.data

import com.kyvo.app.feature.dishes.domain.model.SavedDish
import com.kyvo.app.feature.dishes.domain.model.SavedDishItem
import com.kyvo.app.feature.dishes.domain.repository.SavedDishRepository
import com.kyvo.app.feature.food.domain.model.FoodType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

class SupabaseSavedDishRepository(private val client: SupabaseClient) : SavedDishRepository {
    private val cache = MutableStateFlow<List<SavedDish>>(emptyList())
    private var loaded = false

    override fun observeSavedDishes(): Flow<List<SavedDish>> {
        refreshIfNeeded()
        return cache.asStateFlow()
    }

    override fun observeSavedDish(id: String): Flow<SavedDish?> {
        refreshIfNeeded()
        return cache.map { dishes -> dishes.firstOrNull { it.id == id } }
    }

    override suspend fun saveDish(dish: SavedDish): SavedDish = runCatching {
        val id = client.postgrest.rpc("save_saved_dish", dish.toRpcArguments()).decodeSingle<String>()
        val saved = fetchAll().firstOrNull { it.id == id } ?: error("No pudimos recuperar el platillo guardado.")
        cache.update { current -> (current.filterNot { it.id == saved.id } + saved).sortedByDescending { it.updatedAt } }
        loaded = true
        saved
    }.getOrElse { throw SavedDishRepositoryException("No pudimos guardar el platillo.", it) }

    override suspend fun deleteDish(id: String) {
        runCatching {
            client.from("saved_dishes").delete { filter { eq("id", id) } }
            cache.update { current -> current.filterNot { it.id == id } }
        }.getOrElse { throw SavedDishRepositoryException("No pudimos eliminar el platillo.", it) }
    }

    private fun refreshIfNeeded() {
        if (loaded) return
        loaded = true
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            runCatching { fetchAll() }
                .onSuccess { dishes -> cache.value = dishes }
        }
    }

    private suspend fun fetchAll(): List<SavedDish> = client.from("saved_dishes").select(
        columns = io.github.jan.supabase.postgrest.query.Columns.raw(
            "id,user_id,name,portions,created_at,updated_at,saved_dish_items(id,saved_dish_id,food_reference,food_type,name_snapshot,quantity,unit,grams,calories_snapshot,protein_snapshot,carbohydrates_snapshot,fat_snapshot,fiber_snapshot,image_reference)"
        )
    ).decodeList<RemoteSavedDish>().map(RemoteSavedDish::toDomain).sortedByDescending { it.updatedAt }
}

private fun SavedDish.toRpcArguments(): JsonObject = buildJsonObject {
    if (id.isBlank()) put("p_dish_id", JsonNull) else put("p_dish_id", id)
    put("p_name", name)
    put("p_portions", portions)
    put("p_items", items.toJson())
}

private fun List<SavedDishItem>.toJson(): JsonArray = buildJsonArray {
    forEach { item ->
        add(buildJsonObject {
            put("food_reference", item.foodReference)
            put("food_type", item.foodType.databaseValue)
            put("name_snapshot", item.nameSnapshot)
            put("quantity", item.quantity)
            put("unit", item.unit)
            put("grams", item.grams)
            put("calories_snapshot", item.calories)
            put("protein_snapshot", item.protein)
            put("carbohydrates_snapshot", item.carbohydrates)
            put("fat_snapshot", item.fat)
            if (item.fiber == null) put("fiber_snapshot", JsonNull) else put("fiber_snapshot", item.fiber)
            if (item.imageReference == null) put("image_reference", JsonNull) else put("image_reference", item.imageReference)
        })
    }
}

private val FoodType.databaseValue get() = if (this == FoodType.Generic) "variant" else "commercial"

@Serializable
private data class RemoteSavedDish(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val portions: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("saved_dish_items") val items: List<RemoteSavedDishItem> = emptyList(),
) {
    fun toDomain() = SavedDish(id, userId, name, portions, items.map(RemoteSavedDishItem::toDomain), Instant.parse(createdAt), Instant.parse(updatedAt))
}

@Serializable
private data class RemoteSavedDishItem(
    val id: String,
    @SerialName("saved_dish_id") val savedDishId: String,
    @SerialName("food_reference") val foodReference: String,
    @SerialName("food_type") val foodType: String,
    @SerialName("name_snapshot") val nameSnapshot: String,
    val quantity: Double,
    val unit: String,
    val grams: Double,
    @SerialName("calories_snapshot") val calories: Int,
    @SerialName("protein_snapshot") val protein: Int,
    @SerialName("carbohydrates_snapshot") val carbohydrates: Int,
    @SerialName("fat_snapshot") val fat: Int,
    @SerialName("fiber_snapshot") val fiber: Double? = null,
    @SerialName("image_reference") val imageReference: String? = null,
) {
    fun toDomain() = SavedDishItem(id, savedDishId, foodReference, if (foodType == "variant") FoodType.Generic else FoodType.Commercial, nameSnapshot, quantity, unit, grams, calories, protein, carbohydrates, fat, fiber, imageReference)
}

class SavedDishRepositoryException(message: String, cause: Throwable) : RuntimeException(message, cause)
