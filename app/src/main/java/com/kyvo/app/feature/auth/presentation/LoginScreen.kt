package com.kyvo.app.feature.auth.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.R
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.core.designsystem.component.KyvoBrandLockup
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.core.designsystem.component.KyvoSecondaryButton
import com.kyvo.app.core.designsystem.component.KyvoTextField
import com.kyvo.app.domain.auth.AuthRepository

@Composable
fun LoginRoute(
    authRepository: AuthRepository,
    onAuthenticated: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.factory(authRepository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onAuthenticated()
            viewModel.onEvent(LoginEvent.NavigationHandled)
        }
    }
    LoginScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuthBackground()
        when (state.mode) {
            LoginMode.Welcome -> WelcomeContent(state = state, onEvent = onEvent)
            LoginMode.Email -> EmailLoginContent(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun WelcomeContent(state: LoginUiState, onEvent: (LoginEvent) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxHeight < 720.dp || maxWidth < 360.dp
        val horizontalPadding = if (maxWidth < 360.dp) 18.dp else 24.dp
        val logoSize = if (compact) 70.dp else 92.dp
        val sectionGap = if (compact) 18.dp else 28.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = if (compact) 18.dp else 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KyvoBrandLockup(markSize = logoSize)
            Spacer(Modifier.height(sectionGap))
            Text(
                text = stringResource(R.string.login_tagline_primary),
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.login_tagline_accent),
                color = MaterialTheme.colorScheme.primary,
                fontSize = if (compact) 24.sp else 30.sp,
                lineHeight = if (compact) 28.sp else 34.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(if (compact) 10.dp else 18.dp))
            Text(
                text = stringResource(R.string.login_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(sectionGap))
            BenefitsRow(compact = compact)
            Spacer(Modifier.height(sectionGap))
            KyvoPrimaryButton(
                text = stringResource(R.string.create_account),
                enabled = !state.isLoading,
                onClick = { onEvent(LoginEvent.CreateAccount) },
                modifier = Modifier.testTag(CREATE_ACCOUNT_TAG),
            )
            Spacer(Modifier.height(12.dp))
            KyvoSecondaryButton(
                text = stringResource(R.string.sign_in),
                enabled = !state.isLoading,
                onClick = { onEvent(LoginEvent.OpenEmailLogin) },
                modifier = Modifier.testTag(OPEN_EMAIL_LOGIN_TAG),
            )
            Spacer(Modifier.height(12.dp))
            GoogleButton(
                isLoading = state.isLoading,
                onClick = { onEvent(LoginEvent.SubmitGoogle) },
            )
            state.message?.let {
                Spacer(Modifier.height(12.dp))
                LoginMessageBanner(message = it, onDismiss = { onEvent(LoginEvent.DismissMessage) })
            }
            Spacer(Modifier.height(if (compact) 18.dp else 24.dp))
            LegalText()
        }
    }
}

@Composable
private fun EmailLoginContent(state: LoginUiState, onEvent: (LoginEvent) -> Unit) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordFocusRequester = remember { FocusRequester() }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = if (maxWidth < 360.dp) 18.dp else 24.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onEvent(LoginEvent.BackToWelcome) },
                    enabled = !state.isLoading,
                    modifier = Modifier.size(KyvoTheme.spacing.minimumTouchTarget),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Spacer(Modifier.width(8.dp))
                KyvoBrandLockup(horizontal = true, markSize = 42.dp)
            }
            Spacer(Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.email_login_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.email_login_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                val emailError = state.emailError?.message()
                KyvoTextField(
                    value = state.email,
                    onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                    label = stringResource(R.string.email_label),
                    enabled = !state.isLoading,
                    isError = emailError != null,
                    supportingText = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EMAIL_FIELD_TAG)
                        .fieldErrorSemantics(emailError),
                )
                val passwordError = state.passwordError?.message()
                KyvoTextField(
                    value = state.password,
                    onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                    label = stringResource(R.string.password_label),
                    enabled = !state.isLoading,
                    isError = passwordError != null,
                    supportingText = passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onEvent(LoginEvent.SubmitEmail)
                        },
                    ),
                    visualTransformation = if (state.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        val actionDescription = stringResource(
                            if (state.isPasswordVisible) R.string.hide_password else R.string.show_password,
                        )
                        TextButton(
                            onClick = { onEvent(LoginEvent.TogglePasswordVisibility) },
                            enabled = !state.isLoading,
                            modifier = Modifier.semantics { contentDescription = actionDescription },
                        ) {
                            Text(
                                text = stringResource(
                                    if (state.isPasswordVisible) R.string.password_toggle_hide
                                    else R.string.password_toggle_show,
                                ),
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester)
                        .testTag(PASSWORD_FIELD_TAG)
                        .fieldErrorSemantics(passwordError),
                )
                state.message?.let {
                    LoginMessageBanner(message = it, onDismiss = { onEvent(LoginEvent.DismissMessage) })
                }
                KyvoPrimaryButton(
                    text = stringResource(R.string.sign_in),
                    isLoading = state.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onEvent(LoginEvent.SubmitEmail)
                    },
                    modifier = Modifier.testTag(SUBMIT_LOGIN_TAG),
                )
                TextButton(
                    onClick = { onEvent(LoginEvent.BackToWelcome) },
                    enabled = !state.isLoading,
                    modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.back))
                }
            }
        }
    }
}

