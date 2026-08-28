/**
 * <언어의 조각> Game State Manager (100 Stages, Tile-based Evaluation, Progress Saving)
 */
import { STAGES_100 } from './stages.js';
import { evaluateTileGuess } from './wordValidator.js';
import { rotateTile, parseTileStreamToSyllables, isRotatable } from './hangulEngine.js';

export class GameState {
  constructor() {
    this.stageIndex = parseInt(localStorage.getItem('wordgame_stage_index') || '0', 10);
    if (isNaN(this.stageIndex) || this.stageIndex < 0 || this.stageIndex >= STAGES_100.length) {
      this.stageIndex = 0;
    }

    this.currentPuzzle = null;
    this.score = parseInt(localStorage.getItem('wordgame_score') || '0', 10);
    if (isNaN(this.score)) this.score = 0;

    // Active selected tile for click-to-swap
    this.selectedTileIndex = null;
    
    // Active Tiles in 1 single row: [{ id: 0, char: 'ㄱ', originalChar: 'ㄱ' }]
    this.activeTiles = [];
    
    // Guess history: [{ tiles, feedback, summary, word, syllables, isExactMatch, timestamp }]
    this.guesses = [];
    this.isRoundOver = false;
    this.isGameOver = false;

    // Listeners
    this.listeners = [];
  }

  subscribe(listener) {
    this.listeners.push(listener);
  }

  notify() {
    for (const l of this.listeners) {
      l(this);
    }
  }

  /**
   * Start a new game or restart from stage 1
   */
  startNewGame() {
    this.stageIndex = 0;
    this.score = 0;
    this.isGameOver = false;
    this.saveProgress();
    this.loadStage(0);
  }

  saveProgress() {
    localStorage.setItem('wordgame_stage_index', String(this.stageIndex));
    localStorage.setItem('wordgame_score', String(this.score));
  }

  /**
   * Load stage by index (0 ~ 99)
   * @param {number} index 
   */
  loadStage(index) {
    if (index >= STAGES_100.length) {
      this.isGameOver = true;
      this.notify();
      return;
    }
    this.stageIndex = index;
    this.saveProgress();
    const stage = STAGES_100[index];
    this.loadPuzzle(stage);
  }

  /**
   * Load puzzle
   * @param {object} puzzle 
   */
  loadPuzzle(puzzle) {
    this.currentPuzzle = puzzle;
    this.guesses = [];
    this.isRoundOver = false;
    this.selectedTileIndex = null;

    // Initialize unified single row of tiles (shuffled initially for fun gameplay)
    const initialTiles = [...puzzle.tiles];
    // Shuffle tiles
    for (let i = initialTiles.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [initialTiles[i], initialTiles[j]] = [initialTiles[j], initialTiles[i]];
    }

    this.activeTiles = initialTiles.map((char, index) => ({
      id: index,
      char,
      originalChar: char
    }));

    this.notify();
  }

  /**
   * Select a tile for click-to-swap
   * @param {number} index 
   */
  selectTile(index) {
    if (index < 0 || index >= this.activeTiles.length) {
      this.selectedTileIndex = null;
      this.notify();
      return;
    }

    if (this.selectedTileIndex === null) {
      this.selectedTileIndex = index;
    } else if (this.selectedTileIndex === index) {
      this.selectedTileIndex = null; // deselect
    } else {
      // Swap the two tiles!
      this.swapTiles(this.selectedTileIndex, index);
      this.selectedTileIndex = null;
    }
    this.notify();
  }

  /**
   * Swap two tiles at index A and B
   * @param {number} i 
   * @param {number} j 
   */
  swapTiles(i, j) {
    if (i < 0 || i >= this.activeTiles.length || j < 0 || j >= this.activeTiles.length || i === j) return;
    const temp = this.activeTiles[i];
    this.activeTiles[i] = this.activeTiles[j];
    this.activeTiles[j] = temp;
    this.notify();
  }

  /**
   * Reorder tiles by moving tile from fromIndex to toIndex (drag & drop)
   * @param {number} fromIndex 
   * @param {number} toIndex 
   */
  moveTile(fromIndex, toIndex) {
    if (fromIndex < 0 || fromIndex >= this.activeTiles.length) return;
    if (toIndex < 0 || toIndex >= this.activeTiles.length) return;
    if (fromIndex === toIndex) return;

    const [item] = this.activeTiles.splice(fromIndex, 1);
    this.activeTiles.splice(toIndex, 0, item);
    this.notify();
  }

  /**
   * Rotate a specific tile at index if rotatable
   * @param {number} index 
   * @param {boolean} reverse 
   */
  rotateTileAt(index, reverse = false) {
    if (index < 0 || index >= this.activeTiles.length) return;
    const tile = this.activeTiles[index];
    if (!isRotatable(tile.char)) return; // Do not rotate non-rotatable tiles
    tile.char = rotateTile(tile.char, reverse);
    this.notify();
  }

  /**
   * Reset tiles to initial base tiles
   */
  resetTiles() {
    if (!this.currentPuzzle) return;
    this.activeTiles = this.currentPuzzle.tiles.map((char, index) => ({
      id: index,
      char,
      originalChar: char
    }));
    this.selectedTileIndex = null;
    this.notify();
  }

  /**
   * Shuffle tiles order
   */
  shuffleTiles() {
    for (let i = this.activeTiles.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [this.activeTiles[i], this.activeTiles[j]] = [this.activeTiles[j], this.activeTiles[i]];
    }
    this.selectedTileIndex = null;
    this.notify();
  }

  /**
   * Get current parsed syllables and assembled word from active tiles
   */
  getCurrentAssembled() {
    const tileChars = this.activeTiles.map(t => t.char);
    return parseTileStreamToSyllables(tileChars);
  }

  /**
   * Check if current tile arrangement is ready to submit
   */
  canSubmit() {
    if (this.isRoundOver || this.isGameOver) return false;
    return this.activeTiles.length === this.currentPuzzle.targetTiles.length;
  }

  /**
   * Submit current tile arrangement as guess (Tile-based Evaluation)
   */
  submitGuess() {
    if (!this.canSubmit()) return null;

    const submittedTiles = this.activeTiles.map(t => t.char);
    const targetTiles = this.currentPuzzle.targetTiles;

    const evalResult = evaluateTileGuess(submittedTiles, targetTiles);
    const assembled = this.getCurrentAssembled();

    const guessEntry = {
      tiles: [...submittedTiles],
      feedback: evalResult.feedback,
      summary: evalResult.summary,
      isExactMatch: evalResult.isExactMatch,
      word: assembled.word || '',
      syllables: assembled.syllables || [],
      timestamp: Date.now()
    };

    this.guesses.push(guessEntry);

    if (evalResult.isExactMatch) {
      this.isRoundOver = true;
      const points = this.currentPuzzle.points || this.currentPuzzle.length;
      this.score += points;
      this.saveProgress();

      if (this.stageIndex >= STAGES_100.length - 1) {
        this.isGameOver = true;
      }
    }

    this.notify();
    return guessEntry;
  }

  /**
   * Advance to the next round
   */
  nextRound() {
    this.loadStage(this.stageIndex + 1);
  }
}

export const gameState = new GameState();
