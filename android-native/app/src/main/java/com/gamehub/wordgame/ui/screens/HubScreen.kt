package com.gamehub.wordgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.gamehub.wordgame.data.RankingUiState
import com.gamehub.wordgame.billing.canAccessTheme
import com.gamehub.wordgame.logic.STAGES_100
import com.gamehub.wordgame.logic.STAGE_SETS
import com.gamehub.wordgame.logic.getStageSetForIndex
import com.gamehub.wordgame.ui.components.*
import com.gamehub.wordgame.ui.theme.LocalWgColors
import com.gamehub.wordgame.ui.theme.Radius
import com.gamehub.wordgame.ui.theme.WgType

private data class HubGame(
    val name: String, val tagline: String, val icon: String, val pigment: Pigment,
    val status: PuzzleStatus, val meta: String, val playable: Boolean, val setKey: String
)

@Composable
fun HubScreen(
    score: Int,
    clearedStages: Set<Int>,
    lastStageBySet: Map<String, Int>,
    darkMode: Boolean,
    rankingState: RankingUiState,
    ownedThemes: Set<String>,
    themePrices: Map<String, String>,
    onToggleDarkMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWordGame: (String) -> Unit,
    onOpenStage: (Int) -> Unit,
    onRenameNickname: (String) -> Unit
) {
    var tab by remember { mutableStateOf("today") }
    val c = LocalWgColors.current

    val tabs = listOf(
        TabItem("today", "게임", "today"),
        TabItem("archive", "아카이브", "calendar_month"),
        TabItem("friends", "순위", "emoji_events")
    )

    Column(Modifier.fillMaxSize().background(c.surfacePage)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                "today" -> TodayContent(
                    clearedStages = clearedStages, lastStageBySet = lastStageBySet,
                    ownedThemes = ownedThemes, themePrices = themePrices,
                    darkMode = darkMode, onToggleDarkMode = onToggleDarkMode, onOpenSettings = onOpenSettings,
                    onOpenWordGame = onOpenWordGame
                )
                "archive" -> ArchiveContent(
                    clearedStages = clearedStages, onOpenStage = onOpenStage,
                    darkMode = darkMode, onToggleDarkMode = onToggleDarkMode, onOpenSettings = onOpenSettings
                )
                "friends" -> RankingContent(
                    state = rankingState, myScore = score, myCleared = clearedStages.size, onRenameNickname = onRenameNickname,
                    onToggleDarkMode = onToggleDarkMode, onOpenSettings = onOpenSettings
                )
            }
        }
        WgTabBar(value = tab, items = tabs, onSelect = { tab = it })
    }
}

