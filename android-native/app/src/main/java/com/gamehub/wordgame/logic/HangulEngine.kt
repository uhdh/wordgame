package com.gamehub.wordgame.logic

/**
 * Ported 1:1 from the web game's src/js/hangulEngine.js so puzzle behavior matches exactly.
 */

val CHOSUNG = listOf(
    "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ",
    "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
)

val JUNGSUNG = listOf(
    "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ",
    "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"
)

val JONGSUNG = listOf(
    "", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ",
    "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ",
    "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
)

val ROTATABLE_TILES = setOf(
    "ㄱ", "ㄴ",
    "ㅏ", "ㅓ", "ㅗ", "ㅜ",
    "ㅑ", "ㅕ", "ㅛ", "ㅠ",
    "ㅣ", "ㅡ"
)

fun isRotatable(tile: String) = tile in ROTATABLE_TILES

val CONSONANT_ROTATIONS = mapOf("ㄱ" to "ㄴ", "ㄴ" to "ㄱ")

// Clockwise: 3시 -> 6시 -> 9시 -> 12시
val VOWEL_ROTATIONS = mapOf(
    "ㅏ" to "ㅜ", "ㅜ" to "ㅓ", "ㅓ" to "ㅗ", "ㅗ" to "ㅏ",
    "ㅑ" to "ㅠ", "ㅠ" to "ㅕ", "ㅕ" to "ㅛ", "ㅛ" to "ㅑ",
    "ㅣ" to "ㅡ", "ㅡ" to "ㅣ"
)

val VOWEL_REVERSE_ROTATIONS = mapOf(
    "ㅏ" to "ㅗ", "ㅗ" to "ㅓ", "ㅓ" to "ㅜ", "ㅜ" to "ㅏ",
    "ㅑ" to "ㅛ", "ㅛ" to "ㅕ", "ㅕ" to "ㅠ", "ㅠ" to "ㅑ",
    "ㅣ" to "ㅡ", "ㅡ" to "ㅣ"
)

val CONSONANT_COMBINATIONS = mapOf(
    "ㄱ+ㄱ" to "ㄲ", "ㄷ+ㄷ" to "ㄸ", "ㅂ+ㅂ" to "ㅃ", "ㅅ+ㅅ" to "ㅆ", "ㅈ+ㅈ" to "ㅉ",
    "ㄱ+ㅅ" to "ㄳ", "ㄴ+ㅈ" to "ㄵ", "ㄴ+ㅎ" to "ㄶ", "ㄹ+ㄱ" to "ㄺ", "ㄹ+ㅁ" to "ㄻ",
    "ㄹ+ㅂ" to "ㄼ", "ㄹ+ㅅ" to "ㄽ", "ㄹ+ㅌ" to "ㄾ", "ㄹ+ㅍ" to "ㄿ", "ㄹ+ㅎ" to "ㅀ",
    "ㅂ+ㅅ" to "ㅄ"
)

val VOWEL_COMBINATIONS = mapOf(
    "ㅏ+ㅣ" to "ㅐ", "ㅓ+ㅣ" to "ㅔ", "ㅗ+ㅣ" to "ㅚ", "ㅜ+ㅣ" to "ㅟ",
    "ㅑ+ㅣ" to "ㅒ", "ㅕ+ㅣ" to "ㅖ", "ㅗ+ㅏ" to "ㅘ", "ㅜ+ㅓ" to "ㅝ",
    "ㅗ+ㅐ" to "ㅙ", "ㅘ+ㅣ" to "ㅙ", "ㅗ+ㅏ+ㅣ" to "ㅙ",
    "ㅜ+ㅔ" to "ㅞ", "ㅝ+ㅣ" to "ㅞ", "ㅜ+ㅓ+ㅣ" to "ㅞ",
    "ㅡ+ㅣ" to "ㅢ"
)

fun rotateTile(tile: String, reverse: Boolean = false): String {
    CONSONANT_ROTATIONS[tile]?.let { return it }
    if (reverse) VOWEL_REVERSE_ROTATIONS[tile]?.let { return it }
    VOWEL_ROTATIONS[tile]?.let { return it }
    return tile
}

fun combineTiles(tiles: List<String>): String? {
    if (tiles.size < 2) return null
    val key2 = "${tiles[0]}+${tiles[1]}"
    return when (tiles.size) {
        2 -> CONSONANT_COMBINATIONS[key2] ?: VOWEL_COMBINATIONS[key2]
        3 -> VOWEL_COMBINATIONS["${tiles[0]}+${tiles[1]}+${tiles[2]}"]
        else -> null
    }
}

