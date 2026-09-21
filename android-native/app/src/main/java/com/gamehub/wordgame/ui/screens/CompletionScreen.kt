package com.gamehub.wordgame.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.ui.components.BtnSize
import com.gamehub.wordgame.ui.components.BtnTone
import com.gamehub.wordgame.ui.components.WgButton
import com.gamehub.wordgame.ui.components.iconFor
import com.gamehub.wordgame.logic.STAGES_100
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.WgType

@Composable
fun CompletionScreen(score: Int, clearedCount: Int, onRestart: () -> Unit) {
    val c = LocalWgColors.current
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(c.surfaceInvert)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(iconFor("workspace_premium"), contentDescription = null, tint = c.pigmentGold, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(16.dp))
        Text("${STAGES_100.size} STAGES CLEARED", style = WgType.label, color = c.ink4)
        Spacer(Modifier.height(10.dp))
        Text(
            "단어조각을\n모두 맞췄어요",
            style = WgType.display.copy(fontSize = androidx.compose.ui.unit.TextUnit(36f, androidx.compose.ui.unit.TextUnitType.Sp)),
            color = c.textInvert,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = c.ink2)
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatBlock("$score", "총 승점", c)
            StatBlock("$clearedCount", "마친 단계", c)
        }
        Spacer(Modifier.height(24.dp))

        Text(
            "1단계부터 다시 시작하면 기록은 그대로 남습니다.",
            style = WgType.bodySm, color = c.ink4, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WgButton(
                "결과 공유", tone = BtnTone.Accent, size = BtnSize.Lg, block = true, iconLeft = "share",
                onClick = {
                    val text = "<단어조각> ${STAGES_100.size}단계를 모두 클리어했어요! 총 승점 $score"
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }, null))
                }
            )
            WgButton("1단계부터 다시", onClick = onRestart, tone = BtnTone.Outline, size = BtnSize.Lg, block = true)
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String, c: com.gamehub.wordgame.ui.theme.WgColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = WgType.stat, color = c.textInvert)
        Spacer(Modifier.height(4.dp))
        Text(label, style = WgType.caption, color = c.ink4)
    }
}
