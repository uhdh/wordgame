/**
 * <언어의 조각> Hangul Engine
 * Handles Hangul decomposition, syllable composition, tile rotation, combination, and linear tile stream parsing.
 */

// Basic Hangul Jamo Unicode tables
export const CHOSUNG = [
  'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
  'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
];

export const JUNGSUNG = [
  'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
  'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
];

export const JONGSUNG = [
  '', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
  'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
  'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
];

// Rotatable tiles check: only ㄱ, ㄴ, ㅏ, ㅓ, ㅗ, ㅜ, ㅑ, ㅕ, ㅛ, ㅠ, ㅣ, ㅡ
export const ROTATABLE_TILES = new Set([
  'ㄱ', 'ㄴ',
  'ㅏ', 'ㅓ', 'ㅗ', 'ㅜ',
  'ㅑ', 'ㅕ', 'ㅛ', 'ㅠ',
  'ㅣ', 'ㅡ'
]);

export function isRotatable(tile) {
  return ROTATABLE_TILES.has(tile);
}

// Tile Rotation mappings
export const CONSONANT_ROTATIONS = {
  'ㄱ': 'ㄴ',
  'ㄴ': 'ㄱ'
};

export const VOWEL_ROTATIONS = {
  // Clockwise rotation (시계 방향 90°: 3시 ㅏ -> 6시 ㅜ -> 9시 ㅓ -> 12시 ㅗ)
  'ㅏ': 'ㅜ',
  'ㅜ': 'ㅓ',
  'ㅓ': 'ㅗ',
  'ㅗ': 'ㅏ',

  // 3시 ㅑ -> 6시 ㅠ -> 9시 ㅕ -> 12시 ㅛ
  'ㅑ': 'ㅠ',
  'ㅠ': 'ㅕ',
  'ㅕ': 'ㅛ',
  'ㅛ': 'ㅑ',

  'ㅣ': 'ㅡ',
  'ㅡ': 'ㅣ'
};

export const VOWEL_REVERSE_ROTATIONS = {
  // Counter-clockwise rotation (반시계 방향)
  'ㅏ': 'ㅗ',
  'ㅗ': 'ㅓ',
  'ㅓ': 'ㅜ',
  'ㅜ': 'ㅏ',

  'ㅑ': 'ㅛ',
  'ㅛ': 'ㅕ',
  'ㅕ': 'ㅠ',
  'ㅠ': 'ㅑ',

  'ㅣ': 'ㅡ',
  'ㅡ': 'ㅣ'
};

// Combinations are strictly ordered in the sequence they are written
export const CONSONANT_COMBINATIONS = {
  'ㄱ+ㄱ': 'ㄲ',
  'ㄷ+ㄷ': 'ㄸ',
  'ㅂ+ㅂ': 'ㅃ',
  'ㅅ+ㅅ': 'ㅆ',
  'ㅈ+ㅈ': 'ㅉ',
  'ㄱ+ㅅ': 'ㄳ',
  'ㄴ+ㅈ': 'ㄵ',
  'ㄴ+ㅎ': 'ㄶ',
  'ㄹ+ㄱ': 'ㄺ',
  'ㄹ+ㅁ': 'ㄻ',
  'ㄹ+ㅂ': 'ㄼ',
  'ㄹ+ㅅ': 'ㄽ',
  'ㄹ+ㅌ': 'ㄾ',
  'ㄹ+ㅍ': 'ㄿ',
  'ㄹ+ㅎ': 'ㅀ',
  'ㅂ+ㅅ': 'ㅄ'
};

