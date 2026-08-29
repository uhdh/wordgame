/**
 * <언어의 조각> Application Controller (Mobile Optimized, Touch Drag & Drop, Dual Mode: Normal & Hardcore Wordle)
 */

import { gameState } from './gameState.js';
import { sound } from './audioEffects.js';
import { isRotatable, getWordChosungHint, decomposeHangul } from './hangulEngine.js';
import { STAGES_100 } from './stages.js';

// DOM Elements
const el = {
  // Mode Switcher
  btnModeNormal: document.getElementById('btnModeNormal'),
  btnModeHardcore: document.getElementById('btnModeHardcore'),
  normalModeView: document.getElementById('normalModeView'),
  hardcoreModeView: document.getElementById('hardcoreModeView'),

  // Header & Meta
  roundNumber: document.getElementById('roundNumber'),
  difficultyPill: document.getElementById('difficultyPill'),
  scoreVal: document.getElementById('scoreVal'),

  // Normal Mode Elements
  targetDesc: document.getElementById('targetDesc'),
  btnToggleHint: document.getElementById('btnToggleHint'),
  stageHintBox: document.getElementById('stageHintBox'),
  stageHintText: document.getElementById('stageHintText'),
  previewWordBoxes: document.getElementById('previewWordBoxes'),
  tilesTrackContainer: document.getElementById('tilesTrackContainer'),
  tilesTrack: document.getElementById('tilesTrack'),
  btnShuffleTiles: document.getElementById('btnShuffleTiles'),
  btnResetTiles: document.getElementById('btnResetTiles'),
  btnSubmitGuess: document.getElementById('btnSubmitGuess'),
  historyList: document.getElementById('historyList'),
  historyCount: document.getElementById('historyCount'),

  // Hardcore Wordle Mode Elements
  hardcoreTargetDesc: document.getElementById('hardcoreTargetDesc'),
  hardcoreAttemptCount: document.getElementById('hardcoreAttemptCount'),
  btnToggleHardcoreHint: document.getElementById('btnToggleHardcoreHint'),
  hardcoreHintBox: document.getElementById('hardcoreHintBox'),
  hardcoreHintText: document.getElementById('hardcoreHintText'),
  wordleGrid: document.getElementById('wordleGrid'),
  wordleKeypad: document.getElementById('wordleKeypad'),
  hardcoreFailModal: document.getElementById('hardcoreFailModal'),
  hardcoreFailWord: document.getElementById('hardcoreFailWord'),
  btnRetryHardcore: document.getElementById('btnRetryHardcore'),

  // Modals & Actions
  btnRules: document.getElementById('btnRules'),
  rulesModal: document.getElementById('rulesModal'),
  btnCloseRules: document.getElementById('btnCloseRules'),
  btnStageSelect: document.getElementById('btnStageSelect'),
  stageSelectModal: document.getElementById('stageSelectModal'),
  btnCloseStageSelect: document.getElementById('btnCloseStageSelect'),
  stagesGrid: document.getElementById('stagesGrid'),
  btnSound: document.getElementById('btnSound'),
  soundIconOn: document.getElementById('soundIconOn'),
  soundIconOff: document.getElementById('soundIconOff'),
  roundClearModal: document.getElementById('roundClearModal'),
  roundClearTitle: document.getElementById('roundClearTitle'),
  roundClearWord: document.getElementById('roundClearWord'),
  awardedPoints: document.getElementById('awardedPoints'),
  btnNextRound: document.getElementById('btnNextRound'),
  gameOverModal: document.getElementById('gameOverModal'),
  finalScoreText: document.getElementById('finalScoreText'),
  btnRestartGame: document.getElementById('btnRestartGame'),
  confettiCanvas: document.getElementById('confettiCanvas')
};

