package com.gamehub.wordgame

import android.content.Intent
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gamehub.wordgame.data.GameViewModel
import com.gamehub.wordgame.data.RankingViewModel
import com.gamehub.wordgame.data.SettingsViewModel
import com.gamehub.wordgame.billing.BillingViewModel
import com.gamehub.wordgame.billing.canAccessTheme
import com.gamehub.wordgame.logic.STAGES_100
import com.gamehub.wordgame.logic.STAGE_SETS
import com.gamehub.wordgame.logic.SoundEngine
import com.gamehub.wordgame.logic.TileState
import com.gamehub.wordgame.ui.screens.*
import com.gamehub.wordgame.ui.theme.WordGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val settings by settingsVm.settings.collectAsState()
            WordGameTheme(darkTheme = settings.darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
                        WordGameApp()
                    }
                }
            }
        }
    }
}

@Composable
private fun WordGameApp() {
    val context = LocalContext.current
    val nav: NavHostController = rememberNavController()

    val settingsVm: SettingsViewModel = viewModel()
    val gameVm: GameViewModel = viewModel()
    val rankingVm: RankingViewModel = viewModel()
    val billingVm: BillingViewModel = viewModel()

    val settings by settingsVm.settings.collectAsState()
    val onboarded by settingsVm.onboarded.collectAsState()
    val gameState by gameVm.state.collectAsState()
    val rankingState by rankingVm.state.collectAsState()
    val billingState by billingVm.state.collectAsState()

    var purchaseTarget by remember { mutableStateOf<String?>(null) }
    var pendingStage by remember { mutableStateOf<Int?>(null) }
    var pendingForceReset by remember { mutableStateOf(false) }

    fun openStage(index: Int, forceReset: Boolean = false) {
        val puzzle = STAGES_100.getOrNull(index) ?: return
        if (!canAccessTheme(puzzle.setKey, billingState.ownedThemes)) {
            purchaseTarget = puzzle.setKey
            pendingStage = index
            pendingForceReset = forceReset
            return
        }
        gameVm.goToStage(index, forceReset)
        if (nav.currentDestination?.route != "game") nav.navigate("game")
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) billingVm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(billingState.ownedThemes, pendingStage) {
        val index = pendingStage ?: return@LaunchedEffect
        val puzzle = STAGES_100.getOrNull(index) ?: return@LaunchedEffect
        if (canAccessTheme(puzzle.setKey, billingState.ownedThemes)) {
            pendingStage = null
            gameVm.goToStage(index, pendingForceReset)
            pendingForceReset = false
            if (nav.currentDestination?.route != "game") nav.navigate("game")
        }
    }

    LaunchedEffect(billingState.message) {
        val message = billingState.message ?: return@LaunchedEffect
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        billingVm.clearMessage()
    }

    val sound = remember { SoundEngine() }
    LaunchedEffect(settings.soundOn) { sound.muted = !settings.soundOn }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    fun haptic(ms: Long = 12) {
        if (!settings.hapticOn) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // Play flip sounds for the newest guess's feedback the moment it lands.
    LaunchedEffect(gameState.guesses.size) {
        val last = gameState.guesses.lastOrNull() ?: return@LaunchedEffect
        if (last.isExactMatch) sound.playWin() else {
            last.feedback.forEachIndexed { i, st -> sound.playFlip(st, i) }
            if (!last.isExactMatch) sound.playTick()
        }
    }

    // Wait for onboarding/save-data to load before picking a start destination.
    if (onboarded == null || !gameState.loaded) return

    val startDestination = if (onboarded == true) "hub" else "onboarding"

    NavHost(navController = nav, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingSheetHost(onDone = {
                settingsVm.setOnboarded()
                nav.navigate("hub") { popUpTo("onboarding") { inclusive = true } }
            })
        }

        composable("hub") {
            LaunchedEffect(Unit) { rankingVm.refresh(gameState.score, gameState.clearedStages.size) }
            HubScreen(
                score = gameState.score,
                clearedStages = gameState.clearedStages,
                lastStageBySet = gameState.lastStageBySet,
                darkMode = settings.darkMode,
                rankingState = rankingState,
                ownedThemes = billingState.ownedThemes,
                themePrices = billingState.prices,
                onToggleDarkMode = { settingsVm.setDarkMode(!settings.darkMode) },
                onOpenSettings = { nav.navigate("settings") },
                onOpenWordGame = { setKey ->
                    if (gameState.isGameOver) {
                        nav.navigate("completion")
                    } else {
                        val target = gameState.lastStageBySet[setKey]
                            ?: STAGE_SETS.first { it.key == setKey }.start
                        openStage(target)
                    }
                },
                onOpenStage = { idx -> openStage(idx) },
                onRenameNickname = { name -> rankingVm.changeNickname(name, gameState.score, gameState.clearedStages.size) }
            )
        }

        composable("stage_select") {
            StageSelectScreen(
                clearedStages = gameState.clearedStages,
                currentStage = gameState.stageIndex,
                ownedThemes = billingState.ownedThemes,
                onBack = { nav.popBackStack() },
                onSelectStage = { idx ->
                    openStage(idx)
                    if (canAccessTheme(STAGES_100[idx].setKey, billingState.ownedThemes)) nav.popBackStack()
                },
                onRequestTheme = { setKey ->
                    purchaseTarget = setKey
                    pendingStage = STAGE_SETS.first { it.key == setKey }.start
                }
            )
        }

        composable("game") {
            if (gameState.isGameOver) {
                CompletionScreen(score = gameState.score, clearedCount = gameState.clearedStages.size, onRestart = {
                    gameVm.startNewGame()
                    nav.navigate("hub") { popUpTo("hub") { inclusive = true } }
                })
            } else {
                GameScreen(
                    state = gameState,
                    soundOn = settings.soundOn,
                    onBack = { nav.popBackStack() },
                    onOpenStageSelect = { nav.navigate("stage_select") },
                    onShareStage = {
                        val puzzle = gameState.currentPuzzle
                        val text = if (puzzle != null) "<단어조각> ${puzzle.setLabel} ${puzzle.localStage}단계 (${puzzle.length}글자 퍼즐)에 도전해보세요!" else "단어조각을 플레이해보세요!"
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
                        }, null))
                    },
                    onToggleSound = { settingsVm.setSoundOn(!settings.soundOn) },
                    onTileTap = { i -> haptic(15); sound.playTileClick(); gameVm.selectTile(i) },
                    onTileMove = { from, to -> haptic(15); sound.playTileClick(); gameVm.moveTile(from, to) },
                    onTileRotate = { i -> haptic(15); sound.playTileRotate(); gameVm.rotateTileAt(i) },
                    onShuffle = { haptic(10); sound.playTileClick(); gameVm.shuffleTiles() },
                    onAutoArrange = { haptic(10); sound.playTileClick(); gameVm.autoArrangeTiles() },
                    onSubmit = {
                        val entry = gameVm.submitGuess()
                        if (entry != null && !entry.isExactMatch) sound.playError()
                    },
                    onToggleHint = { haptic(10); sound.playTileClick(); gameVm.toggleHint() },
                    onNextStage = {
                        val nextIndex = gameState.stageIndex + 1
                        if (nextIndex >= STAGES_100.size) {
                            nav.navigate("completion") { popUpTo("game") { inclusive = true } }
                        } else {
                            openStage(nextIndex, forceReset = true)
                        }
                    },
                    onOpenRanking = { nav.navigate("hub") { popUpTo("hub") { inclusive = true } } },
                    onRandomStage = { idx -> haptic(15); sound.playTileClick(); openStage(idx, forceReset = true) },
                    onOpenHowToPlay = { nav.navigate("how_to_play") }
                )
            }
        }

        composable("completion") {
            CompletionScreen(score = gameState.score, clearedCount = gameState.clearedStages.size, onRestart = {
                gameVm.startNewGame()
                nav.navigate("hub") { popUpTo("hub") { inclusive = true } }
            })
        }

        composable("settings") {
            SettingsScreen(
                settings = settings,
                nickname = settings.nickname,
                onBack = { nav.popBackStack() },
                onSoundChange = settingsVm::setSoundOn,
                onHapticChange = settingsVm::setHapticOn,
                onDarkModeChange = settingsVm::setDarkMode,
                onResetProgress = { gameVm.startNewGame() },
                onOpenHowToPlay = { nav.navigate("how_to_play") }
            )
        }

        composable("how_to_play") {
            HowToPlayScreen(onBack = { nav.popBackStack() })
        }
    }

    purchaseTarget?.let { setKey ->
        val set = STAGE_SETS.first { it.key == setKey }
        val pending = setKey in billingState.pendingThemes
        val price = billingState.prices[setKey] ?: "₩1,000"
        AlertDialog(
            onDismissRequest = {
                purchaseTarget = null
                pendingStage = null
                pendingForceReset = false
            },
            title = { Text("${set.label} 테마 해금") },
            text = {
                Text(if (pending) "결제 처리 중입니다. 결제가 완료되면 자동으로 열립니다." else "${price}에 ${set.label} 테마를 영구 해금합니다.")
            },
            confirmButton = {
                TextButton(
                    enabled = !pending,
                    onClick = {
                        val activity = context as? Activity
                        if (activity != null) billingVm.purchase(activity, setKey)
                        purchaseTarget = null
                    }
                ) { Text(if (pending) "처리 중" else "$price 구매") }
            },
            dismissButton = {
                TextButton(onClick = {
                    purchaseTarget = null
                    pendingStage = null
                    pendingForceReset = false
                }) { Text("취소") }
            }
        )
    }
}

/** Wraps OnboardingScreen so "놀이 방법 먼저 보기" can show the how-to-play sheet inline. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingSheetHost(onDone: () -> Unit) {
    var showHowTo by remember { androidx.compose.runtime.mutableStateOf(false) }
    OnboardingScreen(onStart = onDone, onHowToPlay = { showHowTo = true })
    if (showHowTo) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        androidx.compose.material3.ModalBottomSheet(onDismissRequest = { showHowTo = false }, sheetState = sheetState) {
            HowToPlayContent(onDismiss = { showHowTo = false })
        }
    }
}
