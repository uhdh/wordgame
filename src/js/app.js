/**
 * <언어의 조각> Application Controller (Mobile Optimized, Touch Drag & Drop, Click-to-Swap, 100 Stages)
 */

import { gameState } from './gameState.js';
import { sound } from './audioEffects.js';
import { isRotatable } from './hangulEngine.js';
import { STAGES_100 } from './stages.js';

// DOM Elements
const el = {
  roundNumber: document.getElementById('roundNumber'),
  difficultyPill: document.getElementById('difficultyPill'),
  scoreVal: document.getElementById('scoreVal'),
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
  roundClearWord: document.getElementById('roundClearWord'),
  awardedPoints: document.getElementById('awardedPoints'),
  btnNextRound: document.getElementById('btnNextRound'),
  gameOverModal: document.getElementById('gameOverModal'),
  finalScoreText: document.getElementById('finalScoreText'),
  btnRestartGame: document.getElementById('btnRestartGame'),
  confettiCanvas: document.getElementById('confettiCanvas')
};

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
  // Submit Guess
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

  // Hint Toggle
  el.btnToggleHint.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    el.stageHintBox.classList.toggle('hidden');
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

  el.btnRestartGame.addEventListener('click', () => {
    triggerHaptic(20);
    sound.playTileClick();
    el.gameOverModal.classList.add('hidden');
    gameState.startNewGame();
  });

  // Global Keyboard Shortcuts
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      if (!el.roundClearModal.classList.contains('hidden')) {
        el.btnNextRound.click();
      } else if (!el.gameOverModal.classList.contains('hidden')) {
        el.btnRestartGame.click();
      } else if (gameState.canSubmit()) {
        handleSubmitGuess();
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

  // Header & Info Updates
  el.roundNumber.textContent = state.stageIndex + 1;
  el.scoreVal.textContent = `${state.score}점`;
  
  const level = state.currentPuzzle.level || '쉬움';
  el.difficultyPill.textContent = level;
  el.difficultyPill.className = `difficulty-pill diff-${level}`;

  el.targetDesc.textContent = `${state.currentPuzzle.length}글자 (${state.currentPuzzle.targetTiles.length}개 타일)`;
  el.stageHintText.textContent = state.currentPuzzle.chosungHint;

  // Render Assembled Word Preview
  renderAssembledPreview(state);

  // Render Draggable 1-Line Tile Row
  renderTilesTrack(state);

  // Render History List
  renderHistory(state);

  // Submit button state
  el.btnSubmitGuess.disabled = !state.canSubmit();

  // Check Round Clear or Game Victory
  if (state.isRoundOver && !state.isGameOver) {
    showRoundClearModal(state);
  } else if (state.isGameOver) {
    showGameOverModal(state);
  }
}

/**
 * Render Live Assembled Word Preview
 */
function renderAssembledPreview(state) {
  const assembled = state.getCurrentAssembled();
  el.previewWordBoxes.innerHTML = '';

  const targetLen = state.currentPuzzle.length;
  for (let i = 0; i < targetLen; i++) {
    const char = assembled.syllables[i] || '';
    const box = document.createElement('div');
    box.className = `preview-char-box ${char ? 'filled' : ''}`;
    box.textContent = char || '?';
    el.previewWordBoxes.appendChild(box);
  }

  if (assembled.syllables.length > targetLen) {
    for (let i = targetLen; i < assembled.syllables.length; i++) {
      const box = document.createElement('div');
      box.className = 'preview-char-box filled';
      box.textContent = assembled.syllables[i];
      el.previewWordBoxes.appendChild(box);
    }
  }
}

/**
 * Render Draggable Unified 1-Line Tile Row (with Mobile Touch Support)
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
      const deltaX = Math.abs(touch.clientX - touchStartX);
      const deltaY = Math.abs(touch.clientY - touchStartY);

      // If moving horizontally across tiles, mark dragging
      if (deltaX > 10 && deltaX > deltaY) {
        touchActiveTile.classList.add('dragging');
        const elemBelow = document.elementFromPoint(touch.clientX, touch.clientY);
        const targetCard = elemBelow ? elemBelow.closest('.draggable-tile-card') : null;

        document.querySelectorAll('.draggable-tile-card').forEach(c => c.classList.remove('drag-over'));
        if (targetCard && targetCard !== touchActiveTile) {
          targetCard.classList.add('drag-over');
        }
      }
    }, { passive: true });

    card.addEventListener('touchend', (e) => {
      if (!touchActiveTile || draggedIndex === null) return;
      const changedTouch = e.changedTouches[0];
      const elemBelow = document.elementFromPoint(changedTouch.clientX, changedTouch.clientY);
      const targetCard = elemBelow ? elemBelow.closest('.draggable-tile-card') : null;

      document.querySelectorAll('.draggable-tile-card').forEach(c => {
        c.classList.remove('dragging');
        c.classList.remove('drag-over');
      });

      if (targetCard) {
        const targetIndex = parseInt(targetCard.getAttribute('data-index'), 10);
        if (!isNaN(targetIndex) && targetIndex !== draggedIndex) {
          triggerHaptic(20);
          sound.playTileCombine();
          gameState.moveTile(draggedIndex, targetIndex);
        }
      }

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
      card.classList.remove('dragging');
      document.querySelectorAll('.draggable-tile-card').forEach(c => c.classList.remove('drag-over'));
      draggedIndex = null;
    });

    card.addEventListener('dragover', (e) => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'move';
      if (draggedIndex !== null && draggedIndex !== index) {
        card.classList.add('drag-over');
      }
    });

    card.addEventListener('dragleave', () => {
      card.classList.remove('drag-over');
    });

    card.addEventListener('drop', (e) => {
      e.preventDefault();
      card.classList.remove('drag-over');
      if (draggedIndex !== null && draggedIndex !== index) {
        triggerHaptic(20);
        sound.playTileCombine();
        gameState.moveTile(draggedIndex, index);
      }
    });

    el.tilesTrack.appendChild(card);
  });
}

/**
 * Handle Submit Guess
 */