// Keypad layout definition for Hardcore Wordle
const KEYPAD_ROWS = [
  ['ㅂ', 'ㅈ', 'ㄷ', 'ㄱ', 'ㅅ', 'ㅛ', 'ㅕ', 'ㅑ', 'ㅐ', 'ㅔ'],
  ['ㅁ', 'ㄴ', 'ㅇ', 'ㄹ', 'ㅎ', 'ㅗ', 'ㅓ', 'ㅏ', 'ㅣ'],
  ['ENTER', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅊ', 'ㅠ', 'ㅜ', 'ㅡ', 'BACKSPACE']
];

// Physical Keyboard code to Korean 2-Set Jamo mapping (QWERTY layout)
const CODE_TO_HANGUL = {
  'KeyQ': 'ㅂ', 'KeyW': 'ㅈ', 'KeyE': 'ㄷ', 'KeyR': 'ㄱ', 'KeyT': 'ㅅ',
  'KeyY': 'ㅛ', 'KeyU': 'ㅕ', 'KeyI': 'ㅑ', 'KeyO': 'ㅐ', 'KeyP': 'ㅔ',
  'KeyA': 'ㅁ', 'KeyS': 'ㄴ', 'KeyD': 'ㅇ', 'KeyF': 'ㄹ', 'KeyG': 'ㅎ',
  'KeyH': 'ㅗ', 'KeyJ': 'ㅓ', 'KeyK': 'ㅏ', 'KeyL': 'ㅣ',
  'KeyZ': 'ㅋ', 'KeyX': 'ㅌ', 'KeyC': 'ㅊ', 'KeyV': 'ㅍ',
  'KeyB': 'ㅠ', 'KeyN': 'ㅜ', 'KeyM': 'ㅡ'
};

const SHIFT_CODE_TO_HANGUL = {
  'KeyQ': 'ㅃ', 'KeyW': 'ㅉ', 'KeyE': 'ㄸ', 'KeyR': 'ㄲ', 'KeyT': 'ㅆ',
  'KeyO': 'ㅒ', 'KeyP': 'ㅖ'
};

function highlightKeypadButton(key) {
  const btn = document.querySelector(`.wordle-key[data-key="${key}"]`);
  if (btn) {
    btn.classList.add('key-pressed');
    setTimeout(() => btn.classList.remove('key-pressed'), 130);
  }
}

function highlightActionKey(actionClass) {
  const btn = document.querySelector(`.wordle-key.${actionClass}`);
  if (btn) {
    btn.classList.add('key-pressed');
    setTimeout(() => btn.classList.remove('key-pressed'), 130);
  }
}

// Drag & Drop / Touch State
let draggedIndex = null;
let touchStartX = 0;
let touchStartY = 0;
let touchActiveTile = null;
let currentStageFilter = 'all';

/**
 * Haptic Vibration Helper
 */
function triggerHaptic(pattern = 10) {
  if (typeof navigator !== 'undefined' && navigator.vibrate) {
    try {
      navigator.vibrate(pattern);
    } catch (e) {
      // Ignore vibration errors if not supported
    }
  }
}

/**
 * Initialize Application
 */
function init() {
  gameState.subscribe(render);
  bindEvents();
  gameState.loadStage(gameState.stageIndex);
  updateSoundIcon();
}

/**
 * Bind DOM Event Listeners
 */
function bindEvents() {
  // Mode Switcher Tabs
  el.btnModeNormal.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    gameState.setGameMode('normal');
  });

  el.btnModeHardcore.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    gameState.setGameMode('hardcore');
  });

  // Normal Submit Guess
  el.btnSubmitGuess.addEventListener('click', () => {
    handleSubmitGuess();
  });

  // Shuffle Tiles
  el.btnShuffleTiles.addEventListener('click', () => {
    triggerHaptic(15);
    sound.playTileRotate();
    gameState.shuffleTiles();
  });

  // Reset Tiles
  el.btnResetTiles.addEventListener('click', () => {
    triggerHaptic(15);
    sound.playTileClick();
    gameState.resetTiles();
  });

  // Hint Toggles
  el.btnToggleHint.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    el.stageHintBox.classList.toggle('hidden');
  });

  el.btnToggleHardcoreHint.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    el.hardcoreHintBox.classList.toggle('hidden');
  });

  // Sound Toggle
  el.btnSound.addEventListener('click', () => {
    const isMuted = sound.toggleMute();
    updateSoundIcon();
    triggerHaptic(10);
    if (!isMuted) sound.playTileClick();
  });

  // Rules Modal
  el.btnRules.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    el.rulesModal.classList.remove('hidden');
  });
  el.btnCloseRules.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    el.rulesModal.classList.add('hidden');
  });
  el.rulesModal.addEventListener('click', (e) => {
    if (e.target === el.rulesModal) el.rulesModal.classList.add('hidden');
  });

  // Stage Select Modal
  el.btnStageSelect.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    renderStageSelectGrid();
    el.stageSelectModal.classList.remove('hidden');
  });
  el.btnCloseStageSelect.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    el.stageSelectModal.classList.add('hidden');
  });
  el.stageSelectModal.addEventListener('click', (e) => {
    if (e.target === el.stageSelectModal) el.stageSelectModal.classList.add('hidden');
  });

  // Stage Filter Tabs
  document.querySelectorAll('.stage-filter-tabs .tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      triggerHaptic(10);
      sound.playTileClick();
      document.querySelectorAll('.stage-filter-tabs .tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentStageFilter = btn.getAttribute('data-level');
      renderStageSelectGrid();
    });
  });

  // Next Round & Restart Buttons
  el.btnNextRound.addEventListener('click', () => {
    triggerHaptic(20);
    sound.playTileClick();
    el.roundClearModal.classList.add('hidden');
    gameState.nextRound();
  });

  el.btnRetryHardcore.addEventListener('click', () => {
    triggerHaptic(20);
    sound.playTileClick();
    el.hardcoreFailModal.classList.add('hidden');
    gameState.retryHardcoreStage();
  });

  el.btnRestartGame.addEventListener('click', () => {
    triggerHaptic(20);
    sound.playTileClick();
    el.gameOverModal.classList.add('hidden');
    gameState.startNewGame();
  });

  // Native Mobile Software Keyboard (Samsung, iPhone, Gboard) Integration
  const mobileInput = document.getElementById('mobileWordleInput');
  const wordleBoardCard = document.getElementById('wordleBoardCard');
  if (wordleBoardCard && mobileInput) {
    wordleBoardCard.addEventListener('click', () => {
      mobileInput.focus();
    });
  }

  if (mobileInput) {
    mobileInput.addEventListener('input', () => {
      const val = mobileInput.value;
      if (!val) return;
      for (const char of val) {
        const { cho, jung, jong, isHangul } = decomposeHangul(char);
        if (isHangul) {
          if (cho) gameState.typeHardcoreJamo(cho);
          if (jung) gameState.typeHardcoreJamo(jung);
          if (jong) gameState.typeHardcoreJamo(jong);
        } else {
          const hangulJamos = 'ㅂㅈㄷㄱㅅㅛㅕㅑㅐㅔㅁㄴㅇㄹㅎㅗㅓㅏㅣㅋㅌㅍㅊㅠㅜㅡ';
          if (hangulJamos.includes(char)) {
            gameState.typeHardcoreJamo(char);
          }
        }
      }
      mobileInput.value = '';
    });

    mobileInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        handleHardcoreSubmit();
      } else if (e.key === 'Backspace') {
        gameState.deleteHardcoreJamo();
      }
    });
  }

  // Global Keyboard Shortcuts (Full Physical Keyboard support for QWERTY & Korean)
  window.addEventListener('keydown', (e) => {
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

    if (!el.roundClearModal.classList.contains('hidden')) {
      if (e.key === 'Enter') el.btnNextRound.click();
      return;
    }
    if (!el.hardcoreFailModal.classList.contains('hidden')) {
      if (e.key === 'Enter') el.btnRetryHardcore.click();
      return;
    }
    if (!el.gameOverModal.classList.contains('hidden')) {
      if (e.key === 'Enter') el.btnRestartGame.click();
      return;
    }

    // Normal Mode Shortcuts
    if (gameState.gameMode === 'normal') {
      if (e.key === 'Enter') {
        if (gameState.canSubmit()) handleSubmitGuess();
      } else if (e.key >= '1' && e.key <= '9') {
        const idx = parseInt(e.key, 10) - 1;
        if (idx < gameState.activeTiles.length) {
          triggerHaptic(15);
          sound.playTileClick();
          gameState.selectTile(idx);
        }
      } else if (e.code === 'KeyR' || e.key === 'r' || e.key === 'R' || e.key === 'ㄱ') {
        if (gameState.selectedTileIndex !== null) {
          triggerHaptic(12);
          sound.playTileRotate();
          gameState.rotateTileAt(gameState.selectedTileIndex);
        }
      } else if (e.code === 'Space') {
        e.preventDefault();
        triggerHaptic(15);
        sound.playTileRotate();
        gameState.shuffleTiles();
      }
    }
    // Hardcore Mode Keyboard Support
    else if (gameState.gameMode === 'hardcore') {
      if (e.key === 'Enter' || e.code === 'Enter' || e.code === 'NumpadEnter') {
        highlightActionKey('key-submit');
        handleHardcoreSubmit();
      } else if (e.key === 'Backspace' || e.code === 'Backspace' || e.key === 'Delete' || e.code === 'Delete') {
        highlightActionKey('key-delete');
        triggerHaptic(10);
        sound.playTileClick();
        gameState.deleteHardcoreJamo();
      } else {
        let jamosToType = [];

        if (e.shiftKey && SHIFT_CODE_TO_HANGUL[e.code]) {
          const shiftJamo = SHIFT_CODE_TO_HANGUL[e.code];
          if (shiftJamo === 'ㄲ') jamosToType = ['ㄱ', 'ㄱ'];
          else if (shiftJamo === 'ㄸ') jamosToType = ['ㄷ', 'ㄷ'];
          else if (shiftJamo === 'ㅃ') jamosToType = ['ㅂ', 'ㅂ'];
          else if (shiftJamo === 'ㅆ') jamosToType = ['ㅅ', 'ㅅ'];
          else if (shiftJamo === 'ㅉ') jamosToType = ['ㅈ', 'ㅈ'];
          else if (shiftJamo === 'ㅒ') jamosToType = ['ㅑ', 'ㅣ'];
          else if (shiftJamo === 'ㅖ') jamosToType = ['ㅕ', 'ㅣ'];
          else jamosToType = [shiftJamo];
        } else if (CODE_TO_HANGUL[e.code]) {
          jamosToType = [CODE_TO_HANGUL[e.code]];
        } else if (e.key && e.key.length === 1) {
          const { cho, jung, jong, isHangul } = decomposeHangul(e.key);
          if (isHangul) {
            if (cho) jamosToType.push(cho);
            if (jung) jamosToType.push(jung);
            if (jong) jamosToType.push(jong);
          } else {
            const hangulJamos = 'ㅂㅈㄷㄱㅅㅛㅕㅑㅐㅔㅁㄴㅇㄹㅎㅗㅓㅏㅣㅋㅌㅍㅊㅠㅜㅡ';
            if (hangulJamos.includes(e.key)) {
              jamosToType = [e.key];
            }
          }
        }

        if (jamosToType.length > 0) {
          jamosToType.forEach(jamo => {
            highlightKeypadButton(jamo);
            triggerHaptic(10);
            sound.playTileClick();
            gameState.typeHardcoreJamo(jamo);
          });
        }
      }
    }
  });
}

