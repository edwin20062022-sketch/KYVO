package com.kyvo.app.feature.food

import com.kyvo.app.feature.food.domain.FoodRegistrationFactory
import com.kyvo.app.feature.food.domain.validateFoodAmount
import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.home.data.InMemoryMealRepository
import com.kyvo.app.feature.home.domain.model.MealType
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FoodRegistrationTest {
    private val food = FoodDetail(
        id = "food-1", type = FoodType.Generic, name = "Pollo", variant = "Pechuga",
        imageKey = null, nutrients = FoodNutrients(165.0, 31.0, 0.0, 3.6, 0.0, 0.0, 74.0), servings = emptyList(), imageUrl = "https://example.com/chicken.jpg",
    )

    @Test fun `validates decimal comma and rejects invalid amounts`() {
        assertEquals(150.5, validateFoodAmount("150,5").grams!!, 0.001)
        assertNotNull(validateFoodAmount("0").error)
        assertNotNull(validateFoodAmount("abc").error)
        assertNotNull(validateFoodAmount("10001").error)
    }

    @Test fun `creates a meal with nutrition snapshot and image`() {
        val meal = FoodRegistrationFactory.createMeal(food, 150.0, MealType.Dinner)
        val item = meal.items.single()
        assertEquals(248, item.calories)
        assertEquals(47, item.protein)
        assertEquals(150.0, item.grams!!, 0.001)
        assertEquals(food.imageUrl, item.image)
    }

    @Test fun `new meal is immediately observable by Home repository`() = runTest {
        val repository = InMemoryMealRepository()
        val meal = FoodRegistrationFactory.createMeal(food, 100.0, MealType.Lunch)
        repository.addMeal(LocalDate.of(2026, 9, 8), meal)
        assertEquals(meal.id, repository.observeMealsForDate(LocalDate.of(2026, 9, 8)).first().single().id)
    }
}