fun isConsonant(char: String) = char in CHOSUNG || char in JONGSUNG
fun isVowel(char: String) = char in JUNGSUNG

data class Decomposed(val cho: String, val jung: String, val jong: String, val isHangul: Boolean)

fun decomposeHangul(char: String): Decomposed {
    if (char.length != 1) return Decomposed("", "", "", false)
    val code = char[0].code - 0xAC00
    if (code < 0 || code > 11171) return Decomposed(char, "", "", false)
    val choIdx = code / 588
    val jungIdx = (code % 588) / 28
    val jongIdx = code % 28
    return Decomposed(CHOSUNG[choIdx], JUNGSUNG[jungIdx], JONGSUNG[jongIdx], true)
}

fun getWordChosungHint(word: String): String =
    word.map { ch ->
        val d = decomposeHangul(ch.toString())
        if (d.isHangul) d.cho else ch.toString()
    }.joinToString(" ")

fun composeHangul(cho: String, jung: String, jong: String = ""): String {
    val choIdx = CHOSUNG.indexOf(cho)
    val jungIdx = JUNGSUNG.indexOf(jung)
    val jongIdx = JONGSUNG.indexOf(jong)
    if (choIdx == -1 || jungIdx == -1 || jongIdx == -1) return cho.ifEmpty { jung.ifEmpty { jong } }
    val code = 0xAC00 + (choIdx * 588) + (jungIdx * 28) + jongIdx
    return code.toChar().toString()
}

fun decomposeWordToTargetTiles(word: String): List<String> {
    val out = mutableListOf<String>()
    for (ch in word) {
        val d = decomposeHangul(ch.toString())
        if (!d.isHangul) continue

        when (d.cho) {
            "ㄲ" -> out.addAll(listOf("ㄱ", "ㄱ"))
            "ㄸ" -> out.addAll(listOf("ㄷ", "ㄷ"))
            "ㅃ" -> out.addAll(listOf("ㅂ", "ㅂ"))
            "ㅆ" -> out.addAll(listOf("ㅅ", "ㅅ"))
            "ㅉ" -> out.addAll(listOf("ㅈ", "ㅈ"))
            else -> out.add(d.cho)
        }

        when (d.jung) {
            "ㅐ" -> out.addAll(listOf("ㅏ", "ㅣ"))
            "ㅔ" -> out.addAll(listOf("ㅓ", "ㅣ"))
            "ㅚ" -> out.addAll(listOf("ㅗ", "ㅣ"))
            "ㅟ" -> out.addAll(listOf("ㅜ", "ㅣ"))
            "ㅒ" -> out.addAll(listOf("ㅑ", "ㅣ"))
            "ㅖ" -> out.addAll(listOf("ㅕ", "ㅣ"))
            "ㅘ" -> out.addAll(listOf("ㅗ", "ㅏ"))
            "ㅝ" -> out.addAll(listOf("ㅜ", "ㅓ"))
            "ㅙ" -> out.addAll(listOf("ㅗ", "ㅏ", "ㅣ"))
            "ㅞ" -> out.addAll(listOf("ㅜ", "ㅓ", "ㅣ"))
            "ㅢ" -> out.addAll(listOf("ㅡ", "ㅣ"))
            else -> out.add(d.jung)
        }

        if (d.jong.isNotEmpty()) {
            when (d.jong) {
                "ㄲ" -> out.addAll(listOf("ㄱ", "ㄱ"))
                "ㄳ" -> out.addAll(listOf("ㄱ", "ㅅ"))
                "ㄵ" -> out.addAll(listOf("ㄴ", "ㅈ"))
                "ㄶ" -> out.addAll(listOf("ㄴ", "ㅎ"))
                "ㄺ" -> out.addAll(listOf("ㄹ", "ㄱ"))
                "ㄻ" -> out.addAll(listOf("ㄹ", "ㅁ"))
                "ㄼ" -> out.addAll(listOf("ㄹ", "ㅂ"))
                "ㄽ" -> out.addAll(listOf("ㄹ", "ㅅ"))
                "ㄾ" -> out.addAll(listOf("ㄹ", "ㅌ"))
                "ㄿ" -> out.addAll(listOf("ㄹ", "ㅍ"))
                "ㅀ" -> out.addAll(listOf("ㄹ", "ㅎ"))
                "ㅄ" -> out.addAll(listOf("ㅂ", "ㅅ"))
                "ㅆ" -> out.addAll(listOf("ㅅ", "ㅅ"))
                else -> out.add(d.jong)
            }
        }
    }
    return out
}