function updateSoundIcon() {
  if (sound.muted) {
    el.soundIconOn.classList.add('hidden');
    el.soundIconOff.classList.remove('hidden');
  } else {
    el.soundIconOn.classList.remove('hidden');
    el.soundIconOff.classList.add('hidden');
  }
}

/**
 * Main Render Function
 * @param {GameState} state 
 */
function render(state) {
  if (!state.currentPuzzle) return;

  // Header Stage & Difficulty
  el.roundNumber.textContent = state.stageIndex + 1;
  el.scoreVal.textContent = `${state.score}점`;

  const level = state.currentPuzzle.level || '쉬움';
  el.difficultyPill.textContent = level;
  el.difficultyPill.className = `difficulty-pill diff-${level}`;

  // Mode Switcher Active Tab Toggle
  if (state.gameMode === 'normal') {
    el.btnModeNormal.classList.add('active');
    el.btnModeHardcore.classList.remove('active');
    el.normalModeView.classList.remove('hidden');
    el.hardcoreModeView.classList.add('hidden');
    renderNormalMode(state);
  } else {
    el.btnModeNormal.classList.remove('active');
    el.btnModeHardcore.classList.add('active');
    el.normalModeView.classList.add('hidden');
    el.hardcoreModeView.classList.remove('hidden');
    renderHardcoreMode(state);
  }
}

