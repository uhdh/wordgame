/**
 * <언어의 조각> Game State Manager (Single 100-Stage Puzzle Mode)
 * Full state persistence, 100 stages, tile-based evaluation.
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
   * Load saved progress from localStorage
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
      console.warn('Failed to parse saved progress:', e);
    }
  }

  /**
   * Save complete progress to localStorage
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
          isRoundOver: this.isRoundOver,
          hintUsed: this.hintUsed
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
   * Load puzzle data
   * @param {object} puzzle 
   * @param {boolean} forceReset 
   */
  loadPuzzle(puzzle, forceReset = false) {
    this.currentPuzzle = puzzle;
    this.selectedTileIndex = null;

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
      this.hintUsed = !!this.savedStageState.hintUsed;
    } else {
      this.guesses = [];
      this.isRoundOver = false;
      this.hintUsed = false;

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

  useHint() {
    if (!this.hintUsed) {
      this.hintUsed = true;
      this.saveProgress();
      this.notify();
    }
  }

  selectTile(index) {
    if (index < 0 || index >= this.activeTiles.length) {
      this.selectedTileIndex = null;
      this.notify();
      return;
    }

    if (this.selectedTileIndex === null) {
      this.selectedTileIndex = index;
    } else if (this.selectedTileIndex === index) {
      this.selectedTileIndex = null;
    } else {
      this.swapTiles(this.selectedTileIndex, index);
      this.selectedTileIndex = null;
    }
    this.notify();
  }

  swapTiles(i, j) {
    if (i < 0 || i >= this.activeTiles.length || j < 0 || j >= this.activeTiles.length || i === j) return;
    const temp = this.activeTiles[i];
    this.activeTiles[i] = this.activeTiles[j];
    this.activeTiles[j] = temp;
    this.saveProgress();
    this.notify();
  }

  moveTile(fromIndex, toIndex) {
    if (fromIndex < 0 || fromIndex >= this.activeTiles.length) return;
    if (toIndex < 0 || toIndex >= this.activeTiles.length) return;
    if (fromIndex === toIndex) return;

    const [item] = this.activeTiles.splice(fromIndex, 1);
    this.activeTiles.splice(toIndex, 0, item);
    this.saveProgress();
    this.notify();
  }

  rotateTileAt(index, reverse = false) {
    if (index < 0 || index >= this.activeTiles.length) return;
    const tile = this.activeTiles[index];
    if (!isRotatable(tile.char)) return;
    tile.char = rotateTile(tile.char, reverse);
    this.saveProgress();
    this.notify();
  }

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

  shuffleTiles() {
    for (let i = this.activeTiles.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [this.activeTiles[i], this.activeTiles[j]] = [this.activeTiles[j], this.activeTiles[i]];
    }
    this.selectedTileIndex = null;
    this.saveProgress();
    this.notify();
  }

  getCurrentAssembled() {
    const tileChars = this.activeTiles.map(t => t.char);
    return parseTileStreamToSyllables(tileChars);
  }

  canSubmit() {
    if (this.isRoundOver || this.isGameOver) return false;
    return this.activeTiles.length === this.currentPuzzle.targetTiles.length;
  }

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
      const basePoints = this.currentPuzzle.points || this.currentPuzzle.length;
      const hintPenalty = this.hintUsed ? 1 : 0;
      const attemptPenalty = this.guesses.length > 5 ? 1 : 0;
      const totalPenalty = hintPenalty + attemptPenalty;
      const earnedPoints = Math.max(1, basePoints - totalPenalty);

      this.lastEarnedPoints = earnedPoints;
      this.lastPenaltyDetails = {
        basePoints,
        hintPenalty,
        attemptPenalty,
        totalPenalty
      };

      this.score += earnedPoints;

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

  nextRound() {
    this.savedStageState = null;
    this.loadStage(this.stageIndex + 1, true);
  }
}

export const gameState = new GameState();