export const VOWEL_COMBINATIONS = {
  'ㅏ+ㅣ': 'ㅐ',
  'ㅓ+ㅣ': 'ㅔ',
  'ㅗ+ㅣ': 'ㅚ',
  'ㅜ+ㅣ': 'ㅟ',
  'ㅑ+ㅣ': 'ㅒ',
  'ㅕ+ㅣ': 'ㅖ',
  'ㅗ+ㅏ': 'ㅘ',
  'ㅜ+ㅓ': 'ㅝ',
  'ㅗ+ㅐ': 'ㅙ',
  'ㅘ+ㅣ': 'ㅙ',
  'ㅗ+ㅏ+ㅣ': 'ㅙ',
  'ㅜ+ㅔ': 'ㅞ',
  'ㅝ+ㅣ': 'ㅞ',
  'ㅜ+ㅓ+ㅣ': 'ㅞ',
  'ㅡ+ㅣ': 'ㅢ'
};

/**
 * Rotate a tile
 * @param {string} tile 
 * @param {boolean} reverse 
 * @returns {string}
 */
export function rotateTile(tile, reverse = false) {
  if (CONSONANT_ROTATIONS[tile]) {
    return CONSONANT_ROTATIONS[tile];
  }
  if (reverse && VOWEL_REVERSE_ROTATIONS[tile]) {
    return VOWEL_REVERSE_ROTATIONS[tile];
  }
  if (VOWEL_ROTATIONS[tile]) {
    return VOWEL_ROTATIONS[tile];
  }
  return tile;
}

/**
 * Check if two or three tiles can combine (strictly sequential, directional order)
 * @param {string[]} tiles 
 * @returns {string|null}
 */
export function combineTiles(tiles) {
  if (!tiles || tiles.length < 2) return null;
  const key2 = `${tiles[0]}+${tiles[1]}`;

  if (tiles.length === 2) {
    if (CONSONANT_COMBINATIONS[key2]) return CONSONANT_COMBINATIONS[key2];
    if (VOWEL_COMBINATIONS[key2]) return VOWEL_COMBINATIONS[key2];
  } else if (tiles.length === 3) {
    const key3 = `${tiles[0]}+${tiles[1]}+${tiles[2]}`;
    if (VOWEL_COMBINATIONS[key3]) return VOWEL_COMBINATIONS[key3];
  }
  return null;
}

export function isConsonant(char) {
  return CHOSUNG.includes(char) || JONGSUNG.includes(char);
}

export function isVowel(char) {
  return JUNGSUNG.includes(char);
}

/**
 * Decompose a Hangul Syllable into (Cho, Jung, Jong)
 * @param {string} char 
 * @returns {{ cho: string, jung: string, jong: string, isHangul: boolean }}
 */
export function decomposeHangul(char) {
  if (!char || char.length !== 1) {
    return { cho: '', jung: '', jong: '', isHangul: false };
  }
  const code = char.charCodeAt(0) - 0xAC00;
  if (code < 0 || code > 11171) {
    return { cho: char, jung: '', jong: '', isHangul: false };
  }
  const choIdx = Math.floor(code / 588);
  const jungIdx = Math.floor((code % 588) / 28);
  const jongIdx = code % 28;

  return {
    cho: CHOSUNG[choIdx],
    jung: JUNGSUNG[jungIdx],
    jong: JONGSUNG[jongIdx],
    isHangul: true
  };
}

/**
 * Get Chosung (initial consonants) hint from word
 * e.g. "금메달" -> "ㄱ ㅁ ㄷ"
 * @param {string} word 
 * @returns {string}
 */
export function getWordChosungHint(word) {
  if (!word) return '';
  return word.split('').map(char => {
    const { cho, isHangul } = decomposeHangul(char);
    return isHangul ? cho : char;
  }).join(' ');
}

/**
 * Compose Chosung, Jungsung, and optional Jongsung into a single Hangul syllable
 * @param {string} cho 
 * @param {string} jung 
 * @param {string} jong 
 * @returns {string}
 */
