package com.kyvo.app.feature.food.data

import com.kyvo.app.feature.food.domain.normalizeFoodQuery
import com.kyvo.app.feature.food.domain.model.FoodSearchResult

internal fun rankCatalogResults(query: String, candidates: List<Pair<FoodSearchResult, List<String>>>): List<FoodSearchResult> {
    val normalized = normalizeFoodQuery(query)
    if (normalized.isBlank()) return emptyList()
    return candidates.mapNotNull { (result, aliases) ->
        val names = listOfNotNull(result.name, result.subtitle, result.brand) + aliases
        val rank = when {
            normalizeFoodQuery(result.name) == normalized -> 0
            aliases.any { normalizeFoodQuery(it) == normalized } -> 1
            names.any { normalizeFoodQuery(it).startsWith(normalized) } -> 2
            names.any { normalizeFoodQuery(it).contains(normalized) } -> 3
            else -> return@mapNotNull null
        }
        result to rank
    }.sortedWith(compareBy<Pair<FoodSearchResult, Int>> { it.second }.thenBy { it.first.name }).map { it.first }
}
