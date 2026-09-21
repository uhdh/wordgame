package com.gamehub.wordgame.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.data.Settings
import com.gamehub.wordgame.ui.components.iconFor
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.WgType
import com.gamehub.wordgame.ui.components.GameToolbar

@Composable
fun SettingsScreen(
    settings: Settings,
    nickname: String,
    onBack: () -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onHapticChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onResetProgress: () -> Unit,
    onOpenHowToPlay: () -> Unit
) {
    val c = LocalWgColors.current
    val context = LocalContext.current
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(c.surfacePage)) {
        GameToolbar(title = "설정", onBack = onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsGroup("놀이") {
                SwitchRow("volume_up", "효과음", "타일과 판정에 소리를 냅니다", settings.soundOn, onSoundChange)
                SwitchRow("vibration", "햅틱", "타일을 누를 때 짧게 울립니다", settings.hapticOn, onHapticChange)
                NavRow("help", "플레이 방법", "예시로 알아보는 놀이 방법") { onOpenHowToPlay() }
            }
            SettingsGroup("화면") {
                SwitchRow("dark_mode", "다크 모드", "", settings.darkMode, onDarkModeChange)
            }
            SettingsGroup("계정과 데이터") {
                ValueRow("badge", "닉네임", nickname)
                NavRow("delete", "기록 초기화", "승점과 마친 단계를 모두 지웁니다") { showResetConfirm = true }
                NavRow("policy", "개인정보 처리방침", null) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://pgamex.vercel.app/privacy.html")))
                }
            }

            Text(
                "단어조각 1.0.0 · 안드로이드",
                style = WgType.caption, color = c.textFaint,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("기록을 초기화할까요?") },
            text = { Text("승점과 마친 단계가 모두 사라지고 1단계부터 다시 시작합니다.") },
            confirmButton = {
                Text(
                    "초기화",
                    color = c.stateError,
                    modifier = Modifier
                        .clickable { showResetConfirm = false; onResetProgress() }
                        .padding(12.dp)
                )
            },
            dismissButton = {
                Text("취소", modifier = Modifier.clickable { showResetConfirm = false }.padding(12.dp))
            }
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val c = LocalWgColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = WgType.label, color = c.textFaint, modifier = Modifier.padding(start = 4.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(c.surfaceCard)
                .border(1.dp, c.borderHair, RoundedCornerShape(10.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun RowScaffold(icon: String, label: String, desc: String?, trailing: @Composable () -> Unit) {
    val c = LocalWgColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(iconFor(icon), contentDescription = label, tint = c.textMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = WgType.subtitle, color = c.textStrong)
            if (!desc.isNullOrEmpty()) {
                Text(desc, style = WgType.caption, color = c.textFaint)
            }
        }
        trailing()
    }
}

@Composable
private fun SwitchRow(icon: String, label: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = LocalWgColors.current
    RowScaffold(icon, label, desc) {
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = c.ink0, checkedThumbColor = c.surfaceCard)
        )
    }
}

@Composable
private fun ValueRow(icon: String, label: String, value: String) {
    val c = LocalWgColors.current
    RowScaffold(icon, label, null) {
        Text(value, style = WgType.bodySm, color = c.textMuted)
    }
}

@Composable
private fun NavRow(icon: String, label: String, desc: String?, onClick: () -> Unit) {
    val c = LocalWgColors.current
    Row(Modifier.fillMaxWidth().clickable { onClick() }) {
        RowScaffold(icon, label, desc) {
            Icon(iconFor("chevron_right"), contentDescription = null, tint = c.textFaint)
        }
    }
}
