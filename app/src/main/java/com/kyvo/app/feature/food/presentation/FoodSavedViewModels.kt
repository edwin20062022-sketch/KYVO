package com.kyvo.app.feature.food.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.core.designsystem.LoadFailureKind
import com.kyvo.app.core.designsystem.toLoadFailureKind
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SavedFoodsUiState {
    data object Loading : SavedFoodsUiState
    data class Content(val items: List<FoodSearchResult>, val message: String? = null) : SavedFoodsUiState
    data object Empty : SavedFoodsUiState
    data class Error(val message: String, val kind: LoadFailureKind = LoadFailureKind.Generic) : SavedFoodsUiState
}

class FavoriteFoodsViewModel(private val repository: FoodRepository, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val _state = MutableStateFlow<SavedFoodsUiState>(SavedFoodsUiState.Loading)
    val state: StateFlow<SavedFoodsUiState> = _state.asStateFlow()
    init { load() }
    fun retry() = load()
    private fun load() { viewModelScope.launch(dispatcher) { _state.value = SavedFoodsUiState.Loading; runCatching { repository.getFavoriteFoods() }.onSuccess { foods -> _state.value = if (foods.isEmpty()) SavedFoodsUiState.Empty else SavedFoodsUiState.Content(foods) }.onFailure { _state.value = SavedFoodsUiState.Error(it.message ?: "No pudimos cargar tus favoritos.", it.toLoadFailureKind()) } } }
    fun remove(food: FoodSearchResult) {
        val previous = _state.value
        val content = previous as? SavedFoodsUiState.Content ?: return
        _state.value = content.copy(items = content.items.filterNot { it.id == food.id && it.type == food.type })
        if ((_state.value as SavedFoodsUiState.Content).items.isEmpty()) _state.value = SavedFoodsUiState.Empty
        viewModelScope.launch(dispatcher) {
            runCatching { repository.setFavorite(food.id, food.type, false) }.onFailure {
                _state.value = previous
                _state.value = SavedFoodsUiState.Content((_state.value as SavedFoodsUiState.Content).items, it.message ?: "No pudimos quitar el favorito.")
            }
        }
    }
    companion object { fun factory(repository: FoodRepository) = object : ViewModelProvider.Factory { @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = FavoriteFoodsViewModel(repository) as T } }
}

class FrequentFoodsViewModel(private val repository: FoodRepository, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val _state = MutableStateFlow<SavedFoodsUiState>(SavedFoodsUiState.Loading)
    val state: StateFlow<SavedFoodsUiState> = _state.asStateFlow()
    init { load() }
    fun retry() { load() }
    private fun load() { viewModelScope.launch(dispatcher) { _state.value = SavedFoodsUiState.Loading; runCatching { repository.getFrequentFoods() }.onSuccess { foods -> _state.value = if (foods.isEmpty()) SavedFoodsUiState.Empty else SavedFoodsUiState.Content(foods) }.onFailure { _state.value = SavedFoodsUiState.Error(it.message ?: "No pudimos cargar tus frecuentes.", it.toLoadFailureKind()) } } }
    companion object { fun factory(repository: FoodRepository) = object : ViewModelProvider.Factory { @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = FrequentFoodsViewModel(repository) as T } }
}

data class FoodFavoriteUiState(val isFavorite: Boolean = false, val isLoading: Boolean = true, val error: String? = null)

class FoodFavoriteViewModel(private val repository: FoodRepository, private val foodId: String, private val type: FoodType, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val _state = MutableStateFlow(FoodFavoriteUiState())
    val state: StateFlow<FoodFavoriteUiState> = _state.asStateFlow()
    init { viewModelScope.launch(dispatcher) { runCatching { repository.isFavorite(foodId, type) }.onSuccess { _state.value = FoodFavoriteUiState(it, false) }.onFailure { _state.value = FoodFavoriteUiState(false, false, it.message ?: "No pudimos consultar el favorito.") } } }
    fun toggle() {
        val old = _state.value
        if (old.isLoading) return
        val next = !old.isFavorite
        _state.value = old.copy(isFavorite = next, isLoading = true, error = null)
        viewModelScope.launch(dispatcher) { runCatching { repository.setFavorite(foodId, type, next) }.onSuccess { _state.value = FoodFavoriteUiState(next, false) }.onFailure { _state.value = old.copy(error = it.message ?: "No pudimos actualizar el favorito.") } }
    }
    companion object { fun factory(repository: FoodRepository, foodId: String, type: FoodType) = object : ViewModelProvider.Factory { @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = FoodFavoriteViewModel(repository, foodId, type) as T } }
}
