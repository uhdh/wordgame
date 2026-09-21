package com.gamehub.wordgame.billing

val FREE_THEME_KEYS = setOf("basic", "subway", "brand", "food")

val THEME_PRODUCT_IDS = linkedMapOf(
    "hard" to "unlock_hard",
    "slang" to "unlock_slang",
    "celebrity" to "unlock_celebrity",
    "ott" to "unlock_ott",
    "franchise" to "unlock_franchise"
)

fun canAccessTheme(themeKey: String, ownedThemes: Set<String>): Boolean =
    themeKey in FREE_THEME_KEYS || themeKey in ownedThemes
