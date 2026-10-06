package com.notifast.app.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenInitOrderTest {
    @Test
    fun `bottomNavItems routes are non-empty when accessed first`() {
        val routes = Screen.bottomNavItems.map { it.route }
        println("bottomNavItems routes = $routes")
        println("bottomNavItems = ${Screen.bottomNavItems}")
        assertEquals(listOf("notes", "agenda", "calificaciones", "settings"), routes)
    }
}
