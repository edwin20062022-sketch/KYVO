package com.kyvo.app.feature.food.domain

import java.text.Normalizer

fun normalizeFoodQuery(value: String): String = Normalizer.normalize(value.trim().lowercase(), Normalizer.Form.NFD).replace("\\p{M}+".toRegex(), "").replace("\\s+".toRegex(), " ").removeSuffix("s")