/* =========================================================================
   NORMAL MODE RENDER
   ========================================================================= */

function renderNormalMode(state) {
  el.targetDesc.textContent = `${state.currentPuzzle.length}글자 (${state.currentPuzzle.targetTiles.length}개 타일)`;
  el.stageHintText.textContent = getWordChosungHint(state.currentPuzzle.answer);

  renderAssembledPreview(state);
  renderTilesTrack(state);
  renderHistory(state);

  el.btnSubmitGuess.disabled = !state.canSubmit();

  if (state.isRoundOver && !state.isGameOver) {
    const lastGuess = state.guesses[state.guesses.length - 1];
    if (lastGuess && lastGuess.isExactMatch && el.roundClearModal.classList.contains('hidden')) {
      showRoundClearModal(state);
    }
  }

  if (state.isGameOver && el.gameOverModal.classList.contains('hidden')) {
    showGameOverModal(state);
  }
}

function renderAssembledPreview(state) {
  const assembled = state.getCurrentAssembled();
  el.previewWordBoxes.innerHTML = '';

  const targetLen = state.currentPuzzle.length;
  const syllables = assembled.syllables || [];

  for (let i = 0; i < targetLen; i++) {
    const box = document.createElement('div');
    const char = syllables[i] || '';
    box.className = `preview-syllable-box ${char ? 'filled' : ''}`;
    box.textContent = char;
    el.previewWordBoxes.appendChild(box);
  }
}

/**
 * Clear drag highlight classes from all tile cards
 */
function clearInsertionStyles() {
  document.querySelectorAll('.draggable-tile-card').forEach(c => {
    c.classList.remove('dragging', 'drag-over', 'insert-left', 'insert-right');
  });
}

/**
 * Calculate the target placement based on cursor / touch coordinates.
 */
