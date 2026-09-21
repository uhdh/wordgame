package com.gamehub.wordgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.ui.components.BtnSize
import com.gamehub.wordgame.ui.components.BtnTone
import com.gamehub.wordgame.ui.components.LetterTile
import com.gamehub.wordgame.ui.components.LetterTileState
import com.gamehub.wordgame.ui.components.WgButton
import com.gamehub.wordgame.ui.components.iconFor
import com.gamehub.wordgame.logic.STAGES_100
import com.gamehub.wordgame.ui.theme.GameDim
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.WgType

@Composable
fun OnboardingScreen(onStart: () -> Unit, onHowToPlay: () -> Unit) {
    val c = LocalWgColors.current
    val heroTiles = listOf(
        "ㅇ" to LetterTileState.Correct,
        "ㅓ" to LetterTileState.Present,
        "ㄴ" to LetterTileState.Filled,
        "ㅏ" to LetterTileState.Absent
    )
    val onboard = listOf(
        Triple("rotate_right", "돌려서 바꿔요", "ㄱ은 ㄴ으로, ㅣ는 ㅡ로. 정해진 짝으로만 돌아갑니다."),
        Triple("swap_horiz", "자리를 바꿔요", "타일을 눌러 고른 뒤 다른 타일을 누르면 자리가 바뀝니다."),
        Triple("insights", "판정으로 좁혀요", "초록은 자리까지 맞은 자모, 금색은 자리만 다른 자모예요.")
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(c.surfacePage)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                heroTiles.forEach { (ch, st) -> LetterTile(ch, st, size = GameDim.tileMd) }
            }
            Spacer(Modifier.height(20.dp))
            Text("단어조각", style = WgType.display, color = c.textStrong)
            Spacer(Modifier.height(12.dp))
            Text("자모 타일을 돌리고 자리를 바꿔 낱말을 맞히는 ${STAGES_100.size}단계(8개 테마) 퍼즐이에요.", style = WgType.body, color = c.textMuted)

            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                onboard.forEach { (icon, title, desc) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c.accentWash),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(iconFor(icon), contentDescription = title, tint = c.pigmentRedDeep, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(title, style = WgType.subtitle, color = c.textStrong)
                            Text(desc, style = WgType.bodySm, color = c.textMuted)
                        }
                    }
                }
            }
        }

        Column(
            Modifier.padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WgButton("1단계 시작하기", onClick = onStart, tone = BtnTone.Accent, size = BtnSize.Lg, block = true)
            WgButton("놀이 방법 먼저 보기", onClick = onHowToPlay, tone = BtnTone.Quiet, size = BtnSize.Md, block = true)
        }
    }
}
