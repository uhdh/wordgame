package com.gamehub.wordgame.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Ported 1:1 from the wordgameui design system's tokens/colors.css (both `:root` and
 * `[data-theme="dark"]` blocks). "종이" (paper) = surfaces, "먹" (ink) = text/borders,
 * "안료" (pigment) = state/accent colors.
 */
data class WgColors(
    val paper0: Color, val paper1: Color, val paper2: Color, val paper3: Color, val paperEdge: Color,
    val ink0: Color, val ink1: Color, val ink2: Color, val ink3: Color, val ink4: Color,

    val pigmentRed: Color, val pigmentRedDeep: Color, val pigmentRedWash: Color,
    val pigmentGreen: Color, val pigmentGreenDeep: Color, val pigmentGreenWash: Color,
    val pigmentGold: Color, val pigmentGoldDeep: Color, val pigmentGoldWash: Color,
    val pigmentIndigo: Color, val pigmentIndigoDeep: Color, val pigmentIndigoWash: Color,
    val pigmentPlum: Color, val pigmentPlumDeep: Color, val pigmentPlumWash: Color,
    val pigmentMoss: Color, val pigmentMossDeep: Color, val pigmentMossWash: Color,
    val pigmentClay: Color, val pigmentClayDeep: Color, val pigmentClayWash: Color,

    // Semantic aliases
    val surfacePage: Color, val surfaceCard: Color, val surfaceSunk: Color,
    val surfaceRaised: Color, val surfaceInvert: Color, val surfaceScrim: Color,
    val textStrong: Color, val textBody: Color, val textMuted: Color,
    val textFaint: Color, val textInvert: Color, val textLink: Color,
    val borderHair: Color, val borderFirm: Color, val borderStrong: Color,
    val accent: Color, val accentPress: Color, val accentWash: Color,
    val stateCorrect: Color, val statePresent: Color, val stateAbsent: Color,
    val stateError: Color, val stateInfo: Color, val focusRing: Color
)

val LightWgColors = WgColors(
    paper0 = Color(0xFFFBF9F4), paper1 = Color(0xFFF5F1E8), paper2 = Color(0xFFEDE7DA),
    paper3 = Color(0xFFE2DACA), paperEdge = Color(0xFFD6CCB8),
    ink0 = Color(0xFF141310), ink1 = Color(0xFF2A2722), ink2 = Color(0xFF57524A),
    ink3 = Color(0xFF857E72), ink4 = Color(0xFFB3AB9C),

    pigmentRed = Color(0xFFD2401E), pigmentRedDeep = Color(0xFFA32E13), pigmentRedWash = Color(0xFFF7DFD7),
    pigmentGreen = Color(0xFF1F8A4C), pigmentGreenDeep = Color(0xFF146338), pigmentGreenWash = Color(0xFFD9EDE0),
    pigmentGold = Color(0xFFD69B0B), pigmentGoldDeep = Color(0xFFA07200), pigmentGoldWash = Color(0xFFF8EBC9),
    pigmentIndigo = Color(0xFF3455A4), pigmentIndigoDeep = Color(0xFF233C78), pigmentIndigoWash = Color(0xFFDCE3F3),
    pigmentPlum = Color(0xFF7A4A8C), pigmentPlumDeep = Color(0xFF5A3268), pigmentPlumWash = Color(0xFFEADFEF),
    pigmentMoss = Color(0xFF6C8C4E), pigmentMossDeep = Color(0xFF4C6636), pigmentMossWash = Color(0xFFE3EBD8),
    pigmentClay = Color(0xFFB8763A), pigmentClayDeep = Color(0xFF8A5526), pigmentClayWash = Color(0xFFF3E2D0),

    surfacePage = Color(0xFFFBF9F4), surfaceCard = Color(0xFFFFFFFF), surfaceSunk = Color(0xFFEDE7DA),
    surfaceRaised = Color(0xFFFFFFFF), surfaceInvert = Color(0xFF141310), surfaceScrim = Color(0x85141310),
    textStrong = Color(0xFF141310), textBody = Color(0xFF2A2722), textMuted = Color(0xFF57524A),
    textFaint = Color(0xFF857E72), textInvert = Color(0xFFFBF9F4), textLink = Color(0xFF3455A4),
    borderHair = Color(0xFFD6CCB8), borderFirm = Color(0xFFB3AB9C), borderStrong = Color(0xFF141310),
    accent = Color(0xFFD2401E), accentPress = Color(0xFFA32E13), accentWash = Color(0xFFF7DFD7),
    stateCorrect = Color(0xFF1F8A4C), statePresent = Color(0xFFD69B0B), stateAbsent = Color(0xFF857E72),
    stateError = Color(0xFFD2401E), stateInfo = Color(0xFF3455A4), focusRing = Color(0xFF3455A4)
)

