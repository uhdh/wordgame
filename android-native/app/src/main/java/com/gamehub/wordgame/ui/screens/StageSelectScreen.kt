package com.gamehub.wordgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.logic.STAGES_100
import com.gamehub.wordgame.logic.STAGE_SETS
import com.gamehub.wordgame.logic.getStageSetForIndex
import com.gamehub.wordgame.billing.canAccessTheme
import com.gamehub.wordgame.ui.components.GameToolbar
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.WgType

private val LEVELS = listOf("전체", "쉬움", "보통", "어려움", "최고난도")

@Composable
fun StageSelectScreen(
    clearedStages: Set<Int>,
    currentStage: Int,
    ownedThemes: Set<String>,
    onBack: () -> Unit,
    onSelectStage: (Int) -> Unit,
    onRequestTheme: (String) -> Unit
) {
    val c = LocalWgColors.current
    var levelFilter by remember { mutableStateOf("전체") }
    var setFilter by remember { mutableStateOf(getStageSetForIndex(currentStage).key) }

    val currentSet = STAGE_SETS.first { it.key == setFilter }
    val clearedInSet = clearedStages.count { it >= currentSet.start && it < currentSet.start + currentSet.size }

    Column(Modifier.fillMaxSize().background(c.surfacePage)) {
        GameToolbar(
            title = "단계 선택",
            subtitle = "${currentSet.label} ${currentSet.size}단계 중 ${clearedInSet}단계 마침",
            onBack = onBack
        )

        Row(
            Modifier
                .fillMaxWidth()
                .background(c.surfaceCard)
                .border(width = 1.dp, color = c.borderHair)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            STAGE_SETS.forEach { set ->
                val selected = set.key == setFilter
                val unlocked = canAccessTheme(set.key, ownedThemes)
                Box(
                    Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) c.ink0 else c.surfaceCard)
                        .border(2.dp, if (selected) c.ink0 else c.borderHair, RoundedCornerShape(999.dp))
                        .clickable { if (unlocked) setFilter = set.key else onRequestTheme(set.key) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (unlocked) set.label else "${set.label} 🔒", style = WgType.label, color = if (selected) c.textInvert else c.textBody)
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .background(c.surfaceCard)
                .border(width = 1.dp, color = c.borderHair)
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LEVELS.forEach { lv ->
                val selected = lv == levelFilter
                Box(
                    Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) c.ink0 else c.surfaceCard)
                        .border(2.dp, if (selected) c.ink0 else c.borderHair, RoundedCornerShape(999.dp))
                        .clickable { levelFilter = lv }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(lv, style = WgType.label, color = if (selected) c.textInvert else c.textBody)
                }
            }
        }

        val filtered = remember(levelFilter, setFilter) {
            STAGES_100.filter { it.setKey == setFilter && (levelFilter == "전체" || it.level == levelFilter) }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filtered) { puzzle ->
                val stageIdx = puzzle.stage - 1
                val cleared = stageIdx in clearedStages
                val isCurrent = stageIdx == currentStage
                val (bg, border, fg) = when {
                    isCurrent -> Triple(c.ink0, c.ink0, c.textInvert)
                    cleared -> Triple(c.pigmentGreenWash, c.pigmentGreen, c.pigmentGreenDeep)
                    else -> Triple(c.surfaceCard, c.borderHair, c.textFaint)
                }
                Column(
                    Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .border(2.dp, border, RoundedCornerShape(6.dp))
                        .clickable { onSelectStage(stageIdx) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("${puzzle.localStage}", style = WgType.subtitle, color = fg)
                    Text("${puzzle.length}글자", style = WgType.caption.copy(fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp)), color = fg)
                }
            }
        }
    }
}
