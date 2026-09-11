package com.kyvo.app.feature.food

import com.kyvo.app.core.designsystem.LoadFailureKind
import com.kyvo.app.core.designsystem.toLoadFailureKind
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadFailureKindTest {
    @Test fun networkFailureIsOffline() {
        assertEquals(LoadFailureKind.Offline, RuntimeException(UnknownHostException()).toLoadFailureKind())
    }

    @Test fun genericFailureRemainsGeneric() {
        assertEquals(LoadFailureKind.Generic, IllegalStateException().toLoadFailureKind())
    }
}