val DarkWgColors = WgColors(
    paper0 = Color(0xFF131210), paper1 = Color(0xFF1C1A17), paper2 = Color(0xFF26231E),
    paper3 = Color(0xFF332F28), paperEdge = Color(0xFF403A32),
    ink0 = Color(0xFFF6F2E9), ink1 = Color(0xFFE4DFD4), ink2 = Color(0xFFB6AEA0),
    ink3 = Color(0xFF8A8274), ink4 = Color(0xFF5C554A),

    pigmentRed = Color(0xFFF26B45), pigmentRedDeep = Color(0xFFC74A26), pigmentRedWash = Color(0xFF3A1E14),
    pigmentGreen = Color(0xFF4FBF7C), pigmentGreenDeep = Color(0xFF2E8A55), pigmentGreenWash = Color(0xFF14301F),
    pigmentGold = Color(0xFFF0BC3D), pigmentGoldDeep = Color(0xFFB98C15), pigmentGoldWash = Color(0xFF33280C),
    pigmentIndigo = Color(0xFF7A9AE6), pigmentIndigoDeep = Color(0xFF4E6FBC), pigmentIndigoWash = Color(0xFF1A2340),
    pigmentPlum = Color(0xFFB189C2), pigmentPlumDeep = Color(0xFF7F5C90), pigmentPlumWash = Color(0xFF2B1D31),
    pigmentMoss = Color(0xFF9CBC7C), pigmentMossDeep = Color(0xFF6F8F52), pigmentMossWash = Color(0xFF1E2716),
    pigmentClay = Color(0xFFD9975C), pigmentClayDeep = Color(0xFFA66F38), pigmentClayWash = Color(0xFF2F2015),

    // dark [data-theme] re-binds semantics onto the dark primitives (see the .dc.html's own
    // note: DS's dark block only restates raw pigments, so semantics must be recomputed here too)
    surfacePage = Color(0xFF131210), surfaceCard = Color(0xFF1C1A17), surfaceSunk = Color(0xFF26231E),
    surfaceRaised = Color(0xFF26231E), surfaceInvert = Color(0xFFF6F2E9), surfaceScrim = Color(0xA8000000),
    textStrong = Color(0xFFF6F2E9), textBody = Color(0xFFE4DFD4), textMuted = Color(0xFFB6AEA0),
    textFaint = Color(0xFF8A8274), textInvert = Color(0xFF131210), textLink = Color(0xFF7A9AE6),
    borderHair = Color(0xFF403A32), borderFirm = Color(0xFF5C554A), borderStrong = Color(0xFFF6F2E9),
    accent = Color(0xFFF26B45), accentPress = Color(0xFFC74A26), accentWash = Color(0xFF3A1E14),
    stateCorrect = Color(0xFF4FBF7C), statePresent = Color(0xFFF0BC3D), stateAbsent = Color(0xFF8A8274),
    stateError = Color(0xFFF26B45), stateInfo = Color(0xFF7A9AE6), focusRing = Color(0xFF7A9AE6)
)

val LocalWgColors = compositionLocalOf { LightWgColors }
