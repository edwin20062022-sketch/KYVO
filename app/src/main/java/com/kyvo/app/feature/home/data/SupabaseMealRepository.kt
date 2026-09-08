package com.kyvo.app.feature.home.data

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.home.domain.repository.MealRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Supabase adapter. RLS derives ownership from the authenticated session. */
class SupabaseMealRepository(private val client: SupabaseClient) : MealRepository {
    override fun observeMealsForDate(date: LocalDate): Flow<List<Meal>> = flow { emit(fetch(date)) }
    override fun observeMeal(id: String): Flow<Meal?> = flow { emit(fetchAll().firstOrNull { it.id == id }) }
    override suspend fun addMeal(date: LocalDate, meal: Meal) {
        client.from("meals").insert(RemoteMealInsert(meal.id, date.toString(), meal.type.databaseValue, meal.title, meal.time))
        client.from("meal_items").insert(meal.items.map { it.toInsert(meal.id) })
    }
    override suspend fun updateMeal(meal: Meal) { client.from("meals").update(RemoteMealUpdate(meal.type.databaseValue, meal.title, meal.time)) { filter { eq("id", meal.id) } } }
    override suspend fun deleteMeal(id: String) { client.from("meals").delete { filter { eq("id", id) } } }
    private suspend fun fetch(date: LocalDate): List<Meal> = client.from("meals").select { filter { eq("date", date.toString()) } }.decodeList<RemoteMeal>().map(RemoteMeal::toDomain)
    private suspend fun fetchAll(): List<Meal> = client.from("meals").select().decodeList<RemoteMeal>().map(RemoteMeal::toDomain)
}

private val MealType.databaseValue get() = name.lowercase()
private fun MealItem.toInsert(mealId: String) = RemoteMealItemInsert(mealId, "custom", name, quantity, unit, quantity, calories.toDouble(), protein.toDouble(), carbohydrates.toDouble(), fat.toDouble(), null, null, null)

@Serializable private data class RemoteMeal(val id: String, val date: String, val type: String, val title: String, val time: String? = null, @SerialName("meal_items") val items: List<RemoteMealItem> = emptyList())
@Serializable private data class RemoteMealItem(val id: String, @SerialName("name_snapshot") val name: String, val quantity: Double, val unit: String, @SerialName("calories_snapshot") val calories: Double, @SerialName("protein_snapshot") val protein: Double, @SerialName("carbohydrates_snapshot") val carbohydrates: Double, @SerialName("fat_snapshot") val fat: Double)
@Serializable private data class RemoteMealInsert(val id: String, val date: String, val type: String, val title: String, val time: String?)
@Serializable private data class RemoteMealUpdate(val type: String, val title: String, val time: String?)
@Serializable private data class RemoteMealItemInsert(@SerialName("meal_id") val mealId: String, @SerialName("food_type") val foodType: String, @SerialName("name_snapshot") val name: String, val quantity: Double, val unit: String, val grams: Double, @SerialName("calories_snapshot") val calories: Double, @SerialName("protein_snapshot") val protein: Double, @SerialName("carbohydrates_snapshot") val carbohydrates: Double, @SerialName("fat_snapshot") val fat: Double, @SerialName("fiber_snapshot") val fiber: Double?, @SerialName("sugar_snapshot") val sugar: Double?, @SerialName("sodium_mg_snapshot") val sodium: Double?)
private fun RemoteMeal.toDomain() = Meal(id, MealType.entries.firstOrNull { it.databaseValue == type } ?: MealType.Snack, title, time, items.map { MealItem(it.id, it.name, it.quantity, it.unit, it.calories.roundToInt(), it.protein.roundToInt(), it.carbohydrates.roundToInt(), it.fat.roundToInt()) }, items.sumOf { it.calories }.roundToInt(), items.sumOf { it.protein }.roundToInt(), items.sumOf { it.carbohydrates }.roundToInt(), items.sumOf { it.fat }.roundToInt())
private fun Double.roundToInt() = kotlin.math.round(this).toInt()
