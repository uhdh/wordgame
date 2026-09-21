package com.gamehub.wordgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.ui.components.BtnSize
import com.gamehub.wordgame.ui.components.BtnTone
import com.gamehub.wordgame.ui.components.LetterTile
import com.gamehub.wordgame.ui.components.LetterTileState
import com.gamehub.wordgame.ui.components.WgButton
import com.gamehub.wordgame.ui.components.iconFor
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.WgType

internal val rotRules = listOf(
    listOf("ㄱ", "ㄴ"),
    listOf("ㅣ", "ㅡ"),
    listOf("ㅏ", "ㅜ", "ㅓ", "ㅗ"),
    listOf("ㅑ", "ㅠ", "ㅕ", "ㅛ")
)
internal val combos = listOf(
    Triple("ㅗ", "ㅏ", "와"), Triple("ㅜ", "ㅓ", "워"),
    Triple("ㅡ", "ㅣ", "의"), Triple("ㅏ", "ㅣ", "애")
)

/** Content of the "놀이 방법" (how-to-play) sheet — ported from screen 1f of the design. */
@Composable
fun HowToPlayContent(onDismiss: () -> Unit) {
    val c = LocalWgColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text("놀이 방법", style = WgType.titleLg, color = c.textStrong, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        Text(
            "주어진 자모 타일을 돌리고 자리를 바꿔 정답 낱말을 완성합니다. 타일은 정해진 짝으로만 회전합니다.",
            style = WgType.bodySm, color = c.textMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Text("자모 회전", style = WgType.subtitle, color = c.textStrong)
        Spacer(Modifier.height(8.dp))
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
        Spacer(Modifier.height(24.dp))

        Text("모음 결합", style = WgType.subtitle, color = c.textStrong)
        Spacer(Modifier.height(4.dp))
        Text("두 타일을 나란히 두면 이중 모음이 됩니다.", style = WgType.bodySm, color = c.textMuted)
        Spacer(Modifier.height(10.dp))
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
        Spacer(Modifier.height(24.dp))

        Text("판정 색", style = WgType.subtitle, color = c.textStrong)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("ㄱ", LetterTileState.Correct, "자리까지 맞아요"),
                Triple("ㅁ", LetterTileState.Present, "낱말에 있지만 자리가 달라요"),
                Triple("ㅅ", LetterTileState.Absent, "쓰이지 않아요")
            ).forEach { (ch, st, desc) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LetterTile(ch, st, size = 40.dp)
                    Text(desc, style = WgType.bodySm, color = c.textMuted)
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        WgButton("알겠어요", onClick = onDismiss, tone = BtnTone.Primary, size = BtnSize.Lg, block = true)
        Spacer(Modifier.height(16.dp))
    }
}
