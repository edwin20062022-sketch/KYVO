package com.kyvo.app.feature.food.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FoodSearchUiState {
    data object Idle : FoodSearchUiState
    data object Loading : FoodSearchUiState
    data class Results(val query: String, val items: List<FoodSearchResult>) : FoodSearchUiState
    data class Empty(val query: String) : FoodSearchUiState
    data class Error(val query: String, val message: String) : FoodSearchUiState
}

sealed interface FoodDetailUiState {
    data object Loading : FoodDetailUiState
    data class Content(val food: FoodDetail) : FoodDetailUiState
    data object NotFound : FoodDetailUiState
    data class Error(val message: String) : FoodDetailUiState
}

class FoodSearchViewModel(
    private val repository: FoodRepository,
    initialQuery: String = "",
) : ViewModel() {
    private val _query = MutableStateFlow(initialQuery)
    val query: StateFlow<String> = _query.asStateFlow()
    private val _state = MutableStateFlow<FoodSearchUiState>(FoodSearchUiState.Idle)
    val state: StateFlow<FoodSearchUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init { if (initialQuery.trim().length >= MIN_QUERY_LENGTH) search(initialQuery) }

    fun onQueryChanged(value: String) {
        _query.value = value
        searchJob?.cancel()
        if (value.trim().length < MIN_QUERY_LENGTH) {
            _state.value = FoodSearchUiState.Idle
            return
        }
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(DEBOUNCE_MS)
            executeSearch(value)
        }
    }

    fun searchNow() {
        searchJob?.cancel()
        if (_query.value.trim().length >= MIN_QUERY_LENGTH) search(_query.value)
    }

    fun retry() = searchNow()

    private fun search(rawQuery: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) { executeSearch(rawQuery) }
    }

    private suspend fun executeSearch(rawQuery: String) {
        val normalized = rawQuery.trim()
        if (normalized.length < MIN_QUERY_LENGTH) return
        _state.value = FoodSearchUiState.Loading
        runCatching { repository.searchFoods(normalized) }
            .onSuccess { results ->
                _state.value = if (results.isEmpty()) FoodSearchUiState.Empty(normalized) else FoodSearchUiState.Results(normalized, results)
            }
            .onFailure { error ->
                _state.value = FoodSearchUiState.Error(normalized, error.message ?: "No pudimos cargar los alimentos.")
            }
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
        private const val DEBOUNCE_MS = 400L
        fun factory(repository: FoodRepository, initialQuery: String = "") = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = FoodSearchViewModel(repository, initialQuery) as T
        }
    }
}

class FoodDetailViewModel(
    private val repository: FoodRepository,
    private val foodId: String,
    private val type: String,
) : ViewModel() {
    private val _state = MutableStateFlow<FoodDetailUiState>(FoodDetailUiState.Loading)
    val state: StateFlow<FoodDetailUiState> = _state.asStateFlow()

    init { load() }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = FoodDetailUiState.Loading
            runCatching { repository.getFood(foodId) }
                .onSuccess { food -> _state.value = food?.let(FoodDetailUiState::Content) ?: FoodDetailUiState.NotFound }
                .onFailure { error -> _state.value = FoodDetailUiState.Error(error.message ?: "No pudimos cargar este alimento.") }
        }
    }

    companion object {
        fun factory(repository: FoodRepository, foodId: String, type: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = FoodDetailViewModel(repository, foodId, type) as T
        }
    }
}