function getDropPlacement(clientX, clientY, draggedIndex) {
  const cards = Array.from(document.querySelectorAll('.draggable-tile-card'));
  if (cards.length === 0 || draggedIndex === null) return null;

  const trackContainer = el.tilesTrackContainer || document.getElementById('tilesTrackContainer');
  if (!trackContainer) return null;
  const containerRect = trackContainer.getBoundingClientRect();

  if (
    clientY < containerRect.top - 100 ||
    clientY > containerRect.bottom + 100 ||
    clientX < containerRect.left - 100 ||
    clientX > containerRect.right + 100
  ) {
    return null;
  }

  const firstCard = cards[0];
  const lastCard = cards[cards.length - 1];
  const firstRect = firstCard.getBoundingClientRect();
  const lastRect = lastCard.getBoundingClientRect();

  // 1. Clearly before first card -> insert at 0
  if (clientY <= firstRect.bottom + 10 && clientX < firstRect.left + 20) {
    return {
      targetCard: firstCard,
      cardIndex: 0,
      insertSide: 'left',
      toIndex: 0
    };
  }

  // 2. Clearly after last card -> insert at last index
  if (
    (clientY >= lastRect.top - 15 && clientX > lastRect.right - 20) ||
    clientY > lastRect.bottom + 5
  ) {
    const lastIndex = cards.length - 1;
    return {
      targetCard: lastCard,
      cardIndex: lastIndex,
      insertSide: 'right',
      toIndex: lastIndex
    };
  }

  // 3. Direct card hover
  const elemBelow = document.elementFromPoint(clientX, clientY);
  const directCard = elemBelow ? elemBelow.closest('.draggable-tile-card') : null;

  if (directCard) {
    const cardIndex = parseInt(directCard.getAttribute('data-index'), 10);
    const rect = directCard.getBoundingClientRect();
    const isRight = clientX > (rect.left + rect.width / 2);
    let toIndex;
    if (!isRight) {
      toIndex = draggedIndex < cardIndex ? cardIndex - 1 : cardIndex;
    } else {
      toIndex = draggedIndex < cardIndex ? cardIndex : cardIndex + 1;
    }

    return {
      targetCard: directCard,
      cardIndex,
      insertSide: isRight ? 'right' : 'left',
      toIndex: Math.max(0, Math.min(toIndex, cards.length - 1))
    };
  }

  // 4. Proximity fallback
  let closestCard = null;
  let minDistance = Infinity;

  cards.forEach((card) => {
    const rect = card.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    const dist = Math.hypot(clientX - centerX, clientY - centerY);
    if (dist < minDistance) {
      minDistance = dist;
      closestCard = card;
    }
  });

  if (closestCard) {
    const cardIndex = parseInt(closestCard.getAttribute('data-index'), 10);
    const rect = closestCard.getBoundingClientRect();
    const isRight = clientX > (rect.left + rect.width / 2);
    let toIndex;
    if (!isRight) {
      toIndex = draggedIndex < cardIndex ? cardIndex - 1 : cardIndex;
    } else {
      toIndex = draggedIndex < cardIndex ? cardIndex : cardIndex + 1;
    }

    return {
      targetCard: closestCard,
      cardIndex,
      insertSide: isRight ? 'right' : 'left',
      toIndex: Math.max(0, Math.min(toIndex, cards.length - 1))
    };
  }

  return null;
}

/**
 * Render Draggable Unified Tile Row
 */