export function composeHangul(cho, jung, jong = '') {
  const choIdx = CHOSUNG.indexOf(cho);
  const jungIdx = JUNGSUNG.indexOf(jung);
  const jongIdx = JONGSUNG.indexOf(jong);

  if (choIdx === -1 || jungIdx === -1 || jongIdx === -1) {
    return cho || jung || jong || '';
  }

  const code = 0xAC00 + (choIdx * 588) + (jungIdx * 28) + jongIdx;
  return String.fromCharCode(code);
}

/**
 * Decompose a complete Korean word into exact linear target tiles
 * @param {string} word 
 * @returns {string[]}
 */
export function decomposeWordToTargetTiles(word) {
  const targetTiles = [];
  for (let i = 0; i < word.length; i++) {
    const { cho, jung, jong, isHangul } = decomposeHangul(word[i]);
    if (!isHangul) continue;

    // Cho
    if (cho === 'ㄲ') targetTiles.push('ㄱ', 'ㄱ');
    else if (cho === 'ㄸ') targetTiles.push('ㄷ', 'ㄷ');
    else if (cho === 'ㅃ') targetTiles.push('ㅂ', 'ㅂ');
    else if (cho === 'ㅆ') targetTiles.push('ㅅ', 'ㅅ');
    else if (cho === 'ㅉ') targetTiles.push('ㅈ', 'ㅈ');
    else targetTiles.push(cho);

    // Jung
    if (jung === 'ㅐ') targetTiles.push('ㅏ', 'ㅣ');
    else if (jung === 'ㅔ') targetTiles.push('ㅓ', 'ㅣ');
    else if (jung === 'ㅚ') targetTiles.push('ㅗ', 'ㅣ');
    else if (jung === 'ㅟ') targetTiles.push('ㅜ', 'ㅣ');
    else if (jung === 'ㅒ') targetTiles.push('ㅑ', 'ㅣ');
    else if (jung === 'ㅖ') targetTiles.push('ㅕ', 'ㅣ');
    else if (jung === 'ㅘ') targetTiles.push('ㅗ', 'ㅏ');
    else if (jung === 'ㅝ') targetTiles.push('ㅜ', 'ㅓ');
    else if (jung === 'ㅙ') targetTiles.push('ㅗ', 'ㅏ', 'ㅣ');
    else if (jung === 'ㅞ') targetTiles.push('ㅜ', 'ㅓ', 'ㅣ');
    else if (jung === 'ㅢ') targetTiles.push('ㅡ', 'ㅣ');
    else targetTiles.push(jung);

    // Jong
    if (jong) {
      if (jong === 'ㄲ') targetTiles.push('ㄱ', 'ㄱ');
      else if (jong === 'ㄳ') targetTiles.push('ㄱ', 'ㅅ');
      else if (jong === 'ㄵ') targetTiles.push('ㄴ', 'ㅈ');
      else if (jong === 'ㄶ') targetTiles.push('ㄴ', 'ㅎ');
      else if (jong === 'ㄺ') targetTiles.push('ㄹ', 'ㄱ');
      else if (jong === 'ㄻ') targetTiles.push('ㄹ', 'ㅁ');
      else if (jong === 'ㄼ') targetTiles.push('ㄹ', 'ㅂ');
      else if (jong === 'ㄽ') targetTiles.push('ㄹ', 'ㅅ');
      else if (jong === 'ㄾ') targetTiles.push('ㄹ', 'ㅌ');
      else if (jong === 'ㄿ') targetTiles.push('ㄹ', 'ㅍ');
      else if (jong === 'ㅀ') targetTiles.push('ㄹ', 'ㅎ');
      else if (jong === 'ㅄ') targetTiles.push('ㅂ', 'ㅅ');
      else if (jong === 'ㅆ') targetTiles.push('ㅅ', 'ㅅ');
      else targetTiles.push(jong);
    }
  }
  return targetTiles;
}

/**
 * Parse a linear stream of tiles into assembled Korean syllables and standalone Jamo
 * @param {string[]} tiles
 * @returns {{ syllables: string[], word: string, mapToTiles: number[][] }}
 */
