package com.kyvo.app.feature.onboarding.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.feature.onboarding.domain.calculator.NutritionPlanCalculator
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.OnboardingAnswers
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity

@Preview(name = "Onboarding 320x640", widthDp = 320, heightDp = 640, showBackground = true)
@Composable private fun CompactPreview() = PreviewContent(OnboardingUiState(isRestoring = false))

@Preview(name = "Onboarding 360x800", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun MediumPreview() = PreviewContent(OnboardingUiState(isRestoring = false))

@Preview(name = "Onboarding 411x891", widthDp = 411, heightDp = 891, showBackground = true)
@Composable private fun StandardPreview() = PreviewContent(OnboardingUiState(isRestoring = false))

@Preview(name = "Onboarding 480x960", widthDp = 480, heightDp = 960, showBackground = true)
@Composable private fun LargePreview() = PreviewContent(OnboardingUiState(isRestoring = false))

@Preview(name = "Onboarding dark", widthDp = 411, heightDp = 891, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable private fun DarkPreview() {
    KyvoTheme(darkTheme = true) { OnboardingScreen(OnboardingUiState(isRestoring = false), onEvent = {}) }
}

@Preview(name = "Macro reveal", widthDp = 411, heightDp = 891, showBackground = true)
@Composable private fun MacroPreview() {
    val answers = sampleAnswers()
    PreviewContent(
        OnboardingUiState(
            currentStep = OnboardingStep.MacroReveal,
            plan = NutritionPlanCalculator().calculate(answers),
            isRestoring = false,
        ),
    )
}

@Composable private fun PreviewContent(state: OnboardingUiState) {
    KyvoTheme(darkTheme = false) { OnboardingScreen(state, onEvent = {}) }
}

private fun sampleAnswers() = OnboardingAnswers(
    GenderOption.Male, 30, 175.0, 75.0, 5, TrainingType.Strength,
    WorkActivity.Sedentary, FitnessGoal.FatLoss, ExperienceLevel.Intermediate,
    FoodPreference.None, 4,
)
