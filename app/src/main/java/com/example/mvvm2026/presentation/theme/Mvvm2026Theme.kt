package com.example.mvvm2026.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.example.mvvm2026.R

/**
 * Applies copper accents and a sand background to Compose; XML uses blue accents and a blue-gray
 * background. Renderer colors belong to presentation resources, separate from shared ViewModel state
 * and domain rules. Status colors have the same meaning in both views. DayNight resources select
 * light or dark values without changing the feature behavior.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
@Composable
fun Mvvm2026Theme(content: @Composable () -> Unit) {
    val defaults = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = defaults.copy(
        primary = colorResource(R.color.compose_primary),
        onPrimary = colorResource(R.color.compose_on_primary),
        primaryContainer = colorResource(R.color.compose_secondary_container),
        onPrimaryContainer = colorResource(R.color.compose_on_secondary_container),
        secondary = colorResource(R.color.compose_primary),
        onSecondary = colorResource(R.color.compose_on_primary),
        secondaryContainer = colorResource(R.color.compose_secondary_container),
        onSecondaryContainer = colorResource(R.color.compose_on_secondary_container),
        error = colorResource(R.color.status_error),
        onError = colorResource(R.color.on_status_error),
        errorContainer = colorResource(R.color.error_container),
        onErrorContainer = colorResource(R.color.on_error_container),
        background = colorResource(R.color.compose_background),
        surface = colorResource(R.color.surface_card),
        surfaceTint = colorResource(R.color.compose_primary),
        surfaceContainer = colorResource(R.color.surface_low),
        surfaceContainerLowest = colorResource(R.color.surface_card),
        surfaceContainerLow = colorResource(R.color.surface_low),
        surfaceContainerHigh = colorResource(R.color.surface_low),
        surfaceContainerHighest = colorResource(R.color.surface_low),
        onSurface = colorResource(R.color.surface_text),
        onSurfaceVariant = colorResource(R.color.surface_muted),
        outline = colorResource(R.color.surface_outline),
        outlineVariant = colorResource(R.color.surface_outline_variant)
    ), content = content)
}