@Composable
private fun TodayContent(
    clearedStages: Set<Int>, lastStageBySet: Map<String, Int>,
    ownedThemes: Set<String>, themePrices: Map<String, String>,
    darkMode: Boolean, onToggleDarkMode: () -> Unit, onOpenSettings: () -> Unit, onOpenWordGame: (String) -> Unit
) {
    val c = LocalWgColors.current
    val setPigment = mapOf("basic" to Pigment.Red, "subway" to Pigment.Indigo, "brand" to Pigment.Gold, "food" to Pigment.Green, "hard" to Pigment.Clay, "slang" to Pigment.Plum, "celebrity" to Pigment.Moss, "ott" to Pigment.Indigo, "franchise" to Pigment.Clay)
    val setIcon = mapOf("basic" to "extension", "subway" to "directions_subway", "brand" to "storefront", "food" to "restaurant", "hard" to "whatshot", "slang" to "chat_bubble", "celebrity" to "badge", "ott" to "apps", "franchise" to "storefront")
    val setTagline = mapOf(
        "basic" to "자모 타일을 돌려 낱말을 맞혀 보세요",
        "subway" to "수도권 지하철역 이름으로 낱말을 맞혀 보세요",
        "brand" to "국내외 유명 브랜드 이름을 맞혀 보세요",
        "food" to "음식, 음료, 디저트 이름을 맞혀 보세요",
        "hard" to "된소리·겹받침 등 발음이 어려운 단어를 맞혀 보세요",
        "slang" to "요즘 많이 쓰는 신조어를 맞혀 보세요",
        "celebrity" to "국내외 유명인의 이름을 맞혀 보세요",
        "ott" to "인기 OTT 영화·드라마·예능 제목을 맞혀 보세요",
        "franchise" to "익숙한 프랜차이즈 매장 이름을 맞혀 보세요"
    )
    val totalScore = STAGES_100.filter { (it.stage - 1) in clearedStages }.sumOf { it.points }
    val games = STAGE_SETS.map { set ->
        val unlocked = canAccessTheme(set.key, ownedThemes)
        val clearedInSet = clearedStages.count { it >= set.start && it < set.start + set.size }
        val scoreInSet = STAGES_100.filter { it.setKey == set.key && (it.stage - 1) in clearedStages }.sumOf { it.points }
        val localStage = (lastStageBySet[set.key] ?: set.start) - set.start + 1
        HubGame(
            set.label, setTagline[set.key] ?: "", setIcon[set.key] ?: "extension", setPigment[set.key] ?: Pigment.Red,
            status = when {
                !unlocked -> PuzzleStatus.Locked
                clearedInSet >= set.size -> PuzzleStatus.Done
                else -> PuzzleStatus.Progress
            },
            meta = if (!unlocked) themePrices[set.key] ?: "₩1,000" else if (clearedInSet >= set.size) "${set.size}단계 완주 · 승점 ${scoreInSet}점" else "${localStage}단계 · 승점 ${scoreInSet}점",
            playable = unlocked,
            setKey = set.key
        )
    }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WgIconButton("dark_mode", "다크 모드", active = darkMode, onClick = onToggleDarkMode)
                WgIconButton("settings", "설정", onClick = onOpenSettings)
            }
        }
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("단어조각", style = WgType.titleLg, color = c.textStrong)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(iconFor("emoji_events"), contentDescription = null, tint = c.textFaint, modifier = Modifier.size(16.dp))
                    Text("총 승점 ${totalScore}점 · ${clearedStages.size}/${STAGES_100.size}단계 클리어", style = WgType.bodySm, color = c.textMuted)
                }

                games.forEach { g ->
                    PuzzleCard(
                        name = g.name, tagline = g.tagline, icon = g.icon, pigment = g.pigment,
                        status = g.status, meta = g.meta,
                        onClick = { onOpenWordGame(g.setKey) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveContent(
    clearedStages: Set<Int>, onOpenStage: (Int) -> Unit,
    darkMode: Boolean, onToggleDarkMode: () -> Unit, onOpenSettings: () -> Unit
) {
    val c = LocalWgColors.current
    var setFilter by remember { mutableStateOf<String?>(null) }

    val entries = remember(clearedStages, setFilter) {
        STAGES_100.filter { (setFilter == null || it.setKey == setFilter) && (it.stage - 1) in clearedStages }
    }
    val currentSetLabel = setFilter?.let { key -> STAGE_SETS.first { it.key == key }.label } ?: "전체"

    Column(Modifier.fillMaxSize().background(c.surfacePage)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("아카이브", style = WgType.caption, color = c.textFaint)
                Text("푼 단어 모음", style = WgType.titleLg, color = c.textStrong)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                WgIconButton("dark_mode", "다크 모드", active = darkMode, onClick = onToggleDarkMode)
                WgIconButton("settings", "설정", onClick = onOpenSettings)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ArchiveFilterChip(label = "전체", selected = setFilter == null, onClick = { setFilter = null })
            STAGE_SETS.forEach { set ->
                ArchiveFilterChip(label = set.label, selected = set.key == setFilter, onClick = { setFilter = set.key })
            }
        }

        if (entries.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("아직 클리어한 단계가 없어요", style = WgType.subtitle, color = c.textMuted)
                    Text("${currentSetLabel}에서 단계를 클리어하면 여기 쌓여요.", style = WgType.bodySm, color = c.textFaint)
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { puzzle ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(c.surfaceCard)
                            .border(1.dp, c.borderHair, RoundedCornerShape(Radius.md))
                            .clickable { onOpenStage(puzzle.stage - 1) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(Radius.sm))
                                .background(c.pigmentGreenWash),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${puzzle.localStage}", style = WgType.label, color = c.pigmentGreenDeep)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(puzzle.answer, style = WgType.subtitle, color = c.textStrong)
                            Text("${puzzle.length}글자 · ${puzzle.level}", style = WgType.caption, color = c.textFaint)
                        }
                        if (setFilter == null) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(c.surfaceSunk)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(puzzle.setLabel, style = WgType.caption, color = c.textMuted)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(iconFor("chevron_right"), contentDescription = null, tint = c.textFaint)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = LocalWgColors.current
    Box(
        Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) c.ink0 else c.surfaceCard)
            .border(2.dp, if (selected) c.ink0 else c.borderHair, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = WgType.label, color = if (selected) c.textInvert else c.textBody)
    }
}