function renderTilesTrack(state) {
  el.tilesTrack.innerHTML = '';

  state.activeTiles.forEach((tile, index) => {
    const card = document.createElement('div');
    const isSelected = state.selectedTileIndex === index;
    const canRotate = isRotatable(tile.char);

    card.className = `draggable-tile-card ${isSelected ? 'selected' : ''}`;
    card.setAttribute('draggable', 'true');
    card.setAttribute('data-index', String(index));

    card.innerHTML = `
      <span class="tile-index-badge">${index + 1}</span>
      <span class="tile-char-text">${tile.char}</span>
      ${canRotate ? '<button class="tile-rotate-btn" title="타일 회전 (🔄)" aria-label="회전">🔄</button>' : ''}
    `;

    // Rotate Button
    if (canRotate) {
      const rotateBtn = card.querySelector('.tile-rotate-btn');
      const handleRotate = (e) => {
        e.stopPropagation();
        e.preventDefault();
        triggerHaptic(12);
        sound.playTileRotate();
        gameState.rotateTileAt(index);
      };
      rotateBtn.addEventListener('click', handleRotate);
      rotateBtn.addEventListener('touchend', handleRotate);
    }

    // Tap / Click to select and swap
    card.addEventListener('click', (e) => {
      if (e.target.closest('.tile-rotate-btn')) return;
      triggerHaptic(15);
      sound.playTileClick();
      gameState.selectTile(index);
    });

    // Touch Event Handling for Mobile Drag & Drop
    card.addEventListener('touchstart', (e) => {
      if (e.target.closest('.tile-rotate-btn')) return;
      const touch = e.touches[0];
      touchStartX = touch.clientX;
      touchStartY = touch.clientY;
      touchActiveTile = card;
      draggedIndex = index;
    }, { passive: true });

    card.addEventListener('touchmove', (e) => {
      if (!touchActiveTile || draggedIndex === null) return;
      const touch = e.touches[0];
      const dist = Math.hypot(touch.clientX - touchStartX, touch.clientY - touchStartY);

      if (dist > 6) {
        touchActiveTile.classList.add('dragging');
        const placement = getDropPlacement(touch.clientX, touch.clientY, draggedIndex);

        document.querySelectorAll('.draggable-tile-card').forEach(c => {
          c.classList.remove('insert-left', 'insert-right', 'drag-over');
        });

        if (placement && placement.targetCard && (placement.toIndex !== draggedIndex || placement.targetCard !== touchActiveTile)) {
          placement.targetCard.classList.add(`insert-${placement.insertSide}`);
        }
      }
    }, { passive: true });

    card.addEventListener('touchend', (e) => {
      if (!touchActiveTile || draggedIndex === null) return;
      const changedTouch = e.changedTouches[0];
      const placement = getDropPlacement(changedTouch.clientX, changedTouch.clientY, draggedIndex);

      if (placement && placement.toIndex !== draggedIndex && placement.toIndex >= 0 && placement.toIndex < gameState.activeTiles.length) {
        triggerHaptic(20);
        sound.playTileCombine();
        gameState.moveTile(draggedIndex, placement.toIndex);
      }

      clearInsertionStyles();
      draggedIndex = null;
      touchActiveTile = null;
    });

    // HTML5 Desktop Drag and Drop Events
    card.addEventListener('dragstart', (e) => {
      draggedIndex = index;
      card.classList.add('dragging');
      e.dataTransfer.effectAllowed = 'move';
      e.dataTransfer.setData('text/plain', String(index));
    });

    card.addEventListener('dragend', () => {
      clearInsertionStyles();
      draggedIndex = null;
    });

    card.addEventListener('dragover', (e) => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'move';
      if (draggedIndex !== null) {
        const placement = getDropPlacement(e.clientX, e.clientY, draggedIndex);
        document.querySelectorAll('.draggable-tile-card').forEach(c => {
          c.classList.remove('insert-left', 'insert-right', 'drag-over');
        });
        if (placement && placement.targetCard && placement.toIndex !== draggedIndex) {
          placement.targetCard.classList.add(`insert-${placement.insertSide}`);
        }
      }
    });

    card.addEventListener('drop', (e) => {
      e.preventDefault();
      if (draggedIndex !== null) {
        const placement = getDropPlacement(e.clientX, e.clientY, draggedIndex);
        if (placement && placement.toIndex !== draggedIndex && placement.toIndex >= 0 && placement.toIndex < gameState.activeTiles.length) {
          triggerHaptic(20);
          sound.playTileCombine();
          gameState.moveTile(draggedIndex, placement.toIndex);
        }
      }
      clearInsertionStyles();
      draggedIndex = null;
    });

    el.tilesTrack.appendChild(card);
  });
}

function handleSubmitGuess() {
  const guessEntry = gameState.submitGuess();
  if (!guessEntry) {
    triggerHaptic([40, 40, 40]);
    return;
  }

  triggerHaptic(30);
  if (guessEntry.isExactMatch) {
    sound.playRoundWin();
  } else {
    sound.playTileCombine();
  }
}

function renderHistory(state) {
  el.historyCount.textContent = `${state.guesses.length}회 제출`;

  if (state.guesses.length === 0) {
    el.historyList.innerHTML = `
      <div class="empty-history-placeholder">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 9h6M9 13h6M9 17h3"/></svg>
        <p>타일을 회전하고 배치한 뒤 <strong>[단어 제출]</strong>을 눌러보세요!</p>
      </div>
    `;
    return;
  }

  el.historyList.innerHTML = '';
  const MAX_HISTORY_ITEMS = 5;
  const recentGuesses = state.guesses.slice(-MAX_HISTORY_ITEMS).reverse();

  recentGuesses.forEach((entry) => {
    const attemptNum = state.guesses.indexOf(entry) + 1;
    const row = document.createElement('div');
    const isNew = !entry.hasAnimated;
    row.className = `history-item ${isNew ? 'new-history-entry' : ''}`;

    const tilesHtml = entry.tiles.map((tile, i) => {
      const status = entry.feedback[i];
      return `
        <div class="history-chip ${status}" title="${tile}">
          <span class="chip-char">${tile}</span>
        </div>
      `;
    }).join('');

    entry.hasAnimated = true;

    row.innerHTML = `
      <span class="history-attempt-num">#${attemptNum}</span>
      <div class="history-tiles-box">
        ${tilesHtml}
      </div>
    `;

    el.historyList.appendChild(row);
  });
}

/* =========================================================================
   HARDCORE WORDLE MODE RENDER
   ========================================================================= */

