package com.kyvo.app.feature.home.domain

import java.time.LocalDate

fun interface DateProvider { fun today(): LocalDate }
object SystemDateProvider : DateProvider { override fun today(): LocalDate = LocalDate.now() }
