package com.notifast.app.presentation.theme.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.notifast.app.presentation.theme.color.BackgroundDark
import com.notifast.app.presentation.theme.color.BackgroundLight
import com.notifast.app.presentation.theme.color.ErrorContainerDark
import com.notifast.app.presentation.theme.color.ErrorContainerLight
import com.notifast.app.presentation.theme.color.ErrorDark
import com.notifast.app.presentation.theme.color.ErrorLight
import com.notifast.app.presentation.theme.color.OnBackgroundDark
import com.notifast.app.presentation.theme.color.OnBackgroundLight
import com.notifast.app.presentation.theme.color.OnErrorContainerDark
import com.notifast.app.presentation.theme.color.OnErrorContainerLight
import com.notifast.app.presentation.theme.color.OnErrorDark
import com.notifast.app.presentation.theme.color.OnErrorLight
import com.notifast.app.presentation.theme.color.OnPrimaryContainerDark
import com.notifast.app.presentation.theme.color.OnPrimaryContainerLight
import com.notifast.app.presentation.theme.color.OnPrimaryDark
import com.notifast.app.presentation.theme.color.OnPrimaryLight
import com.notifast.app.presentation.theme.color.OnSecondaryContainerDark
import com.notifast.app.presentation.theme.color.OnSecondaryContainerLight
import com.notifast.app.presentation.theme.color.OnSecondaryDark
import com.notifast.app.presentation.theme.color.OnSecondaryLight
import com.notifast.app.presentation.theme.color.OnSurfaceDark
import com.notifast.app.presentation.theme.color.OnSurfaceLight
import com.notifast.app.presentation.theme.color.OnSurfaceVariantDark
import com.notifast.app.presentation.theme.color.OnSurfaceVariantLight
import com.notifast.app.presentation.theme.color.OnTertiaryContainerDark
import com.notifast.app.presentation.theme.color.OnTertiaryContainerLight
import com.notifast.app.presentation.theme.color.OnTertiaryDark
import com.notifast.app.presentation.theme.color.OnTertiaryLight
import com.notifast.app.presentation.theme.color.OutlineDark
import com.notifast.app.presentation.theme.color.OutlineLight
import com.notifast.app.presentation.theme.color.OutlineVariantDark
import com.notifast.app.presentation.theme.color.OutlineVariantLight
import com.notifast.app.presentation.theme.color.PrimaryContainerDark
import com.notifast.app.presentation.theme.color.PrimaryContainerLight
import com.notifast.app.presentation.theme.color.PrimaryDark
import com.notifast.app.presentation.theme.color.PrimaryLight
import com.notifast.app.presentation.theme.color.SecondaryContainerDark
import com.notifast.app.presentation.theme.color.SecondaryContainerLight
import com.notifast.app.presentation.theme.color.SecondaryDark
import com.notifast.app.presentation.theme.color.SecondaryLight
import com.notifast.app.presentation.theme.color.SurfaceContainerDark
import com.notifast.app.presentation.theme.color.SurfaceContainerHighDark
import com.notifast.app.presentation.theme.color.SurfaceContainerHighLight
import com.notifast.app.presentation.theme.color.SurfaceContainerLight
import com.notifast.app.presentation.theme.color.SurfaceDark
import com.notifast.app.presentation.theme.color.SurfaceLight
import com.notifast.app.presentation.theme.color.SurfaceVariantDark
import com.notifast.app.presentation.theme.color.SurfaceVariantLight
import com.notifast.app.presentation.theme.color.TertiaryContainerDark
import com.notifast.app.presentation.theme.color.TertiaryContainerLight
import com.notifast.app.presentation.theme.color.TertiaryDark
import com.notifast.app.presentation.theme.color.TertiaryLight
import com.notifast.app.presentation.theme.type.Typography

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

@Composable
fun NotiFastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: com.notifast.app.domain.model.theme.ThemePreset = com.notifast.app.domain.model.theme.ThemePreset.CLASSIC,
    backgroundPreset: com.notifast.app.domain.model.theme.BackgroundPreset = com.notifast.app.domain.model.theme.BackgroundPreset.CLASSIC,
    customPrimaryHex: String? = null,
    dynamicColor: Boolean = false, // Forced false to maintain NotiFast 3.0 tactile branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themePreset == com.notifast.app.domain.model.theme.ThemePreset.CLASSIC &&
        backgroundPreset == com.notifast.app.domain.model.theme.BackgroundPreset.CLASSIC &&
        customPrimaryHex.isNullOrBlank() -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        else -> {
            com.notifast.app.presentation.theme.ThemePaletteGenerator.generateColorScheme(
                preset = themePreset,
                isDark = darkTheme,
                customPrimaryHex = customPrimaryHex,
                backgroundPreset = backgroundPreset
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
