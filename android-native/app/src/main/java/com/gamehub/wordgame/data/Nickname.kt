package com.gamehub.wordgame.data

import com.gamehub.wordgame.logic.STAGES_100

/** 300개 스테이지 단어 pool에서 무작위 2개를 이어 붙이고 "#번호"를 붙인 닉네임을 생성한다. */
private val NICKNAME_WORD_POOL = STAGES_100.map { it.answer }

fun randomNickname(): String {
    val word = NICKNAME_WORD_POOL.random() + NICKNAME_WORD_POOL.random()
    val num = (1..999).random()
    return "$word#$num"
}
