package com.kyvo.app.feature.home.data

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.home.domain.repository.MealRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Supabase adapter. RLS derives ownership from the authenticated session. */
class SupabaseMealRepository(private val client: SupabaseClient) : MealRepository {
    private val cache = MutableStateFlow<Map<LocalDate, List<Meal>>>(emptyMap())
    private val loadedDates = mutableSetOf<LocalDate>()
    override fun observeMealsForDate(date: LocalDate): Flow<List<Meal>> = flow {
        if (loadedDates.add(date)) cache.update { it + (date to fetch(date)) }
        emitAll(cache.map { it[date].orEmpty() })
    }
    override fun observeMeal(id: String): Flow<Meal?> = cache.map { days -> days.values.flatten().firstOrNull { it.id == id } }
    override suspend fun addMeal(date: LocalDate, meal: Meal) {
        client.from("meals").insert(RemoteMealInsert(meal.id, date.toString(), meal.type.databaseValue, meal.title, meal.time))
        client.from("meal_items").insert(meal.items.map { it.toInsert(meal.id) })
        cache.update { it + (date to (it[date].orEmpty() + meal)) }
    }
    override suspend fun updateMeal(meal: Meal) {
        client.from("meals").update(RemoteMealUpdate(meal.type.databaseValue, meal.title, meal.time)) { filter { eq("id", meal.id) } }
        client.from("meal_items").delete { filter { eq("meal_id", meal.id) } }
        client.from("meal_items").insert(meal.items.map { it.toInsert(meal.id) })
        cache.update { days -> days.mapValues { (_, values) -> values.map { if (it.id == meal.id) meal else it } } }
    }
    override suspend fun deleteMeal(id: String) { client.from("meals").delete { filter { eq("id", id) } }; cache.update { days -> days.mapValues { (_, values) -> values.filterNot { it.id == id } } } }
    override suspend fun addSavedDishToDay(dishId: String, date: LocalDate, type: MealType, portions: Double): Meal {
        val mealId = client.postgrest.rpc("add_saved_dish_to_day", buildJsonObject {
            put("p_dish_id", dishId); put("p_date", date.toString()); put("p_meal_type", type.databaseValue); put("p_portions", portions)
        }).decodeSingle<String>()
        val meal = fetch(date).firstOrNull { it.id == mealId } ?: error("No pudimos recuperar la comida registrada.")
        cache.update { it + (date to (it[date].orEmpty().filterNot { existing -> existing.id == meal.id } + meal)) }
        return meal
    }
    private suspend fun fetch(date: LocalDate): List<Meal> = client.from("meals").select { filter { eq("date", date.toString()) } }.decodeList<RemoteMeal>().map(RemoteMeal::toDomain)
    private suspend fun fetchAll(): List<Meal> = client.from("meals").select().decodeList<RemoteMeal>().map(RemoteMeal::toDomain)
}

private val MealType.databaseValue get() = name.lowercase()
private fun MealItem.toInsert(mealId: String) = RemoteMealItemInsert(mealId, foodId, foodType?.databaseValue ?: "custom", name, quantity, unit, grams ?: quantity, calories.toDouble(), protein.toDouble(), carbohydrates.toDouble(), fat.toDouble(), fiber, sugar, sodiumMg)

@Serializable private data class RemoteMeal(val id: String, val date: String, val type: String, val title: String, val time: String? = null, @SerialName("meal_items") val items: List<RemoteMealItem> = emptyList())
@Serializable private data class RemoteMealItem(val id: String, @SerialName("food_reference") val foodId: String? = null, @SerialName("food_type") val foodType: String = "custom", @SerialName("name_snapshot") val name: String, val quantity: Double, val unit: String, val grams: Double? = null, @SerialName("calories_snapshot") val calories: Double, @SerialName("protein_snapshot") val protein: Double, @SerialName("carbohydrates_snapshot") val carbohydrates: Double, @SerialName("fat_snapshot") val fat: Double, @SerialName("fiber_snapshot") val fiber: Double? = null, @SerialName("sugar_snapshot") val sugar: Double? = null, @SerialName("sodium_mg_snapshot") val sodiumMg: Double? = null)
@Serializable private data class RemoteMealInsert(val id: String, val date: String, val type: String, val title: String, val time: String?)
@Serializable private data class RemoteMealUpdate(val type: String, val title: String, val time: String?)
@Serializable private data class RemoteMealItemInsert(@SerialName("meal_id") val mealId: String, @SerialName("food_reference") val foodId: String?, @SerialName("food_type") val foodType: String, @SerialName("name_snapshot") val name: String, val quantity: Double, val unit: String, val grams: Double, @SerialName("calories_snapshot") val calories: Double, @SerialName("protein_snapshot") val protein: Double, @SerialName("carbohydrates_snapshot") val carbohydrates: Double, @SerialName("fat_snapshot") val fat: Double, @SerialName("fiber_snapshot") val fiber: Double?, @SerialName("sugar_snapshot") val sugar: Double?, @SerialName("sodium_mg_snapshot") val sodium: Double?)
private fun RemoteMeal.toDomain() = Meal(id, MealType.entries.firstOrNull { it.databaseValue == type } ?: MealType.Snack, title, time, items.map { MealItem(it.id, it.name, it.quantity, it.unit, it.calories.roundToInt(), it.protein.roundToInt(), it.carbohydrates.roundToInt(), it.fat.roundToInt(), fiber = it.fiber, sugar = it.sugar, sodiumMg = it.sodiumMg, grams = it.grams, foodId = it.foodId, foodType = it.foodType.toFoodType()) }, items.sumOf { it.calories }.roundToInt(), items.sumOf { it.protein }.roundToInt(), items.sumOf { it.carbohydrates }.roundToInt(), items.sumOf { it.fat }.roundToInt())
private val FoodType.databaseValue get() = if (this == FoodType.Generic) "variant" else "commercial"
private fun String.toFoodType() = when (this) { "variant" -> FoodType.Generic; "commercial" -> FoodType.Commercial; else -> null }
private fun Double.roundToInt() = kotlin.math.round(this).toInt()
