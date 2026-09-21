package com.gamehub.wordgame.logic

/** Ported 1:1 from src/js/wordValidator.js — tile-based Wordle-style scoring. */

enum class TileState { CORRECT, PRESENT, ABSENT }

data class TileGuessResult(
    val feedback: List<TileState>,
    val isExactMatch: Boolean,
    val green: Int,
    val yellow: Int,
    val red: Int
)

fun evaluateTileGuess(submittedTiles: List<String>, targetTiles: List<String>): TileGuessResult {
    val feedback = MutableList(submittedTiles.size) { TileState.ABSENT }
    val remainingCounts = mutableMapOf<String, Int>()
    for (t in targetTiles) remainingCounts[t] = (remainingCounts[t] ?: 0) + 1

    for (i in submittedTiles.indices) {
        if (i < targetTiles.size && submittedTiles[i] == targetTiles[i]) {
            feedback[i] = TileState.CORRECT
            remainingCounts[submittedTiles[i]] = (remainingCounts[submittedTiles[i]] ?: 0) - 1
        }
    }

    for (i in submittedTiles.indices) {
        if (feedback[i] == TileState.CORRECT) continue
        val tile = submittedTiles[i]
        val remaining = remainingCounts[tile] ?: 0
        if (remaining > 0) {
            feedback[i] = TileState.PRESENT
            remainingCounts[tile] = remaining - 1
        }
    }

    val isExactMatch = feedback.size == targetTiles.size && feedback.all { it == TileState.CORRECT }
    return TileGuessResult(
        feedback = feedback,
        isExactMatch = isExactMatch,
        green = feedback.count { it == TileState.CORRECT },
        yellow = feedback.count { it == TileState.PRESENT },
        red = feedback.count { it == TileState.ABSENT }
    )
}
