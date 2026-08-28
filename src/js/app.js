/**
 * <언어의 조각> Application Controller (100 Stages, Chosung Hints, Rotatable-Only Buttons, Click/Drag Swap)
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

// Drag & Drop State
let draggedIndex = null;
let currentStageFilter = 'all';

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
    sound.playTileRotate();
    gameState.shuffleTiles();
  });

  // Reset Tiles
  el.btnResetTiles.addEventListener('click', () => {
    sound.playTileClick();
    gameState.resetTiles();
  });

  // Hint Toggle
  el.btnToggleHint.addEventListener('click', () => {
    sound.playTileClick();
    el.stageHintBox.classList.toggle('hidden');
  });

  // Sound Toggle
  el.btnSound.addEventListener('click', () => {
    const isMuted = sound.toggleMute();
    updateSoundIcon();
    if (!isMuted) sound.playTileClick();
  });

  // Rules Modal
  el.btnRules.addEventListener('click', () => {
    sound.playTileClick();
    el.rulesModal.classList.remove('hidden');
  });
  el.btnCloseRules.addEventListener('click', () => {
    sound.playTileClick();
    el.rulesModal.classList.add('hidden');
  });
  el.rulesModal.addEventListener('click', (e) => {
    if (e.target === el.rulesModal) el.rulesModal.classList.add('hidden');
  });

  // Stage Select Modal
  el.btnStageSelect.addEventListener('click', () => {
    sound.playTileClick();
    renderStageSelectGrid();
    el.stageSelectModal.classList.remove('hidden');
  });
  el.btnCloseStageSelect.addEventListener('click', () => {
    sound.playTileClick();
    el.stageSelectModal.classList.add('hidden');
  });
  el.stageSelectModal.addEventListener('click', (e) => {
    if (e.target === el.stageSelectModal) el.stageSelectModal.classList.add('hidden');
  });

  // Stage Filter Tabs
  document.querySelectorAll('.stage-filter-tabs .tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      sound.playTileClick();
      document.querySelectorAll('.stage-filter-tabs .tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentStageFilter = btn.getAttribute('data-level');
      renderStageSelectGrid();
    });
  });

  // Next Round & Restart Buttons
  el.btnNextRound.addEventListener('click', () => {
    sound.playTileClick();
    el.roundClearModal.classList.add('hidden');
    gameState.nextRound();
  });

  el.btnRestartGame.addEventListener('click', () => {
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

  el.targetDesc.textContent = `${state.currentPuzzle.length}글자 정답 단어 (${state.currentPuzzle.targetTiles.length}개 타일 조합)`;
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
 * Render Draggable Unified 1-Line Tile Row
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
      ${canRotate ? '<button class="tile-rotate-btn" title="타일 회전 (🔄)">🔄</button>' : ''}
    `;

    // Rotate Button (Only rendered on rotatable tiles!)
    if (canRotate) {
      const rotateBtn = card.querySelector('.tile-rotate-btn');
      rotateBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        sound.playTileRotate();
        gameState.rotateTileAt(index);
      });
    }

    // Click on tile card to select / swap
    card.addEventListener('click', () => {
      sound.playTileClick();
      gameState.selectTile(index);
    });

    // Drag and Drop Events
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
    sound.playError();
    return;
  }

  // Play audio tones for tile flip
  guessEntry.feedback.forEach((status, idx) => {
    sound.playFlip(status, idx);
  });
}

/**
 * Render Guess History (Wordle Board with tile-based colors)
 */
function renderHistory(state) {
  el.historyCount.textContent = `${state.guesses.length}회 제출`;

  if (state.guesses.length === 0) {
    el.historyList.innerHTML = `
      <div class="empty-history-placeholder">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 9h6M9 13h6M9 17h3"/></svg>
        <p>타일을 회전하고 배치한 뒤 <strong>[단어 제출]</strong>을 눌러 결과를 확인해보세요!</p>
      </div>
    `;
    return;
  }

  el.historyList.innerHTML = '';

  state.guesses.forEach((entry, attemptIdx) => {
    const row = document.createElement('div');
    row.className = 'history-row';

    const tilesHtml = entry.tiles.map((tileChar, tileIdx) => {
      const status = entry.feedback[tileIdx] || 'absent';
      const isLatest = attemptIdx === state.guesses.length - 1;
      return `
        <div class="history-tile-card status-${status} ${isLatest ? 'flip-anim' : ''}" style="animation-delay: ${tileIdx * 0.05}s">
          ${tileChar}
        </div>
      `;
    }).join('');

    row.innerHTML = `
      <div class="history-row-left">
        <span class="history-attempt-num">#${attemptIdx + 1}</span>
        ${entry.word ? `<span class="history-word-tag">${entry.word}</span>` : ''}
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
      <span class="stage-card-len">${stage.length}글자 단어</span>
      <span class="stage-card-diff diff-${stage.level}">${stage.level}</span>
    `;

    btn.addEventListener('click', () => {
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

  for (let i = 0; i < 120; i++) {
    particles.push({
      x: canvas.width / 2,
      y: canvas.height / 2,
      vx: (Math.random() - 0.5) * 16,
      vy: (Math.random() - 0.8) * 18,
      size: Math.random() * 8 + 4,
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
      p.opacity -= 0.009;

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