data class ParsedTiles(val syllables: List<String>, val word: String, val mapToTiles: List<List<Int>>)

fun parseTileStreamToSyllables(tiles: List<String>): ParsedTiles {
    if (tiles.isEmpty()) return ParsedTiles(emptyList(), "", emptyList())

    val syllables = mutableListOf<String>()
    val mapToTiles = mutableListOf<List<Int>>()
    var i = 0

    while (i < tiles.size) {
        val t1 = tiles[i]

        if (isConsonant(t1)) {
            var cho = t1
            val choIndices = mutableListOf(i)
            var nextIdx = i + 1

            if (nextIdx < tiles.size && isConsonant(tiles[nextIdx])) {
                val doubleCons = combineTiles(listOf(cho, tiles[nextIdx]))
                if (doubleCons != null && doubleCons in CHOSUNG) {
                    if (nextIdx + 1 < tiles.size && isVowel(tiles[nextIdx + 1])) {
                        cho = doubleCons
                        choIndices.add(nextIdx)
                        nextIdx++
                    }
                }
            }

            if (nextIdx < tiles.size && isVowel(tiles[nextIdx])) {
                var jung = tiles[nextIdx]
                val jungIndices = mutableListOf(nextIdx)
                nextIdx++

                while (nextIdx < tiles.size && isVowel(tiles[nextIdx])) {
                    val combo = combineTiles(listOf(jung, tiles[nextIdx]))
                    if (combo != null && combo in JUNGSUNG) {
                        jung = combo
                        jungIndices.add(nextIdx)
                        nextIdx++
                    } else break
                }

                var jong = ""
                var jongIndices = listOf<Int>()

                if (nextIdx < tiles.size && isConsonant(tiles[nextIdx])) {
                    val cons1 = tiles[nextIdx]
                    val comboNext = if (nextIdx + 1 < tiles.size && isConsonant(tiles[nextIdx + 1]))
                        combineTiles(listOf(cons1, tiles[nextIdx + 1])) else null
                    val hasVowelAfter =
                        (nextIdx + 1 < tiles.size && isVowel(tiles[nextIdx + 1])) ||
                            (nextIdx + 2 < tiles.size && isConsonant(tiles[nextIdx + 1]) &&
                                isVowel(tiles[nextIdx + 2]) && comboNext != null && comboNext in CHOSUNG)

                    if (!hasVowelAfter) {
                        jong = cons1
                        val jongList = mutableListOf(nextIdx)
                        nextIdx++

                        if (nextIdx < tiles.size && isConsonant(tiles[nextIdx])) {
                            val hasVowelAfter2 = nextIdx + 1 < tiles.size && isVowel(tiles[nextIdx + 1])
                            if (!hasVowelAfter2) {
                                val complexJong = combineTiles(listOf(jong, tiles[nextIdx]))
                                if (complexJong != null && complexJong in JONGSUNG) {
                                    jong = complexJong
                                    jongList.add(nextIdx)
                                    nextIdx++
                                }
                            }
                        }
                        jongIndices = jongList
                    }
                }

                val syllable = composeHangul(cho, jung, jong)
                syllables.add(syllable)
                mapToTiles.add(choIndices + jungIndices + jongIndices)
                i = nextIdx
                continue
            } else {
                syllables.add(cho)
                mapToTiles.add(choIndices)
                i = nextIdx
                continue
            }
        } else {
            var jung = t1
            val jungIndices = mutableListOf(i)
            var nextIdx = i + 1
            while (nextIdx < tiles.size && isVowel(tiles[nextIdx])) {
                val combo = combineTiles(listOf(jung, tiles[nextIdx]))
                if (combo != null && combo in JUNGSUNG) {
                    jung = combo
                    jungIndices.add(nextIdx)
                    nextIdx++
                } else break
            }
            syllables.add(jung)
            mapToTiles.add(jungIndices)
            i = nextIdx
        }
    }

    return ParsedTiles(syllables, syllables.joinToString(""), mapToTiles)
}

