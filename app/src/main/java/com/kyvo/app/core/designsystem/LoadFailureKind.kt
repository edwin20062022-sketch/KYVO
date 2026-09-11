package com.kyvo.app.core.designsystem

import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

enum class LoadFailureKind { Offline, Generic }

fun Throwable.toLoadFailureKind(): LoadFailureKind {
    var current: Throwable? = this
    while (current != null) {
        if (current is UnknownHostException || current is ConnectException || current is IOException) return LoadFailureKind.Offline
        current = current.cause
    }
    return LoadFailureKind.Generic
}
