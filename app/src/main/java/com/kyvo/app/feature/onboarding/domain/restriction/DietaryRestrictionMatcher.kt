package com.kyvo.app.feature.onboarding.domain.restriction

object DietaryRestrictionMatcher {

    private const val LIKELY_SAME_THRESHOLD = 0.55

    fun isLikelySameRestriction(a: String, b: String): Boolean {
        val normA = DietaryRestrictionNormalizer.normalize(a)
        val normB = DietaryRestrictionNormalizer.normalize(b)
        if (normA == normB) return true
        if (normA.isEmpty() || normB.isEmpty()) return false
        val similarity = similarity(normA, normB)
        return similarity >= LIKELY_SAME_THRESHOLD
    }

    fun similarity(a: String, b: String): Double {
        val normA = DietaryRestrictionNormalizer.normalize(a)
        val normB = DietaryRestrictionNormalizer.normalize(b)
        if (normA == normB) return 1.0
        if (normA.isEmpty() || normB.isEmpty()) return 0.0
        val tokensA = normA.split(" ").toSet()
        val tokensB = normB.split(" ").toSet()
        val intersection = tokensA.intersect(tokensB).size.toDouble()
        val union = tokensA.union(tokensB).size.toDouble()
        val jaccard = if (union > 0) intersection / union else 0.0
        val levenshtein = levenshteinSimilarity(normA, normB)
        return (jaccard * 0.6) + (levenshtein * 0.4)
    }

    private fun levenshteinSimilarity(a: String, b: String): Double {
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return 1.0
        val distance = levenshteinDistance(a, b)
        return 1.0 - (distance.toDouble() / maxLen)
    }

    internal fun levenshteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost,
                )
            }
        }
        return dp[a.length][b.length]
    }
}