fun getVowelBaseComponents(vowel: String): List<String> = when (vowel) {
    "ㅏ", "ㅓ", "ㅗ", "ㅜ" -> listOf("ㅏ")
    "ㅣ", "ㅡ" -> listOf("ㅣ")
    "ㅑ", "ㅕ", "ㅛ", "ㅠ" -> listOf("ㅑ")
    "ㅐ", "ㅔ", "ㅚ", "ㅟ" -> listOf("ㅏ", "ㅣ")
    "ㅒ", "ㅖ" -> listOf("ㅑ", "ㅣ")
    "ㅘ", "ㅝ" -> listOf("ㅏ", "ㅏ")
    "ㅙ", "ㅞ" -> listOf("ㅏ", "ㅏ", "ㅣ")
    "ㅢ" -> listOf("ㅣ", "ㅣ")
    else -> listOf(vowel)
}

fun getConsonantBaseComponents(cons: String): List<String> = when (cons) {
    "" -> emptyList()
    "ㄱ", "ㄴ" -> listOf("ㄱ")
    "ㄲ" -> listOf("ㄱ", "ㄱ")
    "ㄳ" -> listOf("ㄱ", "ㅅ")
    "ㄵ" -> listOf("ㄱ", "ㅈ")
    "ㄶ" -> listOf("ㄱ", "ㅎ")
    "ㄷ" -> listOf("ㄷ")
    "ㄸ" -> listOf("ㄷ", "ㄷ")
    "ㄹ" -> listOf("ㄹ")
    "ㄺ" -> listOf("ㄹ", "ㄱ")
    "ㄻ" -> listOf("ㄹ", "ㅁ")
    "ㄼ" -> listOf("ㄹ", "ㅂ")
    "ㄽ" -> listOf("ㄹ", "ㅅ")
    "ㄾ" -> listOf("ㄹ", "ㅌ")
    "ㄿ" -> listOf("ㄹ", "ㅍ")
    "ㅀ" -> listOf("ㄹ", "ㅎ")
    "ㅁ" -> listOf("ㅁ")
    "ㅂ" -> listOf("ㅂ")
    "ㅃ" -> listOf("ㅂ", "ㅂ")
    "ㅄ" -> listOf("ㅂ", "ㅅ")
    "ㅅ" -> listOf("ㅅ")
    "ㅆ" -> listOf("ㅅ", "ㅅ")
    "ㅇ" -> listOf("ㅇ")
    "ㅈ" -> listOf("ㅈ")
    "ㅉ" -> listOf("ㅈ", "ㅈ")
    "ㅊ" -> listOf("ㅊ")
    "ㅋ" -> listOf("ㅋ")
    "ㅌ" -> listOf("ㅌ")
    "ㅍ" -> listOf("ㅍ")
    "ㅎ" -> listOf("ㅎ")
    else -> listOf(cons)
}

/**
 * 음절 단위 -> 초성/중성/종성 슬롯 단위로 묶은 기본형 타일.
 * 슬롯이 타일 1개면 회전해도 조합에 영향 없어 안전하지만, 겹모음(ㅐ 등)이나
 * 쌍자음/겹받침처럼 슬롯 하나가 타일 2~3개로 이루어진 경우 그 조합은
 * combineTiles()가 정확한 베이스 글자 조합만 인식하므로 개별 회전하면 깨진다.
 * 청크(음절) 순서만 섞으면 항상 글자 수와 일치하는 완전한 음절로 파싱된다.
 */
fun getWordSyllableSlots(word: String): List<List<List<String>>> {
    val chunks = mutableListOf<List<List<String>>>()
    for (ch in word) {
        val d = decomposeHangul(ch.toString())
        if (!d.isHangul) continue
        val slots = mutableListOf<List<String>>()
        slots.add(getConsonantBaseComponents(d.cho))
        slots.add(getVowelBaseComponents(d.jung))
        if (d.jong.isNotEmpty()) slots.add(getConsonantBaseComponents(d.jong))
        chunks.add(slots)
    }
    return chunks
}

data class WordBaseTiles(val consonants: List<String>, val vowels: List<String>)

fun getWordBaseTiles(word: String): WordBaseTiles {
    val consonants = mutableListOf<String>()
    val vowels = mutableListOf<String>()
    for (ch in word) {
        val d = decomposeHangul(ch.toString())
        if (!d.isHangul) continue
        consonants.addAll(getConsonantBaseComponents(d.cho))
        vowels.addAll(getVowelBaseComponents(d.jung))
        if (d.jong.isNotEmpty()) consonants.addAll(getConsonantBaseComponents(d.jong))
    }
    val collator = java.text.Collator.getInstance(java.util.Locale.KOREAN)
    consonants.sortWith(collator)
    vowels.sortWith(collator)
    return WordBaseTiles(consonants, vowels)
}
