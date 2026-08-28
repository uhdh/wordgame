/**
 * <언어의 조각> Word & Tile Validator (Tile-based Evaluation)
 * Evaluates guesses directly based on tile sequences (타일 기준 채점).
 */

/**
 * Compare submitted tiles with target tiles and calculate slot feedback
 * @param {string[]} submittedTiles - Array of current tiles (e.g. ['ㄱ', 'ㅡ', 'ㅁ', ...])
 * @param {string[]} targetTiles - Array of target solution tiles
 * @returns {{
 *   feedback: Array<'correct'|'present'|'absent'>,
 *   isExactMatch: boolean,
 *   summary: { green: number, yellow: number, red: number }
 * }}
 */
export function evaluateTileGuess(submittedTiles, targetTiles) {
  const len = Math.max(submittedTiles.length, targetTiles.length);
  const feedback = new Array(submittedTiles.length).fill('absent');

  // Frequency map for target tiles not yet matched green
  const remainingCounts = {};
  for (const t of targetTiles) {
    remainingCounts[t] = (remainingCounts[t] || 0) + 1;
  }

  // 1st Pass: Identify exact position matches (Green / 초록)
  for (let i = 0; i < submittedTiles.length; i++) {
    if (i < targetTiles.length && submittedTiles[i] === targetTiles[i]) {
      feedback[i] = 'correct';
      remainingCounts[submittedTiles[i]] -= 1;
    }
  }

  // 2nd Pass: Identify misplaced matches (Yellow / 노랑)
  for (let i = 0; i < submittedTiles.length; i++) {
    if (feedback[i] === 'correct') continue;
    const tile = submittedTiles[i];
    if (remainingCounts[tile] && remainingCounts[tile] > 0) {
      feedback[i] = 'present';
      remainingCounts[tile] -= 1;
    } else {
      feedback[i] = 'absent';
    }
  }

  const isExactMatch = feedback.length === targetTiles.length && feedback.every(s => s === 'correct');
  const summary = {
    green: feedback.filter(s => s === 'correct').length,
    yellow: feedback.filter(s => s === 'present').length,
    red: feedback.filter(s => s === 'absent').length
  };

  return {
    feedback,
    isExactMatch,
    summary
  };
}
