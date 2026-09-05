package com.kyvo.app.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kyvo.app.R
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.core.designsystem.component.KyvoBrandLockup

@Composable
fun OnboardingPendingScreen() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(KyvoTheme.spacing.xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KyvoBrandLockup(markSize = KyvoTheme.spacing.xxxl)
            Text(
                text = stringResource(R.string.onboarding_pending_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = KyvoTheme.spacing.xl),
            )
            Text(
                text = stringResource(R.string.onboarding_pending_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = KyvoTheme.spacing.sm),
            )
        }
    }
}