function handleSubmitGuess() {
  const guessEntry = gameState.submitGuess();
  if (!guessEntry) {
    triggerHaptic([40, 40, 40]);
    sound.playError();
    return;
  }

  triggerHaptic([20, 40, 20]);

  // Play audio tones for tile flip
  guessEntry.feedback.forEach((status, idx) => {
    sound.playFlip(status, idx);
  });
}

/**
 * Render Guess History (Wordle Board with tile-based colors, without assembled word)
 */
function renderHistory(state) {
  el.historyCount.textContent = `${state.guesses.length}회 제출`;

  if (state.guesses.length === 0) {
    el.historyList.innerHTML = `
      <div class="empty-history-placeholder">
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 9h6M9 13h6M9 17h3"/></svg>
        <p>타일을 회전하고 배치한 뒤 <strong>[단어 제출]</strong>을 눌러 결과를 확인해보세요!</p>
      </div>
    `;
    return;
  }

  el.historyList.innerHTML = '';

  state.guesses.forEach((entry, attemptIdx) => {
    const row = document.createElement('div');
    row.className = 'history-row';

    // Animation should only trigger when the entry is freshly created (first render)
    const shouldAnimate = !entry.hasAnimated;

    const tilesHtml = entry.tiles.map((tileChar, tileIdx) => {
      const status = entry.feedback[tileIdx] || 'absent';
      return `
        <div class="history-tile-card status-${status} ${shouldAnimate ? 'flip-anim' : ''}" style="${shouldAnimate ? `animation-delay: ${tileIdx * 0.04}s;` : ''}">
          ${tileChar}
        </div>
      `;
    }).join('');

    // Mark that this entry has performed its initial registration animation
    entry.hasAnimated = true;

    row.innerHTML = `
      <div class="history-row-left">
        <span class="history-attempt-num">#${attemptIdx + 1}</span>
        <div class="history-tiles-box">
          ${tilesHtml}
        </div>
      </div>
      <div class="history-summary-pills">
        ${entry.summary.green > 0 ? `<span class="summary-tag tag-green">🟩 ${entry.summary.green}</span>` : ''}
        ${entry.summary.yellow > 0 ? `<span class="summary-tag tag-yellow">🟨 ${entry.summary.yellow}</span>` : ''}
        ${entry.summary.red > 0 ? `<span class="summary-tag tag-red">🟥 ${entry.summary.red}</span>` : ''}
      </div>
    `;

    el.historyList.appendChild(row);
  });

  // Scroll to latest
  el.historyList.scrollTop = el.historyList.scrollHeight;
}

/**
 * Render Stage Select Grid
 */
function renderStageSelectGrid() {
  el.stagesGrid.innerHTML = '';

  STAGES_100.forEach((stage, idx) => {
    if (currentStageFilter !== 'all' && stage.level !== currentStageFilter) return;

    const isCurrent = gameState.stageIndex === idx;
    const btn = document.createElement('button');
    btn.className = `stage-card-btn ${isCurrent ? 'current' : ''}`;
    btn.innerHTML = `
      <span class="stage-card-num">${stage.stage}단계</span>
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

/**
 * Show Round Clear Celebration Modal
 */
function showRoundClearModal(state) {
  triggerHaptic([50, 60, 50, 60, 100]);
  sound.playWin();
  triggerConfetti();

  el.roundClearWord.textContent = `정답: ${state.currentPuzzle.answer}`;
  el.awardedPoints.textContent = `+${state.currentPuzzle.points || state.currentPuzzle.length}점`;
  el.roundClearModal.classList.remove('hidden');
}

/**
 * Show Final Victory Modal
 */
function showGameOverModal(state) {
  triggerHaptic([80, 80, 80, 80, 200]);
  sound.playWin();
  triggerConfetti();

  el.finalScoreText.textContent = `${state.score}점`;
  el.gameOverModal.classList.remove('hidden');
}

/**
 * Confetti Canvas Particle Generator
 */
function triggerConfetti() {
  const canvas = el.confettiCanvas;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  const particles = [];
  const colors = ['#10b981', '#f59e0b', '#ef4444', '#6366f1', '#06b6d4', '#fbbf24', '#ffffff'];

  const count = window.innerWidth < 480 ? 70 : 120;
  for (let i = 0; i < count; i++) {
    particles.push({
      x: canvas.width / 2,
      y: canvas.height / 2,
      vx: (Math.random() - 0.5) * 16,
      vy: (Math.random() - 0.8) * 18,
      size: Math.random() * 7 + 3,
      color: colors[Math.floor(Math.random() * colors.length)],
      rotation: Math.random() * 360,
      rotSpeed: (Math.random() - 0.5) * 10,
      opacity: 1
    });
  }

  let animationFrame;
  function update() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    let alive = false;

    particles.forEach(p => {
      p.x += p.vx;
      p.y += p.vy;
      p.vy += 0.35;
      p.rotation += p.rotSpeed;
      p.opacity -= 0.01;

      if (p.opacity > 0) {
        alive = true;
        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.rotate((p.rotation * Math.PI) / 180);
        ctx.fillStyle = p.color;
        ctx.globalAlpha = p.opacity;
        ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size);
        ctx.restore();
      }
    });

    if (alive) {
      animationFrame = requestAnimationFrame(update);
    } else {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      cancelAnimationFrame(animationFrame);
    }
  }

  update();
}

document.addEventListener('DOMContentLoaded', init);
