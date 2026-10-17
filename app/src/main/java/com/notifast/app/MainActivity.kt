package com.notifast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.feature.main.MainViewModel
import com.notifast.app.presentation.navigation.MainContent
import com.notifast.app.presentation.theme.theme.NotiFastTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            androidx.compose.runtime.LaunchedEffect(intent) {
                mainViewModel.handleNotificationIntent(intent)
            }
            val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()
            val hapticsEnabled by mainViewModel.hapticsEnabled.collectAsStateWithLifecycle()
            val themePresetId by mainViewModel.themePresetId.collectAsStateWithLifecycle()
            val backgroundPresetId by mainViewModel.backgroundPresetId.collectAsStateWithLifecycle()
            val customPrimaryHex by mainViewModel.customPrimaryHex.collectAsStateWithLifecycle()
            val customBackgroundHex by mainViewModel.customBackgroundHex.collectAsStateWithLifecycle()
            val themePreset = remember(themePresetId) {
                com.notifast.app.domain.model.theme.ThemePreset.fromId(themePresetId)
            }
            val backgroundPreset = remember(backgroundPresetId) {
                com.notifast.app.domain.model.theme.BackgroundPreset.fromId(backgroundPresetId)
            }
            val darkTheme = isDarkMode ?: isSystemInDarkTheme()
            val systemHaptics = LocalHapticFeedback.current
            val effectiveHaptics = remember(hapticsEnabled, systemHaptics) {
                if (hapticsEnabled) {
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
                customPrimaryHex = customPrimaryHex,
                customBackgroundHex = customBackgroundHex
            ) {
                CompositionLocalProvider(LocalHapticFeedback provides effectiveHaptics) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainContent()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
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