function renderHardcoreMode(state) {
  const numLetters = state.currentPuzzle.length;
  const numTiles = state.currentPuzzle.targetTiles.length;
  el.hardcoreTargetDesc.textContent = `${numLetters}글자 (${numTiles}개 타일 추리)`;
  el.hardcoreAttemptCount.textContent = `시도 ${Math.min(state.hardcoreGuesses.length + 1, 6)} / 6`;
  el.hardcoreHintText.textContent = getWordChosungHint(state.currentPuzzle.answer);

  renderWordleGrid(state);
  renderWordleKeypad(state);

  // Check victory / failure conditions
  if (state.hardcoreIsRoundOver) {
    const lastGuess = state.hardcoreGuesses[state.hardcoreGuesses.length - 1];
    if (lastGuess && lastGuess.isExactMatch && el.roundClearModal.classList.contains('hidden')) {
      showRoundClearModal(state, true);
    } else if (state.hardcoreGuesses.length >= 6 && !lastGuess.isExactMatch && el.hardcoreFailModal.classList.contains('hidden')) {
      showHardcoreFailModal(state);
    }
  }
}

function renderWordleGrid(state) {
  el.wordleGrid.innerHTML = '';
  const numTiles = state.currentPuzzle.targetTiles.length;
  const currentTypedTiles = state.hardcoreInputJamos || [];
  const numGuesses = state.hardcoreGuesses.length;

  for (let r = 0; r < 6; r++) {
    const row = document.createElement('div');
    row.className = 'wordle-row';
    row.id = `wordleRow-${r}`;

    if (r < numGuesses) {
      // Completed Guess Row (Tile-based)
      const guess = state.hardcoreGuesses[r];
      const guessTiles = Array.isArray(guess.tiles) ? guess.tiles : [];
      for (let c = 0; c < numTiles; c++) {
        const tile = document.createElement('div');
        const status = (guess.feedback && guess.feedback[c]) || 'absent';
        tile.className = `wordle-tile status-${status}`;
        tile.textContent = guessTiles[c] || '';
        row.appendChild(tile);
      }
    } else if (r === numGuesses && !state.hardcoreIsRoundOver) {
      // Active In-Progress Row (Tile-based)
      for (let c = 0; c < numTiles; c++) {
        const tile = document.createElement('div');
        const char = currentTypedTiles[c] || '';
        const isCursor = c === currentTypedTiles.length;
        tile.className = `wordle-tile ${char ? 'filled' : ''} ${isCursor ? 'active-cursor' : ''}`;
        tile.textContent = char;
        row.appendChild(tile);
      }
    } else {
      // Empty Future Row
      for (let c = 0; c < numTiles; c++) {
        const tile = document.createElement('div');
        tile.className = 'wordle-tile';
        tile.textContent = '';
        row.appendChild(tile);
      }
    }

    el.wordleGrid.appendChild(row);
  }
}

function renderWordleKeypad(state) {
  el.wordleKeypad.innerHTML = '';

  KEYPAD_ROWS.forEach(rowKeys => {
    const rowEl = document.createElement('div');
    rowEl.className = 'key-row';

    rowKeys.forEach(key => {
      const btn = document.createElement('button');
      if (key === 'ENTER') {
        btn.className = 'wordle-key key-action key-submit';
        btn.innerHTML = '↵ 제출';

        const handleSubmit = (e) => {
          if (e.type === 'touchstart') e.preventDefault();
          highlightActionKey('key-submit');
          handleHardcoreSubmit();
        };
        btn.addEventListener('touchstart', handleSubmit, { passive: false });
        btn.addEventListener('click', handleSubmit);
      } else if (key === 'BACKSPACE') {
        btn.className = 'wordle-key key-action key-delete';
        btn.innerHTML = '⌫';

        const handleDelete = (e) => {
          if (e.type === 'touchstart') e.preventDefault();
          highlightActionKey('key-delete');
          triggerHaptic(12);
          sound.playTileClick();
          gameState.deleteHardcoreJamo();
        };
        btn.addEventListener('touchstart', handleDelete, { passive: false });
        btn.addEventListener('click', handleDelete);
      } else {
        const status = state.hardcoreKeyStates[key];
        btn.className = `wordle-key ${status ? 'key-' + status : ''}`;
        btn.textContent = key;
        btn.setAttribute('data-key', key);

        const handleType = (e) => {
          if (e.type === 'touchstart') e.preventDefault();
          highlightKeypadButton(key);
          triggerHaptic(12);
          sound.playTileClick();
          gameState.typeHardcoreJamo(key);
        };
        btn.addEventListener('touchstart', handleType, { passive: false });
        btn.addEventListener('click', handleType);
      }
      rowEl.appendChild(btn);
    });

    el.wordleKeypad.appendChild(rowEl);
  });
}

