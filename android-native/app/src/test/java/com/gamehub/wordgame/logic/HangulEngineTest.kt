package com.gamehub.wordgame.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HangulEngineTest {
    @Test
    fun `rotate consonant and vowel matches JS mapping`() {
        assertEquals("ㄴ", rotateTile("ㄱ"))
        assertEquals("ㄱ", rotateTile("ㄴ"))
        assertEquals("ㅜ", rotateTile("ㅏ"))
        assertEquals("ㅏ", rotateTile("ㅗ"))
        assertEquals("ㅗ", rotateTile("ㅏ", reverse = true))
    }

    @Test
    fun `decompose and recompose 금메달 round-trips`() {
        val d = decomposeHangul("금")
        assertEquals("ㄱ", d.cho)
        assertEquals("ㅡ", d.jung)
        assertEquals("ㅁ", d.jong)
        assertEquals("금", composeHangul(d.cho, d.jung, d.jong))
        assertEquals("ㄱ ㅁ ㄷ", getWordChosungHint("금메달"))
    }

    @Test
    fun `decompose word to target tiles then parse back to same word`() {
        val target = decomposeWordToTargetTiles("금메달")
        val parsed = parseTileStreamToSyllables(target)
        assertEquals("금메달", parsed.word)
    }

    @Test
    fun `vowel combination assembles compound vowels`() {
        assertEquals("와", composeHangul("ㅇ", combineTiles(listOf("ㅗ", "ㅏ"))!!))
        assertEquals("의", composeHangul("ㅇ", combineTiles(listOf("ㅡ", "ㅣ"))!!))
    }

    @Test
    fun `stage table follows reviewed set sizes with matching tile pools`() {
        assertEquals(641, STAGES_100.size)
        assertEquals(listOf(100, 100, 93, 92, 90, 37, 88, 41), STAGE_SETS.map { it.size })
        val stage1 = STAGES_100.first()
        assertEquals("무지개", stage1.answer)
        assertEquals("basic", stage1.setKey)
        assertTrue(stage1.tiles.size >= stage1.targetTiles.toSet().size)

        val stage101 = STAGES_100[100]
        assertEquals("강남", stage101.answer)
        assertEquals("subway", stage101.setKey)
        assertEquals(1, stage101.localStage)

        STAGE_SETS.forEach { set ->
            assertEquals(set.key, STAGES_100[set.start].setKey)
            assertEquals(1, STAGES_100[set.start].localStage)
        }

        assertTrue(STAGES_100.any { it.answer == "유재석" && it.setKey == "celebrity" })
        assertTrue(STAGES_100.any { it.answer == "지금불륜이문제가아닙니다" && it.setKey == "ott" })
        assertTrue(STAGES_100.none { it.answer == "티몬" })
        assertEquals("두부", STAGES_100.first { it.setKey == "food" }.answer)
        assertTrue(STAGES_100.indexOfFirst { it.answer == "애플" } > STAGES_100.indexOfFirst { it.answer == "헤라" })
    }

    @Test
    fun `no duplicate answers across all stages`() {
        val answers = STAGES_100.map { it.answer }
        assertEquals(answers.size, answers.toSet().size)
    }
}

class WordValidatorTest {
    @Test
    fun `exact match marks every tile correct`() {
        val target = listOf("ㄱ", "ㅡ", "ㅁ")
        val result = evaluateTileGuess(target, target)
        assertTrue(result.isExactMatch)
        assertTrue(result.feedback.all { it == TileState.CORRECT })
    }

    @Test
    fun `present tiles counted by remaining frequency not total`() {
        // target has one ㄱ; guess has two ㄱ (both mispositioned) — only one may be present, the other absent
        val target = listOf("ㄱ", "ㅏ", "ㄴ")
        val guess = listOf("ㄴ", "ㄱ", "ㄱ")
        val result = evaluateTileGuess(guess, target)
        assertEquals(TileState.PRESENT, result.feedback[0])
        assertEquals(TileState.PRESENT, result.feedback[1])
        assertEquals(TileState.ABSENT, result.feedback[2])
    }
}
