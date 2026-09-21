package com.gamehub.wordgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.data.RankingUiState
import com.gamehub.wordgame.net.LeaderboardApi
import com.gamehub.wordgame.ui.components.GameToolbar
import com.gamehub.wordgame.ui.components.ToolbarAction
import com.gamehub.wordgame.ui.components.WgCard
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.WgType

@Composable
fun RankingContent(
    state: RankingUiState, myScore: Int, myCleared: Int, onRenameNickname: (String) -> Unit,
    onToggleDarkMode: () -> Unit, onOpenSettings: () -> Unit
) {
    val c = LocalWgColors.current
    var nickInput by remember(state.nickname) { mutableStateOf(state.nickname) }
    val focusManager = LocalFocusManager.current

    Column(Modifier.fillMaxSize().background(c.surfacePage)) {
        GameToolbar(
            title = "순위", subtitle = "단어조각",
            actions = listOf(
                ToolbarAction("dark_mode", "다크 모드", onToggleDarkMode),
                ToolbarAction("settings", "설정", onOpenSettings)
            )
        )

        Column(
            Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WgCard {
                Text("내 닉네임", style = WgType.label, color = c.textFaint)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = nickInput,
                    onValueChange = { nickInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onRenameNickname(nickInput.ifBlank { state.nickname })
                        focusManager.clearFocus()
                    }),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = c.surfacePage,
                        focusedContainerColor = c.surfacePage
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text("승점 ${myScore}점 · ${myCleared}단계까지 마쳤어요", style = WgType.caption, color = c.textFaint)
                if (state.statusMessage != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(state.statusMessage, style = WgType.caption, color = c.accent)
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.surfaceCard)
                    .border(1.dp, c.borderHair, RoundedCornerShape(10.dp))
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(c.surfaceSunk)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("순위", style = WgType.label, color = c.textMuted, modifier = Modifier.width(34.dp))
                    Text("닉네임", style = WgType.label, color = c.textMuted, modifier = Modifier.weight(1f))
                    Text("승점", style = WgType.label, color = c.textMuted, modifier = Modifier.width(56.dp))
                    Text("클리어", style = WgType.label, color = c.textMuted, modifier = Modifier.width(56.dp))
                }

                when {
                    state.loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = c.accent)
                    }
                    state.error != null -> Text(state.error, style = WgType.bodySm, color = c.textFaint, modifier = Modifier.padding(16.dp))
                    state.rows.isEmpty() -> Text("아직 등록된 랭킹 기록이 없습니다.", style = WgType.bodySm, color = c.textFaint, modifier = Modifier.padding(16.dp))
                    else -> LazyColumn {
                        itemsIndexed(state.rows) { index, row ->
                            val isMe = row.nickname == state.nickname
                            val fg = if (isMe) c.textInvert else c.textBody
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(if (isMe) c.ink0 else c.surfaceCard)
                                    .border(width = 1.dp, color = c.borderHair)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}", style = WgType.subtitle, color = fg, modifier = Modifier.width(34.dp))
                                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Text(row.nickname, style = WgType.subtitle, color = fg, maxLines = 1)
                                    if (isMe) {
                                        Spacer(Modifier.width(6.dp))
                                        Box(
                                            Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(c.accent)
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text("나", style = WgType.caption, color = androidx.compose.ui.graphics.Color.White)
                                        }
                                    }
                                }
                                Text("${row.score}", style = WgType.subtitle, color = fg, modifier = Modifier.width(56.dp))
                                Text("${row.cleared}", style = WgType.bodySm, color = fg, modifier = Modifier.width(56.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
