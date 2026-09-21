package com.gamehub.wordgame.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeAccessTest {
    @Test
    fun `only first four themes are free and purchases unlock the rest`() {
        listOf("basic", "subway", "brand", "food").forEach {
            assertTrue(canAccessTheme(it, emptySet()))
        }
        THEME_PRODUCT_IDS.keys.forEach {
            assertFalse(canAccessTheme(it, emptySet()))
            assertTrue(canAccessTheme(it, setOf(it)))
        }
    }
}
