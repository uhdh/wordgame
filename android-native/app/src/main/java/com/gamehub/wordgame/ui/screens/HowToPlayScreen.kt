package com.gamehub.wordgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.logic.decomposeWordToTargetTiles
import com.gamehub.wordgame.logic.evaluateTileGuess
import com.gamehub.wordgame.ui.components.GameToolbar
import com.gamehub.wordgame.ui.components.LetterTile
import com.gamehub.wordgame.ui.components.LetterTileState
import com.gamehub.wordgame.ui.components.iconFor
import com.gamehub.wordgame.ui.components.toLetterTileState
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.WgType

/** 설정 > 플레이 방법 — 실제 게임 로직으로 계산한 예시를 보여주는 전체 화면. 플레이스토어 스크린샷용으로도 쓰인다. */
@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    val c = LocalWgColors.current
    Column(Modifier.fillMaxSize().background(c.surfacePage)) {
        GameToolbar(title = "플레이 방법", onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            GuessExampleSection()
            RotationSection()
            ComboSection()
            SplitJamoSection()
            ScoringSection()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val c = LocalWgColors.current
    Text(text, style = WgType.title, color = c.textStrong)
}

@Composable
private fun ExampleWordRow(label: String, tiles: List<String>, states: List<LetterTileState>? = null) {
    val c = LocalWgColors.current
    Column {
        Text(label, style = WgType.label, color = c.textFaint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            tiles.forEachIndexed { i, t ->
                LetterTile(t, states?.getOrNull(i) ?: LetterTileState.Filled, size = 42.dp, modifier = Modifier.padding(horizontal = 3.dp))
            }
        }
    }
}

private data class GuessStep(val label: String, val tiles: List<String>, val feedback: List<LetterTileState>, val desc: String)

/** 정답 '두루미'를 4번의 시도로 좁혀가는 실제 판정(evaluateTileGuess) 기반 단계별 예시.
 *  게임 화면의 "등록 기록" 카드와 같은 방식으로 여러 번 도전해 정답에 다가가는 흐름을 보여준다. */
@Composable
private fun GuessExampleSection() {
    val c = LocalWgColors.current
    val target = remember { decomposeWordToTargetTiles("두루미") }
    val steps = remember(target) {
        listOf(
            "라미다" to "자모 4개는 있지만, 2개는 단어에 없어요.",
            "다리마" to "'ㄷ·ㄹ·ㅁ' 세 자리를 제대로 맞혔어요!",
            "두리무" to "모음 ㅏ를 회전해서 ㅜ로 만들 수 있어요.",
            "두루미" to "전부 초록! 정답을 맞혔어요."
        ).mapIndexed { i, (word, desc) ->
            val tiles = decomposeWordToTargetTiles(word)
            val feedback = evaluateTileGuess(tiles, target).feedback.map { it.toLetterTileState() }
            GuessStep("${i + 1}번째 시도", tiles, feedback, desc)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "한글 단어를 자모 낱개로 풀어쓴 뒤,\n여러 번 도전해서 정답을 좁혀가요!",
            style = WgType.title, color = c.textStrong, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            steps.forEach { step ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExampleWordRow(step.label, step.tiles, step.feedback)
                    Text(step.desc, style = WgType.bodySm, color = c.textMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LegendRow(LetterTileState.Correct, "자모와 자리가 모두 정답이에요!")
            LegendRow(LetterTileState.Present, "낱말에 있지만 자리가 달라요.")
            LegendRow(LetterTileState.Absent, "이 자모는 낱말에 없어요.")
        }
    }
}

@Composable
private fun LegendRow(state: LetterTileState, desc: String) {
    val c = LocalWgColors.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LetterTile("가", state, size = 36.dp)
        Text(desc, style = WgType.bodySm, color = c.textMuted)
    }
}

@Composable
private fun RotationSection() {
    val c = LocalWgColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("자모 회전")
        Text("타일을 누르면 정해진 짝끼리만 돌아가요.", style = WgType.bodySm, color = c.textMuted)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rotRules.forEach { seq ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    seq.forEachIndexed { i, ch ->
                        LetterTile(ch, LetterTileState.Filled, size = 36.dp)
                        if (i != seq.lastIndex) {
                            Icon(iconFor("arrow_right_alt"), contentDescription = null, tint = c.textFaint, modifier = Modifier.padding(horizontal = 6.dp).size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComboSection() {
    val c = LocalWgColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("모음 결합")
        Text("두 타일을 나란히 두면 이중 모음이 돼요.", style = WgType.bodySm, color = c.textMuted)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            combos.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { (a, b, r) ->
                        Row(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(c.surfaceSunk)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(a, style = WgType.subtitle, color = c.textBody)
                            Text(" + ", style = WgType.caption, color = c.textFaint)
                            Text(b, style = WgType.subtitle, color = c.textBody)
                            Icon(iconFor("arrow_right_alt"), contentDescription = null, tint = c.textFaint, modifier = Modifier.padding(horizontal = 4.dp).size(16.dp))
                            Text(r, style = WgType.title, color = c.accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitJamoSection() {
    val c = LocalWgColors.current
    val tiles = remember { decomposeWordToTargetTiles("여덟") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("복합모음·쌍자음·겹받침은 더 작은 자모로 나뉘어요.")
        ExampleWordRow("정답 '여덟'", tiles)
        Text(
            "'덟'의 겹받침 'ㄼ'은 작은 ㄹ·ㅂ으로 풀어져요. 쌍자음·겹받침이 있는 단어는 타일 수가 더 많아져요.",
            style = WgType.bodySm, color = c.textMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ScoringSection() {
    val c = LocalWgColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("승점 규칙")
        listOf(
            "글자 수가 많을수록 승점이 높아요.",
            "6번째 시도부터는 승점이 1점 깎여요.",
            "초성 힌트를 쓰면 승점이 1점 깎여요.",
            "'섞기'는 타일을 자유롭게 다시 섞어요. '알아서 잘'은 정답 글자 수에 맞춰 하루 10번까지 자동으로 배치해줘요."
        ).forEach { line ->
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•", style = WgType.body, color = c.accent)
                Text(line, style = WgType.bodySm, color = c.textMuted)
            }
        }
    }
}
