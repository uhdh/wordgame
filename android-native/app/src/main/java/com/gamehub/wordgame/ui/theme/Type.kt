package com.gamehub.wordgame.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ponytail: the design's brand serif (Nanum Myeongjo, shipped as .woff2 in the design system)
// needs a TTF/OTF to bundle on Android; FontFamily.Serif renders via the platform's CJK serif
// as a close stand-in. Swap in a bundled Nanum Myeongjo TTF under res/font/ for exact fidelity.
val FontDisplay = FontFamily.Serif
val FontUi = FontFamily.Default
val FontMono = FontFamily.Monospace

object WgType {
    val display = TextStyle(fontFamily = FontDisplay, fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 51.sp)
    val titleLg = TextStyle(fontFamily = FontDisplay, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 35.sp)
    val title = TextStyle(fontFamily = FontUi, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 28.sp)
    val subtitle = TextStyle(fontFamily = FontUi, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp)
    val body = TextStyle(fontFamily = FontUi, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp)
    val bodySm = TextStyle(fontFamily = FontUi, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 21.sp)
    val label = TextStyle(fontFamily = FontUi, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 14.sp)
    val caption = TextStyle(fontFamily = FontUi, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp)
    val tile = TextStyle(fontFamily = FontUi, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 30.sp)
    val stat = TextStyle(fontFamily = FontUi, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 36.sp)
}
