package com.notifast.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserPrefsRepositoryTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: UserPrefsRepositoryImpl

    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    private val hasDarkModeSetKey = booleanPreferencesKey("has_dark_mode_set")
    private val defaultGradeScaleKey = stringPreferencesKey("default_grade_scale")
    private val showCompletedEvaluationsKey = booleanPreferencesKey("show_completed_evaluations")

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        repository = UserPrefsRepositoryImpl(dataStore)
    }

    @Test
    fun `isDarkModeFlow returns null when hasDarkModeSet is not true`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[hasDarkModeSetKey] } returns null
        every { dataStore.data } returns flowOf(prefs)

        repository.isDarkModeFlow.test {
            val value = awaitItem()
            assertNull(value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isDarkModeFlow returns boolean when hasDarkModeSet is true`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[hasDarkModeSetKey] } returns true
        every { prefs[isDarkModeKey] } returns true
        every { dataStore.data } returns flowOf(prefs)

        repository.isDarkModeFlow.test {
            val value = awaitItem()
            assertEquals(true, value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `defaultGradeScaleFlow returns 0-10 by default`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        repository.defaultGradeScaleFlow.test {
            val value = awaitItem()
            assertEquals("0-10", value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `defaultGradeScaleFlow returns configured scale`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[defaultGradeScaleKey] } returns "0-20"
        every { dataStore.data } returns flowOf(prefs)

        repository.defaultGradeScaleFlow.test {
            val value = awaitItem()
            assertEquals("0-20", value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showCompletedEvaluationsFlow returns true by default`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        repository.showCompletedEvaluationsFlow.test {
            val value = awaitItem()
            assertTrue(value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setDarkMode returns success`() = runTest {
        val result = repository.setDarkMode(true)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `setDefaultGradeScale returns success`() = runTest {
        val result = repository.setDefaultGradeScale("0-100")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `setShowCompletedEvaluations returns success`() = runTest {
        val result = repository.setShowCompletedEvaluations(false)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `themePresetIdFlow returns classic by default`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        repository.themePresetIdFlow.test {
            val value = awaitItem()
            assertEquals("classic", value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `themePresetIdFlow returns configured preset`() = runTest {
        val themePresetKey = stringPreferencesKey("theme_preset_id")
        val prefs = mockk<Preferences>()
        every { prefs[themePresetKey] } returns "forest_sage"
        every { dataStore.data } returns flowOf(prefs)

        repository.themePresetIdFlow.test {
            val value = awaitItem()
            assertEquals("forest_sage", value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `customPrimaryHexFlow returns null by default`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        repository.customPrimaryHexFlow.test {
            val value = awaitItem()
            assertNull(value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `customPrimaryHexFlow returns configured hex`() = runTest {
        val customHexKey = stringPreferencesKey("custom_theme_primary_hex")
        val prefs = mockk<Preferences>()
        every { prefs[customHexKey] } returns "#15803D"
        every { dataStore.data } returns flowOf(prefs)

        repository.customPrimaryHexFlow.test {
            val value = awaitItem()
            assertEquals("#15803D", value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setThemePresetId returns success`() = runTest {
        val result = repository.setThemePresetId("midnight_indigo")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `setCustomPrimaryHex returns success`() = runTest {
        val result = repository.setCustomPrimaryHex("#4F46E5")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `backgroundPresetIdFlow returns classic by default`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        repository.backgroundPresetIdFlow.test {
            val value = awaitItem()
            assertEquals("classic", value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `backgroundPresetIdFlow returns configured preset`() = runTest {
        val backgroundPresetKey = stringPreferencesKey("background_preset_id")
        val prefs = mockk<Preferences>()
        every { prefs[backgroundPresetKey] } returns "warm_mocha"
        every { dataStore.data } returns flowOf(prefs)

        repository.backgroundPresetIdFlow.test {
            val value = awaitItem()
            assertEquals("warm_mocha", value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setBackgroundPresetId returns success`() = runTest {
        val result = repository.setBackgroundPresetId("sakura_zen")
        assertTrue(result.isSuccess)
    }
}