@Composable
private fun BenefitsRow(compact: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 12.dp),
    ) {
        BenefitItem(
            type = BenefitType.Goal,
            title = stringResource(R.string.login_benefit_goals_title),
            body = stringResource(R.string.login_benefit_goals_body),
            compact = compact,
            modifier = Modifier.weight(1f),
        )
        BenefitItem(
            type = BenefitType.Tracking,
            title = stringResource(R.string.login_benefit_tracking_title),
            body = stringResource(R.string.login_benefit_tracking_body),
            compact = compact,
            modifier = Modifier.weight(1f),
        )
        BenefitItem(
            type = BenefitType.Share,
            title = stringResource(R.string.login_benefit_share_title),
            body = stringResource(R.string.login_benefit_share_body),
            compact = compact,
            modifier = Modifier.weight(1f),
        )
    }
}

private enum class BenefitType { Goal, Tracking, Share }

@Composable
private fun BenefitItem(
    type: BenefitType,
    title: String,
    body: String,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(if (compact) 48.dp else 56.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            shape = RoundedCornerShape(16.dp),
        ) {
            BenefitIcon(type = type, modifier = Modifier.padding(13.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = if (compact) 10.sp else 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = body,
            fontSize = if (compact) 10.sp else 11.sp,
            lineHeight = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun BenefitIcon(type: BenefitType, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.fillMaxSize()) {
        val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
        when (type) {
            BenefitType.Goal -> {
                drawCircle(color = color, radius = size.minDimension * .36f, style = stroke)
                drawCircle(color = color, radius = size.minDimension * .10f)
            }
            BenefitType.Tracking -> {
                drawArc(color, -90f, 275f, false, style = stroke)
                drawLine(color, center, Offset(center.x, size.height * .16f), stroke.width, StrokeCap.Round)
                drawLine(color, center, Offset(size.width * .78f, center.y), stroke.width, StrokeCap.Round)
            }
            BenefitType.Share -> {
                drawRect(color = color, topLeft = Offset(size.width * .18f, size.height * .38f), size = androidx.compose.ui.geometry.Size(size.width * .64f, size.height * .50f), style = stroke)
                drawLine(color, Offset(center.x, size.height * .62f), Offset(center.x, size.height * .08f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(center.x, size.height * .08f), Offset(size.width * .32f, size.height * .28f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(center.x, size.height * .08f), Offset(size.width * .68f, size.height * .28f), stroke.width, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun GoogleButton(isLoading: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(GOOGLE_LOGIN_TAG),
        shape = MaterialTheme.shapes.medium,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_google_g),
                contentDescription = stringResource(R.string.google_logo_description),
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(stringResource(R.string.continue_with_google), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun LegalText() {
    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary
    Text(
        text = buildAnnotatedString {
            append("Al continuar, aceptas nuestros ")
            withStyle(SpanStyle(color = accentColor)) { append("Términos de servicio") }
            append(" y ")
            withStyle(SpanStyle(color = accentColor)) { append("Política de privacidad") }
            append(".")
        },
        style = MaterialTheme.typography.bodyMedium,
        color = baseColor,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LoginMessageBanner(message: LoginMessage, onDismiss: () -> Unit) {
    val text = message.message()
    val dismissDescription = stringResource(R.string.dismiss_message)
    Surface(
        modifier = Modifier.fillMaxWidth().semantics { error(text) }.testTag(AUTH_MESSAGE_TAG),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { contentDescription = dismissDescription },
            ) {
                Text("×", fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun AuthBackground() {
    val purple = KyvoColors.PurplePrimary
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = purple.copy(alpha = .045f),
            radius = size.width * .72f,
            center = Offset(size.width * .85f, size.height * .08f),
        )
        val watermark = purple.copy(alpha = .035f)
        val width = size.width * .12f
        drawLine(watermark, Offset(size.width * .24f, size.height * .03f), Offset(size.width * .08f, size.height * .44f), width, StrokeCap.Square)
        drawLine(watermark, Offset(size.width * .10f, size.height * .42f), Offset(size.width * .80f, size.height * .02f), width, StrokeCap.Square)
        drawLine(watermark, Offset(size.width * .45f, size.height * .25f), Offset(size.width * .92f, size.height * .48f), width, StrokeCap.Square)
    }
}

@Composable
private fun EmailValidationError.message(): String = stringResource(
    when (this) {
        EmailValidationError.Empty -> R.string.email_empty_error
        EmailValidationError.Invalid -> R.string.email_invalid_error
    },
)

@Composable
private fun PasswordValidationError.message(): String = stringResource(
    when (this) {
        PasswordValidationError.Empty -> R.string.password_empty_error
        PasswordValidationError.TooShort -> R.string.password_short_error
    },
)

@Composable
private fun LoginMessage.message(): String = stringResource(
    when (this) {
        LoginMessage.InvalidCredentials -> R.string.invalid_credentials_error
        LoginMessage.Network -> R.string.network_error
        LoginMessage.ConfigurationRequired -> R.string.configuration_required_error
        LoginMessage.Unknown -> R.string.unknown_auth_error
        LoginMessage.AccountCreationUnavailable -> R.string.account_creation_unavailable
    },
)

private fun Modifier.fieldErrorSemantics(message: String?): Modifier = if (message == null) this else semantics { error(message) }

const val CREATE_ACCOUNT_TAG = "create_account"
const val OPEN_EMAIL_LOGIN_TAG = "open_email_login"
const val GOOGLE_LOGIN_TAG = "google_login"
const val EMAIL_FIELD_TAG = "email_field"
const val PASSWORD_FIELD_TAG = "password_field"
const val SUBMIT_LOGIN_TAG = "submit_login"
const val AUTH_MESSAGE_TAG = "auth_message"
