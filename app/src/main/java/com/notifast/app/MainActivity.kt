package com.notifast.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.domain.model.theme.BackgroundPreset
import com.notifast.app.domain.model.theme.ThemePreset
import com.notifast.app.presentation.feature.main.MainViewModel
import com.notifast.app.presentation.feature.splash.SplashScreen
import com.notifast.app.presentation.navigation.MainContent
import com.notifast.app.presentation.theme.theme.NotiFastTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !mainViewModel.isAppReady.value
        }

        enableEdgeToEdge()

        setContent {
            LaunchedEffect(intent) {
                mainViewModel.handleNotificationIntent(intent)
            }

            val splashState by mainViewModel.splashState.collectAsStateWithLifecycle()
            val themePreset = remember(splashState.themePresetId) {
                ThemePreset.fromId(splashState.themePresetId)
            }
            val backgroundPreset = remember(splashState.backgroundPresetId) {
                BackgroundPreset.fromId(splashState.backgroundPresetId)
            }
            val darkTheme = splashState.isDarkMode ?: isSystemInDarkTheme()
            val systemHaptics = LocalHapticFeedback.current
            val effectiveHaptics = remember(splashState.hapticsEnabled, systemHaptics) {
                if (splashState.hapticsEnabled) {
                    systemHaptics
                } else {
                    object : HapticFeedback {
                        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
                    }
                }
            }

            NotiFastTheme(
                darkTheme = darkTheme,
                themePreset = themePreset,
                backgroundPreset = backgroundPreset,
                customPrimaryHex = splashState.customPrimaryHex,
                customBackgroundHex = splashState.customBackgroundHex
            ) {
                CompositionLocalProvider(LocalHapticFeedback provides effectiveHaptics) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var isSplashVisible by rememberSaveable { mutableStateOf(true) }

                        AnimatedContent(
                            targetState = isSplashVisible,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(durationMillis = 380)) togetherWith
                                    (fadeOut(animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)) +
                                        scaleOut(targetScale = 1.04f, animationSpec = tween(durationMillis = 380)))
                            },
                            label = "SplashToMainTransition"
                        ) { showSplash ->
                            if (showSplash) {
                                SplashScreen(
                                    onSplashComplete = {
                                        isSplashVisible = false
                                    }
                                )
                            } else {
                                MainContent()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    companion object {
        const val EXTRA_DESTINATION = "extra_destination"
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_EVALUATION_ID = "extra_evaluation_id"

        const val DESTINATION_SCHEDULES = "schedules"
        const val DESTINATION_SUBJECT_DETAIL = "subject_detail"
        const val DESTINATION_SETTINGS = "settings"
    }
}
