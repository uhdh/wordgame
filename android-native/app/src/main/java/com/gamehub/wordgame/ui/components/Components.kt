package com.gamehub.wordgame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.Radius
import com.gamehub.wordgame.ui.theme.Sp
import com.gamehub.wordgame.ui.theme.WgType

data class ToolbarAction(val icon: String, val label: String, val onClick: () -> Unit = {})

/** Ported from Wordgameui_…GameToolbar — back arrow, title/subtitle, trailing icon actions. */
@Composable
fun GameToolbar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: List<ToolbarAction> = emptyList()
) {
    val c = LocalWgColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceCard)
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = c.textStrong)
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = WgType.subtitle.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp), color = c.textStrong, maxLines = 1)
            if (subtitle != null) {
                Text(subtitle, style = WgType.caption, color = c.textFaint, maxLines = 1)
            }
        }
        actions.forEach { a ->
            WgIconButton(icon = a.icon, label = a.label, onClick = a.onClick)
        }
        Spacer(Modifier.width(4.dp))
    }
}

enum class IconBtnSize(val dp: androidx.compose.ui.unit.Dp, val iconDp: androidx.compose.ui.unit.Dp) {
    Sm(32.dp, 18.dp), Md(40.dp, 22.dp)
}

enum class IconBtnTone { Outline, Filled, Plain }

@Composable
fun WgIconButton(
    icon: String,
    label: String,
    size: IconBtnSize = IconBtnSize.Md,
    tone: IconBtnTone = IconBtnTone.Plain,
    active: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val c = LocalWgColors.current
    val bg = when {
        active -> c.ink0
        tone == IconBtnTone.Filled -> c.surfaceSunk
        else -> Color.Transparent
    }
    val border = if (tone == IconBtnTone.Outline) c.borderHair else null
    val tint = if (active) c.textInvert else c.textMuted

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg)
            .then(if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(Radius.sm)) else Modifier)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(iconFor(icon), contentDescription = label, tint = tint, modifier = Modifier.size(size.iconDp))
    }
}

enum class BtnTone { Accent, Primary, Outline, Quiet }
enum class BtnSize(val height: androidx.compose.ui.unit.Dp) { Lg(52.dp), Md(44.dp) }

@Composable
fun WgButton(
    text: String,
    onClick: () -> Unit,
    tone: BtnTone = BtnTone.Primary,
    size: BtnSize = BtnSize.Lg,
    block: Boolean = false,
    iconLeft: String? = null,
    iconRight: String? = null,
    enabled: Boolean = true
) {
    val c = LocalWgColors.current
    val (bg, fg, border) = when (tone) {
        BtnTone.Accent -> Triple(c.accent, Color.White, null)
        BtnTone.Primary -> Triple(c.ink0, c.textInvert, null)
        BtnTone.Outline -> Triple(Color.Transparent, c.textBody, c.borderStrong)
        BtnTone.Quiet -> Triple(Color.Transparent, c.textMuted, null)
    }
    val alpha = if (enabled) 1f else 0.45f

    Row(
        modifier = Modifier
            .let { if (block) it.fillMaxWidth() else it }
            .height(size.height)
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg.copy(alpha = bg.alpha * alpha))
            .then(if (border != null) Modifier.border(2.dp, border.copy(alpha = alpha), RoundedCornerShape(Radius.sm)) else Modifier)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconLeft != null) {
            Icon(iconFor(iconLeft), contentDescription = null, tint = fg.copy(alpha = alpha), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = fg.copy(alpha = alpha), style = WgType.subtitle)
        if (iconRight != null) {
            Spacer(Modifier.width(8.dp))
            Icon(iconFor(iconRight), contentDescription = null, tint = fg.copy(alpha = alpha), modifier = Modifier.size(20.dp))
        }
    }
}

enum class BadgeTone { Accent, Correct, Present, Neutral }

@Composable
fun WgBadge(text: String, tone: BadgeTone = BadgeTone.Neutral) {
    val c = LocalWgColors.current
    val (bg, fg) = when (tone) {
        BadgeTone.Accent -> c.accentWash to c.pigmentRedDeep
        BadgeTone.Correct -> c.pigmentGreenWash to c.pigmentGreenDeep
        BadgeTone.Present -> c.pigmentGoldWash to c.pigmentGoldDeep
        BadgeTone.Neutral -> c.surfaceSunk to c.textMuted
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, style = WgType.label, color = fg)
    }
}

/** A "종이" card: white surface, hairline border, rounded corners. */
@Composable
fun WgCard(modifier: Modifier = Modifier, padding: androidx.compose.ui.unit.Dp = Sp.s6, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalWgColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(c.surfaceCard)
            .border(1.dp, c.borderHair, RoundedCornerShape(Radius.md))
            .padding(padding)
    ) {
        content()
    }
}
