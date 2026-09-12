package com.kyvo.app.feature.onboarding.domain.restriction

import java.text.Normalizer
import java.util.Locale

object DietaryRestrictionNormalizer {

    fun normalize(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        val noAccents = removeAccents(trimmed)
        val lowercased = noAccents.lowercase(Locale.ROOT)
        val collapsed = lowercased.replace(Regex("\\s+"), " ")
        val noPunctuation = collapsed.replace(Regex("[^\\w\\s]"), "")
        return noPunctuation.replace(Regex("\\s+"), " ").trim()
    }

    private fun removeAccents(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}"), "")
    }
}