export function parseTileStreamToSyllables(tiles) {
  if (!tiles || tiles.length === 0) {
    return { syllables: [], word: '', mapToTiles: [] };
  }

  const syllables = [];
  const mapToTiles = [];
  let i = 0;

  while (i < tiles.length) {
    const t1 = tiles[i];

    if (isConsonant(t1)) {
      let cho = t1;
      let choIndices = [i];
      let nextIdx = i + 1;

      if (nextIdx < tiles.length && isConsonant(tiles[nextIdx])) {
        const doubleCons = combineTiles([cho, tiles[nextIdx]]);
        if (doubleCons && CHOSUNG.includes(doubleCons)) {
          if (nextIdx + 1 < tiles.length && isVowel(tiles[nextIdx + 1])) {
            cho = doubleCons;
            choIndices.push(nextIdx);
            nextIdx++;
          }
        }
      }

      if (nextIdx < tiles.length && isVowel(tiles[nextIdx])) {
        let jung = tiles[nextIdx];
        let jungIndices = [nextIdx];
        nextIdx++;

        while (nextIdx < tiles.length && isVowel(tiles[nextIdx])) {
          const combo = combineTiles([jung, tiles[nextIdx]]);
          if (combo && JUNGSUNG.includes(combo)) {
            jung = combo;
            jungIndices.push(nextIdx);
            nextIdx++;
          } else {
            break;
          }
        }

        let jong = '';
        let jongIndices = [];

        if (nextIdx < tiles.length && isConsonant(tiles[nextIdx])) {
          const cons1 = tiles[nextIdx];
          const hasVowelAfter = (nextIdx + 1 < tiles.length && isVowel(tiles[nextIdx + 1])) ||
                                (nextIdx + 2 < tiles.length && isConsonant(tiles[nextIdx + 1]) && isVowel(tiles[nextIdx + 2]) && combineTiles([cons1, tiles[nextIdx + 1]]) && CHOSUNG.includes(combineTiles([cons1, tiles[nextIdx + 1]])));

          if (!hasVowelAfter) {
            jong = cons1;
            jongIndices = [nextIdx];
            nextIdx++;

            if (nextIdx < tiles.length && isConsonant(tiles[nextIdx])) {
              const hasVowelAfter2 = (nextIdx + 1 < tiles.length && isVowel(tiles[nextIdx + 1]));
              if (!hasVowelAfter2) {
                const complexJong = combineTiles([jong, tiles[nextIdx]]);
                if (complexJong && JONGSUNG.includes(complexJong)) {
                  jong = complexJong;
                  jongIndices.push(nextIdx);
                  nextIdx++;
                }
              }
            }
          }
        }

        const syllable = composeHangul(cho, jung, jong);
        syllables.push(syllable);
        mapToTiles.push([...choIndices, ...jungIndices, ...jongIndices]);
        i = nextIdx;
        continue;
      } else {
        syllables.push(cho);
        mapToTiles.push(choIndices);
        i = nextIdx;
        continue;
      }
    } else {
      let jung = t1;
      let jungIndices = [i];
      let nextIdx = i + 1;
      while (nextIdx < tiles.length && isVowel(tiles[nextIdx])) {
        const combo = combineTiles([jung, tiles[nextIdx]]);
        if (combo && JUNGSUNG.includes(combo)) {
          jung = combo;
          jungIndices.push(nextIdx);
          nextIdx++;
        } else {
          break;
        }
      }
      syllables.push(jung);
      mapToTiles.push(jungIndices);
      i = nextIdx;
    }
  }

  return {
    syllables,
    word: syllables.join(''),
    mapToTiles
  };
}