function handleHardcoreSubmit() {
  const numTiles = gameState.currentPuzzle.targetTiles.length;

  if (gameState.hardcoreInputJamos.length !== numTiles) {
    // Shake active row
    const activeRowIndex = gameState.hardcoreGuesses.length;
    const rowEl = document.getElementById(`wordleRow-${activeRowIndex}`);
    if (rowEl) {
      rowEl.classList.remove('row-shake');
      void rowEl.offsetWidth; // trigger reflow
      rowEl.classList.add('row-shake');
    }
    triggerHaptic([40, 40, 40]);
    return;
  }

  const guessEntry = gameState.submitHardcoreGuess();
  if (!guessEntry) return;

  triggerHaptic(30);
  if (guessEntry.isExactMatch) {
    sound.playRoundWin();
  } else {
    sound.playTileCombine();
  }
}

function showHardcoreFailModal(state) {
  triggerHaptic([60, 60, 60]);
  el.hardcoreFailWord.textContent = `정답 단어: "${state.currentPuzzle.answer}"`;
  el.hardcoreFailModal.classList.remove('hidden');
}

/* =========================================================================
   COMMON MODALS & STAGE SELECT
   ========================================================================= */

function renderStageSelectGrid() {
  el.stagesGrid.innerHTML = '';

  STAGES_100.forEach((stage, idx) => {
    if (currentStageFilter !== 'all' && stage.level !== currentStageFilter) return;

    const isCurrent = gameState.stageIndex === idx;
    const isCleared = gameState.clearedStages && gameState.clearedStages.includes(idx);

    const btn = document.createElement('button');
    btn.className = `stage-card-btn ${isCurrent ? 'current' : ''} ${isCleared ? 'cleared' : ''}`;
    btn.innerHTML = `
      <span class="stage-card-num">${stage.stage}단계 ${isCleared ? '<span class="stage-cleared-badge">✓</span>' : ''}</span>
      <span class="stage-card-len">${stage.length}글자</span>
      <span class="stage-card-diff diff-${stage.level}">${stage.level}</span>
    `;

    btn.addEventListener('click', () => {
      triggerHaptic(15);
      sound.playTileClick();
      gameState.loadStage(idx);
      el.stageSelectModal.classList.add('hidden');
    });

    el.stagesGrid.appendChild(btn);
  });
}

function showRoundClearModal(state, isHardcore = false) {
  triggerHaptic([50, 60, 50, 60, 100]);
  triggerConfetti();

  const points = (state.currentPuzzle.points || state.currentPuzzle.length) * (isHardcore ? 2 : 1);
  el.roundClearTitle.textContent = isHardcore ? '🔥 하드코어 워들 클리어!' : '🎉 정답입니다!';
  el.roundClearWord.textContent = `정답: ${state.currentPuzzle.answer}`;
  el.awardedPoints.textContent = `+${points}점 ${isHardcore ? '(2배 보너스)' : ''}`;

  el.roundClearModal.classList.remove('hidden');
}

function showGameOverModal(state) {
  triggerHaptic([100, 50, 100, 50, 200]);
  triggerConfetti(true);
  el.finalScoreText.textContent = `${state.score}점`;
  el.gameOverModal.classList.remove('hidden');
}

function triggerConfetti(extended = false) {
  const canvas = el.confettiCanvas;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  const particles = [];
  const count = extended ? 120 : 60;
  const colors = ['#6366f1', '#a855f7', '#06b6d4', '#10b981', '#fbbf24', '#f43f5e'];

  for (let i = 0; i < count; i++) {
    particles.push({
      x: canvas.width / 2,
      y: canvas.height / 2,
      vx: (Math.random() - 0.5) * 16,
      vy: (Math.random() - 0.7) * 16,
      size: Math.random() * 8 + 4,
      color: colors[Math.floor(Math.random() * colors.length)],
      rotation: Math.random() * 360,
      vRot: (Math.random() - 0.5) * 10,
      opacity: 1
    });
  }

  let startTime = performance.now();
  const duration = extended ? 3000 : 1800;

  function animate(now) {
    const elapsed = now - startTime;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    particles.forEach(p => {
      p.x += p.vx;
      p.y += p.vy;
      p.vy += 0.35; // gravity
      p.rotation += p.vRot;
      p.opacity = Math.max(0, 1 - (elapsed / duration));

      ctx.save();
      ctx.globalAlpha = p.opacity;
      ctx.translate(p.x, p.y);
      ctx.rotate((p.rotation * Math.PI) / 180);
      ctx.fillStyle = p.color;
      ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size);
      ctx.restore();
    });

    if (elapsed < duration) {
      requestAnimationFrame(animate);
    } else {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
  }

  requestAnimationFrame(animate);
}

// Start Application
window.addEventListener('DOMContentLoaded', init);
