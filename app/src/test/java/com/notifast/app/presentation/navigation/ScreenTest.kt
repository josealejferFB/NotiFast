package com.notifast.app.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTest {

    @Test
    fun `onboarding screen has correct route`() {
        assertEquals("onboarding", Screen.Onboarding.route)
    }

    @Test
    fun `bottom navigation items contain core screens`() {
        assertEquals(4, Screen.bottomNavItems.size)
        assertTrue(Screen.bottomNavItems.contains(Screen.Notes))
        assertTrue(Screen.bottomNavItems.contains(Screen.Schedules))
        assertTrue(Screen.bottomNavItems.contains(Screen.Grades))
        assertTrue(Screen.bottomNavItems.contains(Screen.Settings))
    }
}
