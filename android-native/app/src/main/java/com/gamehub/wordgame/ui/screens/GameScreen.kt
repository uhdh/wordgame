package com.gamehub.wordgame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gamehub.wordgame.data.DAILY_AUTO_ARRANGE_LIMIT
import com.gamehub.wordgame.data.GameUiState
import com.gamehub.wordgame.logic.TileState
import com.gamehub.wordgame.ui.components.*
import com.gamehub.wordgame.ui.theme.GameDim
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.Radius
import com.gamehub.wordgame.ui.theme.WgType

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: GameUiState,
    soundOn: Boolean,
    onBack: () -> Unit,
    onOpenStageSelect: () -> Unit,
    onShareStage: () -> Unit,
    onToggleSound: () -> Unit,
    onTileTap: (Int) -> Unit,
    onTileMove: (Int, Int) -> Unit,
    onTileRotate: (Int) -> Unit,
    onShuffle: () -> Unit,
    onAutoArrange: () -> Unit,
    onSubmit: () -> Unit,
    onToggleHint: () -> Unit,
    onNextStage: () -> Unit,
    onOpenRanking: () -> Unit,
    onRandomStage: (Int) -> Unit,
    onOpenHowToPlay: () -> Unit
) {
    val c = LocalWgColors.current
    val puzzle = state.currentPuzzle ?: return
    var randomStageTarget by remember { mutableStateOf<Int?>(null) }
    val lastGuess = state.guesses.lastOrNull()
    val showAnswerModal = state.isRoundOver && lastGuess?.isExactMatch == true

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(c.surfacePage)) {
            GameToolbar(
                title = "단어조각",
                subtitle = "${puzzle.setLabel} ${puzzle.localStage}단계 · ${puzzle.level}",
                onBack = onBack,
                actions = listOf(
                    ToolbarAction("shuffle", "랜덤 스테이지") {
                        randomStageTarget = kotlin.random.Random.nextInt(com.gamehub.wordgame.logic.STAGES_100.size)
                    },
                    ToolbarAction("grid_view", "단계 선택", onOpenStageSelect),
                    ToolbarAction("share", "단계 공유", onShareStage),
                    ToolbarAction("volume_up", "효과음", onToggleSound),
                    ToolbarAction("help", "놀이 방법", onOpenHowToPlay)
                )
            )
            Box(Modifier.fillMaxWidth().height(3.dp).background(c.surfaceSunk)) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.stageProgressPct)
                        .background(c.accent)
                )
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GoalCard(state, onToggleHint)
                TilesCard(state, onTileTap, onTileMove, onTileRotate, onShuffle, onAutoArrange, onSubmit)
                HistoryCard(state)
            }
        }

        if (showAnswerModal) {
            AnswerModal(
                puzzle = puzzle,
                lastGuess = lastGuess!!,
                earnedPoints = state.lastEarnedPoints,
                onNext = onNextStage,
                onOpenRanking = onOpenRanking
            )
        }
    }

    randomStageTarget?.let { targetIndex ->
        val targetPuzzle = com.gamehub.wordgame.logic.STAGES_100[targetIndex]
        AlertDialog(
            onDismissRequest = { randomStageTarget = null },
            title = { Text("랜덤 스테이지") },
            text = { Text("${puzzle.setLabel} ${puzzle.localStage}단계 → ${targetPuzzle.setLabel} ${targetPuzzle.localStage}단계로 이동합니다.") },
            confirmButton = {
                Text(
                    "이동하기",
                    color = c.accent,
                    modifier = Modifier.clickable { randomStageTarget = null; onRandomStage(targetIndex) }.padding(12.dp)
                )
            },
            dismissButton = {
                Text("취소", modifier = Modifier.clickable { randomStageTarget = null }.padding(12.dp))
            }
        )
    }
}

