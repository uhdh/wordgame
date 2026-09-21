package com.gamehub.wordgame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.logic.TileState
import com.gamehub.wordgame.ui.theme.GameDim
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.Radius
import com.gamehub.wordgame.ui.theme.WgColors
import com.gamehub.wordgame.ui.theme.WgType

enum class LetterTileState { Empty, Filled, Correct, Present, Absent }

fun TileState.toLetterTileState() = when (this) {
    TileState.CORRECT -> LetterTileState.Correct
    TileState.PRESENT -> LetterTileState.Present
    TileState.ABSENT -> LetterTileState.Absent
}

/** Ported from the design's TILE_ST map: empty/filled outline vs. correct/present/absent fills. */
@Composable
fun letterTileColors(state: LetterTileState): Triple<Color, Color, Color> {
    val c = LocalWgColors.current
    return when (state) {
        LetterTileState.Empty -> Triple(c.paperEdge, Color.Transparent, c.textStrong)
        LetterTileState.Filled -> Triple(c.ink0, Color.Transparent, c.textStrong)
        LetterTileState.Correct -> Triple(c.stateCorrect, c.stateCorrect, Color.White)
        LetterTileState.Present -> Triple(c.statePresent, c.statePresent, Color.White)
        LetterTileState.Absent -> Triple(c.stateAbsent, c.stateAbsent, Color.White)
    }
}

@Composable
fun LetterTile(
    letter: String,
    state: LetterTileState,
    size: androidx.compose.ui.unit.Dp = GameDim.tileSm,
    modifier: Modifier = Modifier
) {
    val (border, bg, fg) = letterTileColors(state)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(Radius.xs))
            .background(bg)
            .border(2.dp, border, RoundedCornerShape(Radius.xs)),
        contentAlignment = Alignment.Center
    ) {
        Text(letter, style = WgType.tile.copy(fontSize = (size.value * 0.44f).sp()), color = fg)
    }
}

private fun Float.sp() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)

@Composable
fun HintMeter(used: Boolean, total: Int = 3, label: String, onUse: () -> Unit) {
    val c = LocalWgColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onUse() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(iconFor("lightbulb"), contentDescription = label, tint = if (used) c.pigmentGold else c.textFaint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = WgType.label, color = if (used) c.pigmentGold else c.textFaint)
        Spacer(Modifier.width(6.dp))
        Row {
            repeat(total) { i ->
                Box(
                    modifier = Modifier
                        .padding(end = 3.dp)
                        .size(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (used && i == 0) c.pigmentGold else c.paper3)
                )
            }
        }
    }
}

/** Answer-modal summary card */
@Composable
fun ResultShareCard(title: String, line: String) {
    val c = LocalWgColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(c.surfaceSunk)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = WgType.subtitle, color = c.textStrong)
        Spacer(Modifier.height(4.dp))
        Text(line, style = WgType.caption, color = c.textFaint)
    }
}

data class TabItem(val id: String, val label: String, val icon: String, val badge: Boolean = false)

@Composable
fun WgTabBar(value: String, items: List<TabItem>, onSelect: (String) -> Unit) {
    val c = LocalWgColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(c.surfaceCard)
            .border(1.dp, c.borderHair, RoundedCornerShape(0.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = item.id == value
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .clickable { onSelect(item.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(iconFor(item.icon), contentDescription = item.label, tint = if (selected) c.ink0 else c.textFaint, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(2.dp))
                Text(item.label, style = WgType.caption, color = if (selected) c.ink0 else c.textFaint)
            }
        }
    }
}

enum class PuzzleStatus { Progress, New, Idle, Done, Locked }
enum class Pigment { Red, Green, Indigo, Gold, Clay, Moss, Plum }

fun pigmentColor(colors: WgColors, p: Pigment): Color = when (p) {
    Pigment.Red -> colors.pigmentRed
    Pigment.Green -> colors.pigmentGreen
    Pigment.Indigo -> colors.pigmentIndigo
    Pigment.Gold -> colors.pigmentGold
    Pigment.Clay -> colors.pigmentClay
    Pigment.Moss -> colors.pigmentMoss
    Pigment.Plum -> colors.pigmentPlum
}

@Composable
fun PuzzleCard(
    name: String,
    tagline: String,
    icon: String,
    pigment: Pigment,
    status: PuzzleStatus,
    meta: String,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val c = LocalWgColors.current
    val tint = pigmentColor(c, pigment)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(c.surfaceCard)
            .border(1.dp, c.borderHair, RoundedCornerShape(Radius.md))
            .clickable(enabled = enabled) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconFor(icon), contentDescription = name, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = WgType.subtitle, color = c.textStrong)
            Text(tagline, style = WgType.caption, color = c.textFaint, maxLines = 2)
        }
        Spacer(Modifier.width(8.dp))
        when (status) {
            PuzzleStatus.Progress -> WgBadge(meta, BadgeTone.Accent)
            PuzzleStatus.Done -> WgBadge(meta, BadgeTone.Correct)
            PuzzleStatus.New -> WgBadge("신규", BadgeTone.Present)
            PuzzleStatus.Idle -> Icon(iconFor("chevron_right"), contentDescription = null, tint = c.textFaint)
            PuzzleStatus.Locked -> WgBadge("잠금 · $meta", BadgeTone.Present)
        }
    }
}
