package com.gamehub.wordgame.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamehub.wordgame.logic.Puzzle
import com.gamehub.wordgame.logic.STAGES_100
import com.gamehub.wordgame.logic.STAGE_SETS
import com.gamehub.wordgame.logic.TileState
import com.gamehub.wordgame.logic.evaluateTileGuess
import com.gamehub.wordgame.logic.getWordSyllableSlots
import com.gamehub.wordgame.logic.isRotatable
import com.gamehub.wordgame.logic.parseTileStreamToSyllables
import com.gamehub.wordgame.logic.rotateTile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 유료 무제한 이용권 도입 전까지의 "알아서 잘" 일일 사용 한도. UI 라벨 표시에도 쓰인다. */
const val DAILY_AUTO_ARRANGE_LIMIT = 10

data class TileItem(val id: Int, val char: String)

data class GuessEntry(
    val n: Int,
    val tiles: List<String>,
    val feedback: List<TileState>,
    val word: String,
    val isExactMatch: Boolean
)

data class GameUiState(
    val loaded: Boolean = false,
    val stageIndex: Int = 0,
    val score: Int = 0,
    val clearedStages: Set<Int> = emptySet(),
    val lastStageBySet: Map<String, Int> = STAGE_SETS.associate { it.key to it.start },
    val currentPuzzle: Puzzle? = null,
    val selectedTileIndex: Int? = null,
    val activeTiles: List<TileItem> = emptyList(),
    val guesses: List<GuessEntry> = emptyList(),
    val isRoundOver: Boolean = false,
    val isGameOver: Boolean = false,
    val hintUsed: Boolean = false,
    val hintOpen: Boolean = false,
    val lastEarnedPoints: Int = 0,
    val errorMessage: String? = null,
    val autoArrangeUsedToday: Int = 0
) {
    val autoArrangeRemaining: Int
        get() = (DAILY_AUTO_ARRANGE_LIMIT - autoArrangeUsedToday).coerceAtLeast(0)

    val assembledWord: String
        get() = parseTileStreamToSyllables(activeTiles.map { it.char }).word

    val assembledSyllables: List<String>
        get() = parseTileStreamToSyllables(activeTiles.map { it.char }).syllables

    val canSubmit: Boolean
        get() = !isRoundOver && !isGameOver && currentPuzzle != null &&
            activeTiles.size == currentPuzzle.targetTiles.size

    val stageProgressPct: Float
        get() = currentPuzzle?.let { puzzle ->
            puzzle.localStage / STAGE_SETS.first { it.key == puzzle.setKey }.size.toFloat()
        } ?: 0f
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val saveStore = SaveStore(application)

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val save = saveStore.load()
            val lastStageBySet = STAGE_SETS.associate { it.key to (save.lastStageBySet[it.key] ?: it.start) }
            val usedToday = if (save.autoArrangeDate == todayKey()) save.autoArrangeCount else 0
            _state.update { it.copy(autoArrangeUsedToday = usedToday) }
            loadStage(save.stageIndex, save.score, save.clearedStages, lastStageBySet, forceReset = false, initialSave = save)
        }
    }

    private fun todayKey(): String =
        java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())

    private fun loadStage(
        index: Int,
        score: Int,
        clearedStages: Set<Int>,
        lastStageBySet: Map<String, Int>,
        forceReset: Boolean,
        initialSave: SaveData? = null
    ) {
        if (index >= STAGES_100.size) {
            _state.update { it.copy(loaded = true, isGameOver = true, stageIndex = index, score = score, clearedStages = clearedStages, lastStageBySet = lastStageBySet) }
            return
        }
        val puzzle = STAGES_100[index]
        val saved = initialSave?.savedStageState
        val newLastStageBySet = lastStageBySet + (puzzle.setKey to index)

        if (!forceReset && saved != null && saved.stageIndex == index && saved.tiles.size == puzzle.targetTiles.size) {
            _state.update {
                it.copy(
                    loaded = true,
                    stageIndex = index,
                    score = score,
                    clearedStages = clearedStages,
                    lastStageBySet = newLastStageBySet,
                    currentPuzzle = puzzle,
                    selectedTileIndex = null,
                    activeTiles = saved.tiles.mapIndexed { i, ch -> TileItem(i, ch) },
                    guesses = saved.guesses.mapIndexed { i, g -> GuessEntry(i + 1, g.tiles, g.feedback, g.word, g.isExactMatch) },
                    isRoundOver = saved.isRoundOver,
                    hintUsed = saved.hintUsed,
                    hintOpen = false,
                    errorMessage = null
                )
            }
        } else {
            val shuffled = puzzle.tiles.shuffled()
            _state.update {
                it.copy(
                    loaded = true,
                    stageIndex = index,
                    score = score,
                    clearedStages = clearedStages,
                    lastStageBySet = newLastStageBySet,
                    currentPuzzle = puzzle,
                    selectedTileIndex = null,
                    activeTiles = shuffled.mapIndexed { i, ch -> TileItem(i, ch) },
                    guesses = emptyList(),
                    isRoundOver = false,
                    hintUsed = false,
                    hintOpen = false,
                    errorMessage = null
                )
            }
        }
        persist()
    }

    fun startNewGame() {
        viewModelScope.launch {
            saveStore.clearAll()
            loadStage(0, 0, emptySet(), STAGE_SETS.associate { it.key to it.start }, forceReset = true)
        }
    }

    fun goToStage(index: Int, forceReset: Boolean = false) {
        loadStage(index, _state.value.score, _state.value.clearedStages, _state.value.lastStageBySet, forceReset = forceReset)
    }

    fun nextRound() {
        val s = _state.value
        loadStage(s.stageIndex + 1, s.score, s.clearedStages, s.lastStageBySet, forceReset = true)
    }

    fun toggleHint() {
        _state.update { s ->
            val opening = !s.hintOpen
            s.copy(hintOpen = opening, hintUsed = s.hintUsed || opening)
        }
        persist()
    }

    fun selectTile(index: Int) {
        val s = _state.value
        val sel = s.selectedTileIndex
        when {
            sel == null -> _state.update { it.copy(selectedTileIndex = index, errorMessage = null) }
            sel == index -> _state.update { it.copy(selectedTileIndex = null) }
            else -> moveTile(sel, index)
        }
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        _state.update { s ->
            if (fromIndex !in s.activeTiles.indices || toIndex !in s.activeTiles.indices || fromIndex == toIndex) return@update s
            val tiles = s.activeTiles.toMutableList()
            val item = tiles.removeAt(fromIndex)
            tiles.add(toIndex, item)
            s.copy(activeTiles = tiles, selectedTileIndex = null, errorMessage = null)
        }
        persist()
    }

    fun rotateTileAt(index: Int) {
        _state.update { s ->
            if (index !in s.activeTiles.indices) return@update s
            val tile = s.activeTiles[index]
            if (!isRotatable(tile.char)) return@update s
            val tiles = s.activeTiles.toMutableList()
            tiles[index] = tile.copy(char = rotateTile(tile.char))
            s.copy(activeTiles = tiles, errorMessage = null)
        }
        persist()
    }

    /** 음절 청크 단위로 셔플해서, 항상 정답 글자 수와 일치하는 완전한 음절로 조합되도록 한다.
     *  슬롯이 타일 1개뿐인 자모만 회전을 무작위로 섞는다 — 겹모음/쌍자음처럼 타일 2개 이상이
     *  결합해야 하는 슬롯을 회전시키면 combineTiles()가 조합을 인식하지 못해 타일이 남는다. */
    private fun shuffledTilesFor(puzzle: Puzzle): List<String> =
        getWordSyllableSlots(puzzle.answer).shuffled().flatMap { slots ->
            slots.flatMap { slot ->
                val tile = slot.singleOrNull()
                if (tile != null && isRotatable(tile)) {
                    var rotated: String = tile
                    repeat(kotlin.random.Random.nextInt(4)) { rotated = rotateTile(rotated) }
                    listOf(rotated)
                } else slot
            }
        }

    /** 정답 글자 수에 맞는 자모 타일 세트를 새로 무작위 배치한다(회전 상태도 무작위 재배치).
     *  하루 [DAILY_AUTO_ARRANGE_LIMIT]회로 제한 — 한도 소진 시 아무 동작도 하지 않는다. */
    fun autoArrangeTiles() {
        val s = _state.value
        if (s.autoArrangeRemaining <= 0) return
        val puzzle = s.currentPuzzle ?: return
        val shuffled = shuffledTilesFor(puzzle)
        val usedToday = s.autoArrangeUsedToday + 1
        _state.update {
            it.copy(
                activeTiles = shuffled.mapIndexed { i, ch -> TileItem(i, ch) },
                selectedTileIndex = null,
                errorMessage = null,
                autoArrangeUsedToday = usedToday
            )
        }
        persist()
        val date = todayKey()
        viewModelScope.launch { saveStore.saveAutoArrangeUsage(date, usedToday) }
    }

    /** 정답 글자 수와 무관하게 지금 보유한 타일 순서만 무작위로 섞는다(횟수 제한 없는 기본 기능). */
    fun shuffleTiles() {
        _state.update { s ->
            s.copy(activeTiles = s.activeTiles.shuffled(), selectedTileIndex = null, errorMessage = null)
        }
        persist()
    }

    fun submitGuess(): GuessEntry? {
        val s = _state.value
        val puzzle = s.currentPuzzle ?: return null
        if (!s.canSubmit) return null

        val submittedTiles = s.activeTiles.map { it.char }
        val result = evaluateTileGuess(submittedTiles, puzzle.targetTiles)
        val assembled = parseTileStreamToSyllables(submittedTiles)
        val entry = GuessEntry(
            n = s.guesses.size + 1,
            tiles = submittedTiles,
            feedback = result.feedback,
            word = assembled.word,
            isExactMatch = result.isExactMatch
        )

        var earnedPoints = 0
        val newGuesses = s.guesses + entry

        if (result.isExactMatch) {
            val hintPenalty = if (s.hintUsed) 1 else 0
            val attemptPenalty = if (newGuesses.size > 5) 1 else 0
            earnedPoints = maxOf(1, puzzle.points - hintPenalty - attemptPenalty)
            val newClearedStages = s.clearedStages + s.stageIndex
            val newScore = s.score + earnedPoints
            val isGameOver = s.stageIndex >= STAGES_100.size - 1

            _state.update {
                it.copy(
                    guesses = newGuesses,
                    isRoundOver = true,
                    isGameOver = isGameOver,
                    lastEarnedPoints = earnedPoints,
                    score = newScore,
                    clearedStages = newClearedStages,
                    errorMessage = null
                )
            }
        } else {
            _state.update { it.copy(guesses = newGuesses, errorMessage = "아직 아니에요") }
        }
        persist()
        return entry
    }

    private fun persist() {
        val s = _state.value
        viewModelScope.launch {
            val savedStageState = if (s.currentPuzzle != null && !s.isGameOver) {
                SavedStageState(
                    stageIndex = s.stageIndex,
                    tiles = s.activeTiles.map { it.char },
                    guesses = s.guesses.map { SavedGuess(it.tiles, it.feedback, it.word, it.isExactMatch) },
                    isRoundOver = s.isRoundOver,
                    hintUsed = s.hintUsed
                )
            } else null
            saveStore.save(s.stageIndex, s.score, s.clearedStages, savedStageState, s.lastStageBySet)
        }
    }
}