@Composable
private fun GoalCard(state: GameUiState, onToggleHint: () -> Unit) {
    val c = LocalWgColors.current
    val puzzle = state.currentPuzzle!!
    WgCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WgBadge("정답 목표", BadgeTone.Accent)
                Text("${puzzle.length}글자 · 타일 ${puzzle.tiles.size}개", style = WgType.bodySm, color = c.textMuted)
            }
            HintMeter(used = state.hintUsed, total = 3, label = "초성", onUse = onToggleHint)
        }

        AnimatedVisibility(visible = state.hintOpen) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(c.pigmentGoldWash)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("초성", style = WgType.label, color = c.pigmentGoldDeep)
                Spacer(Modifier.width(12.dp))
                Text(puzzle.chosungHint, style = WgType.subtitle, color = c.textStrong)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("지금 조합된 낱말", style = WgType.caption, color = c.textFaint, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            val syls = state.assembledSyllables
            for (i in 0 until puzzle.length) {
                val ch = syls.getOrNull(i) ?: ""
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(64.dp, 76.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(c.surfaceCard)
                        .border(2.dp, if (ch.isNotEmpty()) c.borderStrong else c.borderHair, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ch, style = WgType.titleLg, color = if (ch.isNotEmpty()) c.textStrong else c.textFaint)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun TilesCard(
    state: GameUiState,
    onTileTap: (Int) -> Unit,
    onTileMove: (Int, Int) -> Unit,
    onTileRotate: (Int) -> Unit,
    onShuffle: () -> Unit,
    onAutoArrange: () -> Unit,
    onSubmit: () -> Unit
) {
    val c = LocalWgColors.current
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var targetHoverIndex by remember { mutableStateOf<Int?>(null) }
    val tileCenters = remember { mutableMapOf<Int, Offset>() }

    WgCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("보유 타일", style = WgType.subtitle.copy(fontWeight = FontWeight.Bold, fontFamily = com.gamehub.wordgame.ui.theme.FontDisplay), color = c.textStrong)
                Text("드래그 이동 · 🔄 회전", style = WgType.caption, color = c.textFaint)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.sm))
                        .border(1.dp, c.borderHair, RoundedCornerShape(Radius.sm))
                        .clickable(onClick = onShuffle)
                        .padding(10.dp)
                ) {
                    Icon(iconFor("shuffle"), contentDescription = "섞기", tint = c.textMuted, modifier = Modifier.size(18.dp))
                }
                val autoArrangeEnabled = state.autoArrangeRemaining > 0
                val autoArrangeTint = if (autoArrangeEnabled) c.textMuted else c.textFaint
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.sm))
                        .border(1.dp, c.borderHair, RoundedCornerShape(Radius.sm))
                        .clickable(enabled = autoArrangeEnabled, onClick = onAutoArrange)
                        .padding(10.dp)
                ) {
                    Icon(
                        iconFor("auto_fix_high"),
                        contentDescription = "알아서 잘 ${state.autoArrangeRemaining}/$DAILY_AUTO_ARRANGE_LIMIT",
                        tint = autoArrangeTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            state.activeTiles.forEachIndexed { index, tile ->
                val selected = state.selectedTileIndex == index
                val isDragging = draggingIndex == index
                val isHoverTarget = targetHoverIndex == index && draggingIndex != null && draggingIndex != index

                Box(
                    Modifier
                        .size(64.dp, 74.dp)
                        .onGloballyPositioned { coordinates ->
                            val pos = coordinates.positionInParent()
                            tileCenters[index] = Offset(pos.x + coordinates.size.width / 2f, pos.y + coordinates.size.height / 2f)
                        }
                        .zIndex(if (isDragging) 10f else if (isHoverTarget) 2f else 1f)
                        .graphicsLayer {
                            if (isDragging) {
                                translationX = dragOffset.x
                                translationY = dragOffset.y
                                scaleX = 1.12f
                                scaleY = 1.12f
                                shadowElevation = 16f
                            } else if (isHoverTarget) {
                                scaleX = 1.06f
                                scaleY = 1.06f
                            }
                        }
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isDragging -> c.accent
                                isHoverTarget -> c.surfaceSunk
                                selected -> c.ink0
                                else -> c.surfaceCard
                            }
                        )
                        .border(
                            2.dp,
                            when {
                                isDragging -> c.accent
                                isHoverTarget -> c.accent
                                selected -> c.ink0
                                else -> c.borderHair
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .pointerInput(tile.id, index) {
                            detectDragGestures(
                                onDragStart = {
                                    draggingIndex = index
                                    dragOffset = Offset.Zero
                                    targetHoverIndex = index
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount
                                    val startCenter = tileCenters[index]
                                    if (startCenter != null) {
                                        val currentPos = startCenter + dragOffset
                                        var closestIdx = index
                                        var minDistance = Float.MAX_VALUE
                                        tileCenters.forEach { (i, center) ->
                                            val dist = (center - currentPos).getDistance()
                                            if (dist < minDistance) {
                                                minDistance = dist
                                                closestIdx = i
                                            }
                                        }
                                        targetHoverIndex = closestIdx
                                    }
                                },
                                onDragEnd = {
                                    val from = draggingIndex
                                    val to = targetHoverIndex
                                    if (from != null && to != null && from != to) {
                                        onTileMove(from, to)
                                    }
                                    draggingIndex = null
                                    dragOffset = Offset.Zero
                                    targetHoverIndex = null
                                },
                                onDragCancel = {
                                    draggingIndex = null
                                    dragOffset = Offset.Zero
                                    targetHoverIndex = null
                                }
                            )
                        }
                        .clickable { onTileTap(index) }
                ) {
                    Text(
                        "${index + 1}",
                        style = WgType.caption.copy(fontFamily = com.gamehub.wordgame.ui.theme.FontMono, fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp)),
                        color = (if (isDragging || selected) c.textInvert else c.textStrong).copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 3.dp, start = 6.dp)
                    )
                    Text(
                        tile.char,
                        style = WgType.tile,
                        color = if (isDragging || selected) c.textInvert else c.textStrong,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    if (com.gamehub.wordgame.logic.isRotatable(tile.char)) {
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .clickable { onTileRotate(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                iconFor("rotate_right"), contentDescription = "회전",
                                tint = (if (isDragging || selected) c.textInvert else c.textStrong).copy(alpha = 0.55f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        WgButton(
            "낱말 제출", onClick = onSubmit, tone = BtnTone.Accent, size = BtnSize.Lg, block = true,
            iconLeft = "check", enabled = state.canSubmit
        )
        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMessage, style = WgType.bodySm, color = c.stateError, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun HistoryCard(state: GameUiState) {
    val c = LocalWgColors.current
    WgCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("등록 기록", style = WgType.subtitle, color = c.textStrong)
                Text("${state.guesses.size}회", style = WgType.caption, color = c.textFaint)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LegendDot(c.stateCorrect, "일치")
                LegendDot(c.statePresent, "포함")
                LegendDot(c.stateAbsent, "없음")
            }
        }
        Spacer(Modifier.height(10.dp))

        if (state.guesses.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(iconFor("history_edu"), contentDescription = null, tint = c.ink4, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(8.dp))
                Text("타일을 돌려 낱말을 만든 뒤 제출하면 여기에 판정이 쌓여요.", style = WgType.bodySm, color = c.textFaint, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.guesses.reversed().forEach { g ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(c.surfaceSunk)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${g.n}", style = WgType.caption.copy(fontFamily = com.gamehub.wordgame.ui.theme.FontMono), color = c.textFaint, modifier = Modifier.width(22.dp))
                        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            g.tiles.forEachIndexed { i, ch ->
                                val st = g.feedback.getOrNull(i) ?: TileState.ABSENT
                                val bg = when (st) {
                                    TileState.CORRECT -> c.stateCorrect
                                    TileState.PRESENT -> c.statePresent
                                    TileState.ABSENT -> c.stateAbsent
                                }
                                Box(
                                    Modifier.size(32.dp, 36.dp).clip(RoundedCornerShape(3.dp)).background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(ch, style = WgType.subtitle, color = androidx.compose.ui.graphics.Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    val c = LocalWgColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = WgType.caption, color = c.textMuted)
    }
}

@Composable
private fun AnswerModal(
    puzzle: com.gamehub.wordgame.logic.Puzzle,
    lastGuess: com.gamehub.wordgame.data.GuessEntry,
    earnedPoints: Int,
    onNext: () -> Unit,
    onOpenRanking: () -> Unit
) {
    val c = LocalWgColors.current
    Box(
        Modifier
            .fillMaxSize()
            .background(c.surfaceScrim)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(c.surfaceCard)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Verified, contentDescription = null, tint = c.stateCorrect, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text("맞혔어요", style = WgType.titleLg, color = c.textStrong)
            Spacer(Modifier.height(4.dp))
            Text(
                "정답은 ${puzzle.answer}, ${lastGuess.n}번째에 맞혔어요",
                style = WgType.body, color = c.textMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            ResultShareCard(
                title = "단어조각 ${puzzle.setLabel} ${puzzle.localStage}단계",
                line = "${lastGuess.n}/6 · 승점 +${earnedPoints}점"
            )
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WgButton("다음 단계", onClick = onNext, tone = BtnTone.Primary, size = BtnSize.Lg, block = true, iconRight = "chevron_right")
                WgButton("순위 보기", onClick = onOpenRanking, tone = BtnTone.Outline, size = BtnSize.Md, block = true, iconLeft = "emoji_events")
            }
        }
    }
}