export function getVowelBaseComponents(vowel) {
  switch (vowel) {
    case 'ㅏ': return ['ㅏ'];
    case 'ㅓ': return ['ㅏ'];
    case 'ㅗ': return ['ㅏ'];
    case 'ㅜ': return ['ㅏ'];
    case 'ㅣ': return ['ㅣ'];
    case 'ㅡ': return ['ㅣ'];

    case 'ㅑ': return ['ㅑ'];
    case 'ㅕ': return ['ㅑ'];
    case 'ㅛ': return ['ㅑ'];
    case 'ㅠ': return ['ㅑ'];

    case 'ㅐ': return ['ㅏ', 'ㅣ'];
    case 'ㅔ': return ['ㅏ', 'ㅣ'];
    case 'ㅚ': return ['ㅏ', 'ㅣ'];
    case 'ㅟ': return ['ㅏ', 'ㅣ'];

    case 'ㅒ': return ['ㅑ', 'ㅣ'];
    case 'ㅖ': return ['ㅑ', 'ㅣ'];

    case 'ㅘ': return ['ㅏ', 'ㅏ'];
    case 'ㅝ': return ['ㅏ', 'ㅏ'];
    case 'ㅙ': return ['ㅏ', 'ㅏ', 'ㅣ'];
    case 'ㅞ': return ['ㅏ', 'ㅏ', 'ㅣ'];
    case 'ㅢ': return ['ㅣ', 'ㅣ'];

    default: return [vowel];
  }
}

export function getConsonantBaseComponents(cons) {
  switch (cons) {
    case '': return [];
    case 'ㄱ': return ['ㄱ'];
    case 'ㄴ': return ['ㄱ'];
    case 'ㄲ': return ['ㄱ', 'ㄱ'];
    case 'ㄳ': return ['ㄱ', 'ㅅ'];
    case 'ㄵ': return ['ㄱ', 'ㅈ'];
    case 'ㄶ': return ['ㄱ', 'ㅎ'];
    case 'ㄷ': return ['ㄷ'];
    case 'ㄸ': return ['ㄷ', 'ㄷ'];
    case 'ㄹ': return ['ㄹ'];
    case 'ㄺ': return ['ㄹ', 'ㄱ'];
    case 'ㄻ': return ['ㄹ', 'ㅁ'];
    case 'ㄼ': return ['ㄹ', 'ㅂ'];
    case 'ㄽ': return ['ㄹ', 'ㅅ'];
    case 'ㄾ': return ['ㄹ', 'ㅌ'];
    case 'ㄿ': return ['ㄹ', 'ㅍ'];
    case 'ㅀ': return ['ㄹ', 'ㅎ'];
    case 'ㅁ': return ['ㅁ'];
    case 'ㅂ': return ['ㅂ'];
    case 'ㅃ': return ['ㅂ', 'ㅂ'];
    case 'ㅄ': return ['ㅂ', 'ㅅ'];
    case 'ㅅ': return ['ㅅ'];
    case 'ㅆ': return ['ㅅ', 'ㅅ'];
    case 'ㅇ': return ['ㅇ'];
    case 'ㅈ': return ['ㅈ'];
    case 'ㅉ': return ['ㅈ', 'ㅈ'];
    case 'ㅊ': return ['ㅊ'];
    case 'ㅋ': return ['ㅋ'];
    case 'ㅌ': return ['ㅌ'];
    case 'ㅍ': return ['ㅍ'];
    case 'ㅎ': return ['ㅎ'];
    default: return [cons];
  }
}

export function getWordBaseTiles(word) {
  const consonants = [];
  const vowels = [];

  for (let i = 0; i < word.length; i++) {
    const { cho, jung, jong, isHangul } = decomposeHangul(word[i]);
    if (!isHangul) continue;

    consonants.push(...getConsonantBaseComponents(cho));
    vowels.push(...getVowelBaseComponents(jung));
    if (jong) {
      consonants.push(...getConsonantBaseComponents(jong));
    }
  }

  consonants.sort((a, b) => a.localeCompare(b, 'ko'));
  vowels.sort((a, b) => a.localeCompare(b, 'ko'));

  return { consonants, vowels };
}
