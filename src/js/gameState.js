/**
 * <언어의 조각> Game State Manager (100 Stages, Tile-based Evaluation, Complete Progress Saving)
 */
import { STAGES_100 } from './stages.js';
import { evaluateTileGuess } from './wordValidator.js';
import { rotateTile, parseTileStreamToSyllables, isRotatable } from './hangulEngine.js';

export class GameState {
  constructor() {
    this.stageIndex = 0;
    this.score = 0;
    this.clearedStages = [];
    this.savedStageState = null;

    this.currentPuzzle = null;
    this.selectedTileIndex = null;
    this.activeTiles = [];
    this.guesses = [];
    this.isRoundOver = false;
    this.isGameOver = false;

    this.listeners = [];

    this.loadSavedProgress();
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
   * Load saved progress from localStorage (stage, score, cleared stages, in-progress tiles & guesses)
   */
  loadSavedProgress() {
    try {
      const saved = localStorage.getItem('wordgame_save_data');
      if (saved) {
        const data = JSON.parse(saved);
        if (typeof data.stageIndex === 'number' && data.stageIndex >= 0 && data.stageIndex < STAGES_100.length) {
          this.stageIndex = data.stageIndex;
        }
        if (typeof data.score === 'number' && !isNaN(data.score)) {
          this.score = data.score;
        }
        if (Array.isArray(data.clearedStages)) {
          this.clearedStages = data.clearedStages;
        }
        if (data.savedStageState && data.savedStageState.stageIndex === this.stageIndex) {
          this.savedStageState = data.savedStageState;
        }
      } else {
        // Fallback for legacy keys
        const legacyIndex = parseInt(localStorage.getItem('wordgame_stage_index') || '0', 10);
        if (!isNaN(legacyIndex) && legacyIndex >= 0 && legacyIndex < STAGES_100.length) {
          this.stageIndex = legacyIndex;
        }
        const legacyScore = parseInt(localStorage.getItem('wordgame_score') || '0', 10);
        if (!isNaN(legacyScore)) {
          this.score = legacyScore;
        }
      }
    } catch (e) {
      console.warn('Failed to parse saved game progress:', e);
    }
  }

  /**
   * Save complete progress into localStorage
   */
  saveProgress() {
    try {
      const data = {
        stageIndex: this.stageIndex,
        score: this.score,
        clearedStages: this.clearedStages,
        savedStageState: {
          stageIndex: this.stageIndex,
          activeTiles: this.activeTiles,
          guesses: this.guesses,
          isRoundOver: this.isRoundOver
        },
        lastUpdated: Date.now()
      };
      localStorage.setItem('wordgame_save_data', JSON.stringify(data));
      localStorage.setItem('wordgame_stage_index', String(this.stageIndex));
      localStorage.setItem('wordgame_score', String(this.score));
    } catch (e) {
      console.warn('Failed to save game progress:', e);
    }
  }

  /**
   * Start a new game or restart from stage 1
   */
  startNewGame() {
    this.stageIndex = 0;
    this.score = 0;
    this.clearedStages = [];
    this.savedStageState = null;
    this.isGameOver = false;
    this.saveProgress();
    this.loadStage(0, true);
  }

  /**
   * Load stage by index (0 ~ 99)
   * @param {number} index 
   * @param {boolean} forceReset
   */
  loadStage(index, forceReset = false) {
    if (index >= STAGES_100.length) {
      this.isGameOver = true;
      this.notify();
      return;
    }
    this.stageIndex = index;
    const stage = STAGES_100[index];
    this.loadPuzzle(stage, forceReset);
  }

  /**
   * Load puzzle (restoring in-progress tiles & attempts if saved)
   * @param {object} puzzle 
   * @param {boolean} forceReset
   */
  loadPuzzle(puzzle, forceReset = false) {
    this.currentPuzzle = puzzle;
    this.selectedTileIndex = null;

    // Check if we have saved in-progress tiles & guesses for this stage
    if (
      !forceReset &&
      this.savedStageState &&
      this.savedStageState.stageIndex === this.stageIndex &&
      Array.isArray(this.savedStageState.activeTiles) &&
      this.savedStageState.activeTiles.length === puzzle.targetTiles.length
    ) {
      this.activeTiles = this.savedStageState.activeTiles;
      this.guesses = Array.isArray(this.savedStageState.guesses) ? this.savedStageState.guesses : [];
      this.isRoundOver = !!this.savedStageState.isRoundOver;
    } else {
      this.guesses = [];
      this.isRoundOver = false;

      // Initialize unified single row of tiles (shuffled initially for gameplay)
      const initialTiles = [...puzzle.tiles];
      for (let i = initialTiles.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [initialTiles[i], initialTiles[j]] = [initialTiles[j], initialTiles[i]];
      }

      this.activeTiles = initialTiles.map((char, index) => ({
        id: index,
        char,
        originalChar: char
      }));
    }

    this.saveProgress();
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
    this.saveProgress();
    this.notify();
  }

  /**
   * Reorder tiles by moving tile from fromIndex to toIndex (drag & drop insertion)
   * @param {number} fromIndex 
   * @param {number} toIndex 
   */
  moveTile(fromIndex, toIndex) {
    if (fromIndex < 0 || fromIndex >= this.activeTiles.length) return;
    if (toIndex < 0 || toIndex >= this.activeTiles.length) return;
    if (fromIndex === toIndex) return;

    const [item] = this.activeTiles.splice(fromIndex, 1);
    this.activeTiles.splice(toIndex, 0, item);
    this.saveProgress();
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
    if (!isRotatable(tile.char)) return;
    tile.char = rotateTile(tile.char, reverse);
    this.saveProgress();
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
    this.saveProgress();
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
    this.saveProgress();
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

      if (!this.clearedStages.includes(this.stageIndex)) {
        this.clearedStages.push(this.stageIndex);
      }

      this.savedStageState = null;
      this.saveProgress();

      if (this.stageIndex >= STAGES_100.length - 1) {
        this.isGameOver = true;
      }
    } else {
      this.saveProgress();
    }

    this.notify();
    return guessEntry;
  }

  /**
   * Advance to the next round
   */
  nextRound() {
    this.savedStageState = null;
    this.loadStage(this.stageIndex + 1, true);
  }
}

export const gameState = new GameState();
