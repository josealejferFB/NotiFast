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
import com.notifast.app.presentation.theme.color.AquaContainerDark
import com.notifast.app.presentation.theme.color.AquaContainerLight
import com.notifast.app.presentation.theme.color.AquaDark
import com.notifast.app.presentation.theme.color.AquaLight
import com.notifast.app.presentation.theme.color.BackgroundDark
import com.notifast.app.presentation.theme.color.BackgroundLight
import com.notifast.app.presentation.theme.color.ErrorContainerDark
import com.notifast.app.presentation.theme.color.ErrorContainerLight
import com.notifast.app.presentation.theme.color.ErrorDark
import com.notifast.app.presentation.theme.color.ErrorLight
import com.notifast.app.presentation.theme.color.OnAquaContainerDark
import com.notifast.app.presentation.theme.color.OnAquaContainerLight
import com.notifast.app.presentation.theme.color.OnAquaDark
import com.notifast.app.presentation.theme.color.OnAquaLight
import com.notifast.app.presentation.theme.color.OnBackgroundDark
import com.notifast.app.presentation.theme.color.OnBackgroundLight
import com.notifast.app.presentation.theme.color.OnErrorContainerDark
import com.notifast.app.presentation.theme.color.OnErrorContainerLight
import com.notifast.app.presentation.theme.color.OnErrorDark
import com.notifast.app.presentation.theme.color.OnErrorLight
import com.notifast.app.presentation.theme.color.OnRubyContainerDark
import com.notifast.app.presentation.theme.color.OnRubyContainerLight
import com.notifast.app.presentation.theme.color.OnRubyDark
import com.notifast.app.presentation.theme.color.OnRubyLight
import com.notifast.app.presentation.theme.color.OnSurfaceDark
import com.notifast.app.presentation.theme.color.OnSurfaceLight
import com.notifast.app.presentation.theme.color.OnSurfaceVariantDark
import com.notifast.app.presentation.theme.color.OnSurfaceVariantLight
import com.notifast.app.presentation.theme.color.OutlineDark
import com.notifast.app.presentation.theme.color.OutlineLight
import com.notifast.app.presentation.theme.color.RubyContainerDark
import com.notifast.app.presentation.theme.color.RubyContainerLight
import com.notifast.app.presentation.theme.color.RubyDark
import com.notifast.app.presentation.theme.color.RubyLight
import com.notifast.app.presentation.theme.color.SurfaceDark
import com.notifast.app.presentation.theme.color.SurfaceLight
import com.notifast.app.presentation.theme.color.SurfaceVariantDark
import com.notifast.app.presentation.theme.color.SurfaceVariantLight
import com.notifast.app.presentation.theme.type.Typography

private val DarkColorScheme = darkColorScheme(
    primary = RubyDark,
    onPrimary = OnRubyDark,
    primaryContainer = RubyContainerDark,
    onPrimaryContainer = OnRubyContainerDark,
    secondary = AquaDark,
    onSecondary = OnAquaDark,
    secondaryContainer = AquaContainerDark,
    onSecondaryContainer = OnAquaContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = RubyLight,
    onPrimary = OnRubyLight,
    primaryContainer = RubyContainerLight,
    onPrimaryContainer = OnRubyContainerLight,
    secondary = AquaLight,
    onSecondary = OnAquaLight,
    secondaryContainer = AquaContainerLight,
    onSecondaryContainer = OnAquaContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

@Composable
fun NotiFastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Forced false to maintain NotiFast corporate branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
