package com.kyvo.app.feature.mealshare.presentation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kyvo.app.R
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.MealShareOverlayData
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplateSpecs
import com.kyvo.app.feature.mealshare.domain.model.renderFingerprint
import com.kyvo.app.feature.mealshare.domain.model.toOverlayData
import java.io.File

internal const val MEAL_SHARE_PREVIEW_ASPECT_RATIO = MealShareTemplateSpecs.aspectRatio

@Composable
fun MealShareEditorScreen(
    draft: MealShareDraft,
    onTemplateSelected: (MealShareTemplate) -> Unit,
    renderState: MealShareRenderState,
    onRender: (MealShareDraft) -> Unit,
    onDraftChanged: (String) -> Unit,
    onRendered: (com.kyvo.app.feature.mealshare.domain.render.MealShareRenderResult) -> Unit,
    finalizationState: MealShareFinalizationState,
    onConfirmFinalization: () -> Unit,
    canFinalize: Boolean,
    onBack: () -> Unit,
) {
    val overlay = draft.toOverlayData()
    val isPersisted = finalizationState is MealShareFinalizationState.Persisted
    val isPersisting = finalizationState is MealShareFinalizationState.Persisting
    val hasCurrentRender = !draft.renderedImagePath.isNullOrBlank() && draft.renderedFingerprint == draft.renderFingerprint()
    LaunchedEffect(draft.renderFingerprint()) { onDraftChanged(draft.renderFingerprint()) }
    LaunchedEffect(renderState) {
        if (renderState is MealShareRenderState.Success) onRendered(renderState.result)
    }
    Column(Modifier.fillMaxSize()) {
        EditorHeader(onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            RenderPreview(draft, overlay, renderState)
            TemplateSelector(
                photoUri = draft.photoUri,
                selected = draft.template,
                data = overlay,
                enabled = !isPersisted,
                onSelected = onTemplateSelected,
            )
            KyvoPrimaryButton(
                text = if (renderState is MealShareRenderState.Success) "Regenerar imagen final" else "Generar imagen final",
                onClick = { onRender(draft) },
                enabled = !isPersisted && draft.isReadyToRender && renderState !is MealShareRenderState.Rendering,
                isLoading = renderState is MealShareRenderState.Rendering,
            )
            if (renderState is MealShareRenderState.Error) Text(renderState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            when (finalizationState) {
                is MealShareFinalizationState.Persisted -> Text("Comida registrada. La compartirás en el siguiente paso.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                is MealShareFinalizationState.Error -> Text(finalizationState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                else -> Unit
            }
            KyvoPrimaryButton(
                text = if (isPersisting) "Registrando comida..." else if (finalizationState is MealShareFinalizationState.Error) "Reintentar registro" else "Registrar comida",
                onClick = onConfirmFinalization,
                enabled = canFinalize && hasCurrentRender && !isPersisting && !isPersisted,
                isLoading = isPersisting,
            )
            Text("La imagen se guarda temporalmente en este dispositivo. Compartir llegará en el siguiente paso.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun RenderPreview(draft: MealShareDraft, overlay: MealShareOverlayData, renderState: MealShareRenderState) {
    val renderedFile = when (renderState) {
        is MealShareRenderState.Success -> renderState.result.file
        else -> draft.renderedImagePath
            ?.takeIf { draft.renderedFingerprint == draft.renderFingerprint() }
            ?.let(::File)
            ?.takeIf { it.isFile && it.length() > 0L }
    }
    if (renderedFile != null) {
        AsyncImage(
            model = renderedFile,
            contentDescription = "Imagen Meal Share generada",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(MEAL_SHARE_PREVIEW_ASPECT_RATIO).clip(RoundedCornerShape(26.dp)),
        )
    } else {
        MealSharePhotoOverlay(
            photoUri = draft.photoUri,
            template = draft.template,
            data = overlay,
            modifier = Modifier.fillMaxWidth().aspectRatio(MEAL_SHARE_PREVIEW_ASPECT_RATIO),
        )
    }
}

@Composable
private fun EditorHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver al constructor")
        }
        Column(Modifier.weight(1f).padding(start = 6.dp)) {
            Text("Meal Share", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Edita tu publicación", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TemplateSelector(
    photoUri: String?,
    selected: MealShareTemplate,
    data: MealShareOverlayData,
    enabled: Boolean,
    onSelected: (MealShareTemplate) -> Unit,
) {
    Column {
        Text("Elige un estilo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MealShareTemplate.entries.forEach { template ->
                TemplateOption(
                    photoUri = photoUri,
                    template = template,
                    data = data,
                    selected = template == selected,
                    enabled = enabled,
                    onClick = { onSelected(template) },
                )
            }
        }
    }
}

@Composable
private fun TemplateOption(
    photoUri: String?,
    template: MealShareTemplate,
    data: MealShareOverlayData,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(164.dp)
            .semantics { this.selected = selected }
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick),
    ) {
        MealSharePhotoOverlay(
            photoUri = photoUri,
            template = template,
            data = data,
            compact = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(shape)
                .border(if (selected) 3.dp else 1.dp, if (selected) KyvoColors.PurpleAccent else MaterialTheme.colorScheme.outline, shape),
        )
        Text(
            text = template.label(),
            color = if (selected) KyvoColors.PurpleAccent else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
internal fun MealSharePhotoOverlay(
    photoUri: String?,
    template: MealShareTemplate,
    data: MealShareOverlayData,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val shape = RoundedCornerShape(if (compact) 0.dp else 26.dp)
    BoxWithConstraints(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        val denseContent = compact || maxWidth < 520.dp
        if (photoUri.isNullOrBlank()) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        } else {
            AsyncImage(
                model = Uri.parse(photoUri),
                contentDescription = "Vista previa de la fotografía con información nutricional",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        when (template) {
            MealShareTemplate.MINIMAL -> MinimalOverlay(data, denseContent, MealShareTemplateSpecs.forTemplate(template), maxWidth, maxHeight)
            MealShareTemplate.PERFORMANCE -> PerformanceOverlay(data, denseContent, MealShareTemplateSpecs.forTemplate(template), maxWidth, maxHeight)
            MealShareTemplate.EDITORIAL -> EditorialOverlay(data, denseContent, MealShareTemplateSpecs.forTemplate(template), maxWidth, maxHeight)
        }
    }
}

@Composable
private fun BoxScope.MinimalOverlay(data: MealShareOverlayData, compact: Boolean, spec: com.kyvo.app.feature.mealshare.domain.model.MealShareTemplateSpec, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Surface(
        color = Color(0xD91A1A1F),
        shape = RoundedCornerShape(if (compact) 10.dp else 20.dp),
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = width * spec.overlayLeftFraction, y = height * spec.overlayTopFraction)
            .width(width * spec.overlayWidthFraction)
            .height(height * spec.overlayHeightFraction),
    ) {
        Column(Modifier.padding(if (compact) 8.dp else 16.dp)) {
            KyvoLogo(compact)
            Spacer(Modifier.height(if (compact) 5.dp else 10.dp))
            Text(data.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = if (compact) 1 else 2, overflow = TextOverflow.Ellipsis, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium)
            Text(data.caloriesLabel, color = Color.White, fontWeight = FontWeight.Bold, style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.headlineMedium)
            MacroLine(data, Color.White, compact)
        }
    }
}

@Composable
private fun BoxScope.PerformanceOverlay(data: MealShareOverlayData, compact: Boolean, spec: com.kyvo.app.feature.mealshare.domain.model.MealShareTemplateSpec, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Column(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = width * spec.overlayLeftFraction, y = height * spec.overlayTopFraction)
            .width(width * spec.overlayWidthFraction)
            .height(height * spec.overlayHeightFraction)
            .background(Color(0xE80B0B10))
            .padding(if (compact) 8.dp else 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(data.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(data.calories.toString(), color = KyvoColors.PurpleAccent, fontWeight = FontWeight.Bold, style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.displaySmall)
                    Text(" kcal", color = Color.White, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
            ImageMark(compact)
        }
        MacroLine(data, Color.White, compact)
    }
}

@Composable
private fun BoxScope.EditorialOverlay(data: MealShareOverlayData, compact: Boolean, spec: com.kyvo.app.feature.mealshare.domain.model.MealShareTemplateSpec, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Surface(
        color = Color(0xF7FFFFFF),
        shape = RoundedCornerShape(if (compact) 0.dp else 18.dp),
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = width * spec.overlayLeftFraction, y = height * spec.overlayTopFraction)
            .width(width * spec.overlayWidthFraction)
            .height(height * spec.overlayHeightFraction),
    ) {
        Column(Modifier.padding(if (compact) 8.dp else 18.dp)) {
            KyvoLogo(compact)
            Text(data.title, color = KyvoColors.Ink, fontWeight = FontWeight.Bold, maxLines = if (compact) 1 else 2, overflow = TextOverflow.Ellipsis, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = if (compact) 4.dp else 12.dp))
            Text(data.caloriesLabel, color = KyvoColors.Ink, fontWeight = FontWeight.Bold, style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.headlineMedium)
            MacroLine(data, KyvoColors.Ink, compact)
            if (!compact) Text(data.mealTypeLabel.uppercase(), color = KyvoColors.Slate, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun KyvoLogo(compact: Boolean) {
    androidx.compose.foundation.Image(
        painter = painterResource(R.drawable.kyvo_logo),
        contentDescription = "KYVO",
        modifier = Modifier.height(if (compact) 12.dp else 20.dp).width(if (compact) 40.dp else 68.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun ImageMark(compact: Boolean) {
    androidx.compose.foundation.Image(
        painter = painterResource(R.drawable.kyvo_mark_watermark),
        contentDescription = null,
        modifier = Modifier.size(if (compact) 22.dp else 46.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun MacroLine(data: MealShareOverlayData, color: Color, compact: Boolean) {
    Row(Modifier.fillMaxWidth().padding(top = if (compact) 4.dp else 10.dp)) {
        MacroText(data.proteinLabel, color, compact, Modifier.weight(1f))
        MacroText(data.carbohydratesLabel, color, compact, Modifier.weight(1f))
        MacroText(data.fatLabel, color, compact, Modifier.weight(1f))
    }
}

@Composable
private fun MacroText(value: String, color: Color, compact: Boolean, modifier: Modifier) {
    Text(value, color = color, fontWeight = FontWeight.SemiBold, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

private fun MealShareTemplate.label() = when (this) {
    MealShareTemplate.MINIMAL -> "Minimal"
    MealShareTemplate.PERFORMANCE -> "Performance"
    MealShareTemplate.EDITORIAL -> "Editorial"
}
