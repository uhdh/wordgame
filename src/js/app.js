/**
 * <언어의 조각> Application Controller (Game Hub Unified, Leaderboard & Mobile Drag-and-Drop)
 */

import { gameState } from './gameState.js';
import { sound } from './audioEffects.js';
import { isRotatable, getWordChosungHint } from './hangulEngine.js';
import { STAGES_100 } from './stages.js';

// Supabase & Ranking Config
const SUPABASE_URL = 'https://paktzmofotvwfdxcpmzv.supabase.co';
const SUPABASE_ANON_KEY = 'sb_publishable_jWbstEn2pKJTNDxLTR4Jig_asglvzGW';
const SUPABASE_TABLE = 'wordgame_leaderboard';
const NICKNAME_KEY = 'wordgameNickname';

const NICKNAME_POOL = [
  '이상민', '정근우', '박지민', '이태균', '하승진', '현성주', '윤비', '이진형', '홍진호', '서출구',
  '최혜선', '허성범', '김경훈', '김유현', '김남희', '강지후', '곽범', '이관희', '신승용', '최연청', '덕후'
];

function randomNickname() {
  const word = NICKNAME_POOL[Math.floor(Math.random() * NICKNAME_POOL.length)];
  const num = Math.floor(Math.random() * 999) + 1;
  return `${word}#${num}`;
}

function getOrCreateStoredNickname(key) {
  const saved = localStorage.getItem(key);
  if (saved) return saved;
  const generated = randomNickname();
  localStorage.setItem(key, generated);
  return generated;
}

// DOM Elements
const el = {
  // Header & Meta
  roundNumber: document.getElementById('roundNumber'),
  difficultyPill: document.getElementById('difficultyPill'),
  scoreVal: document.getElementById('scoreVal'),

  // Game Elements
  normalModeView: document.getElementById('normalModeView'),
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
  btnOpenLeaderboardFromClear: document.getElementById('btnOpenLeaderboardFromClear'),
  gameOverModal: document.getElementById('gameOverModal'),
  finalScoreText: document.getElementById('finalScoreText'),
  btnRestartGame: document.getElementById('btnRestartGame'),
  confettiCanvas: document.getElementById('confettiCanvas'),

  // Leaderboard Elements
  btnLeaderboard: document.getElementById('btnLeaderboard'),
  leaderboardModal: document.getElementById('leaderboardModal'),
  btnCloseLeaderboard: document.getElementById('btnCloseLeaderboard'),
  leaderboardNicknameInput: document.getElementById('leaderboardNicknameInput'),
  btnSubmitScore: document.getElementById('btnSubmitScore'),
  leaderboardStatusMsg: document.getElementById('leaderboardStatusMsg'),
  leaderboardList: document.getElementById('leaderboardList'),

  // Share & Toast
  btnShareStage: document.getElementById('btnShareStage'),
  toastNotification: document.getElementById('toastNotification')
};

// Drag & Drop State
let draggedIndex = null;
let touchStartX = 0;
let touchStartY = 0;
let touchActiveTile = null;
let hasDraggedThisTouch = false;
let currentStageFilter = 'all';

function triggerHaptic(pattern = 10) {
  if (typeof navigator !== 'undefined' && navigator.vibrate) {
    try {
      navigator.vibrate(pattern);
    } catch (e) {}
  }
}

function syncUrlWithStage(stageIndex) {
  const stageNum = stageIndex + 1;
  const currentUrl = new URL(window.location.href);
  if (currentUrl.searchParams.get('stage') !== String(stageNum)) {
    currentUrl.searchParams.set('stage', stageNum);
    window.history.replaceState({ stage: stageNum }, '', currentUrl.toString());
  }
}

let toastTimer = null;
function showToast(msg) {
  if (!el.toastNotification) return;
  el.toastNotification.textContent = msg;
  el.toastNotification.classList.add('show');
  if (toastTimer) clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    el.toastNotification.classList.remove('show');
    toastTimer = null;
  }, 2400);
}

async function handleShareStage() {
  triggerHaptic(15);
  sound.playTileClick();
  const stageNum = gameState.stageIndex + 1;
  const currentStageObj = STAGES_100[gameState.stageIndex];
  const wordLength = currentStageObj ? currentStageObj.word.length : 3;
  const shareTitle = `<언어의 조각> STAGE ${stageNum}`;
  const shareText = `<언어의 조각> STAGE ${stageNum} (${wordLength}글자 퍼즐)에 도전해보세요!`;
  const shareUrl = `${window.location.origin}${window.location.pathname}?stage=${stageNum}`;

  if (navigator.share) {
    try {
      await navigator.share({
        title: shareTitle,
        text: shareText,
        url: shareUrl,
      });
      showToast(`STAGE ${stageNum} URL을 공유했습니다!`);
      return;
    } catch (e) {
      if (e.name === 'AbortError') return;
    }
  }

  try {
    await navigator.clipboard.writeText(shareUrl);
    showToast(`🔗 STAGE ${stageNum} URL이 클립보드에 복사되었습니다!`);
  } catch (err) {
    const tempInput = document.createElement('input');
    tempInput.value = shareUrl;
    document.body.appendChild(tempInput);
    tempInput.select();
    document.execCommand('copy');
    document.body.removeChild(tempInput);
    showToast(`🔗 STAGE ${stageNum} URL이 클립보드에 복사되었습니다!`);
  }
}

/**
 * Initialize Application
 */
function init() {
  gameState.subscribe(render);
  bindEvents();

  const urlParams = new URLSearchParams(window.location.search);
  const paramStage = urlParams.get('stage') || urlParams.get('s');
  if (paramStage) {
    const parsedStage = parseInt(paramStage, 10);
    if (!isNaN(parsedStage) && parsedStage >= 1 && parsedStage <= 100) {
      gameState.stageIndex = parsedStage - 1;
    }
  }

  gameState.loadStage(gameState.stageIndex);
  syncUrlWithStage(gameState.stageIndex);
  updateSoundIcon();

  if (el.leaderboardNicknameInput) {
    el.leaderboardNicknameInput.value = getOrCreateStoredNickname(NICKNAME_KEY);
  }
}

/**
 * Bind DOM Event Listeners
 */
function bindEvents() {
  // Share Stage Button
  if (el.btnShareStage) {
    el.btnShareStage.addEventListener('click', handleShareStage);
  }
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

  // Hint Toggle
  el.btnToggleHint.addEventListener('click', () => {
    triggerHaptic(10);
    sound.playTileClick();
    const isOpening = el.stageHintBox.classList.contains('hidden');
    el.stageHintBox.classList.toggle('hidden');
    if (isOpening) {
      gameState.useHint();
    }
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

  // Leaderboard Modal Events
  if (el.btnLeaderboard) {
    el.btnLeaderboard.addEventListener('click', () => {
      triggerHaptic(10);
      sound.playTileClick();
      openLeaderboardModal();
    });
  }
  if (el.btnOpenLeaderboardFromClear) {
    el.btnOpenLeaderboardFromClear.addEventListener('click', () => {
      triggerHaptic(10);
      sound.playTileClick();
      openLeaderboardModal();
    });
  }
  if (el.btnCloseLeaderboard) {
    el.btnCloseLeaderboard.addEventListener('click', () => {
      triggerHaptic(10);
      sound.playTileClick();
      el.leaderboardModal.classList.add('hidden');
    });
  }
  if (el.leaderboardModal) {
    el.leaderboardModal.addEventListener('click', (e) => {
      if (e.target === el.leaderboardModal) el.leaderboardModal.classList.add('hidden');
    });
  }
  if (el.btnSubmitScore) {
    el.btnSubmitScore.addEventListener('click', handleScoreSubmit);
  }
  if (el.leaderboardNicknameInput) {
    el.leaderboardNicknameInput.addEventListener('change', () => {
      const v = el.leaderboardNicknameInput.value.trim();
      if (v) localStorage.setItem(NICKNAME_KEY, v);
      else el.leaderboardNicknameInput.value = getOrCreateStoredNickname(NICKNAME_KEY);
    });
  }

  // Global Keyboard Shortcuts
  window.addEventListener('keydown', (e) => {
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

    if (!el.roundClearModal.classList.contains('hidden')) {
      if (e.key === 'Enter') el.btnNextRound.click();
      return;
    }
    if (!el.gameOverModal.classList.contains('hidden')) {
      if (e.key === 'Enter') el.btnRestartGame.click();
      return;
    }

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

  el.roundNumber.textContent = state.stageIndex + 1;
  el.scoreVal.textContent = `${state.score}점`;

  const level = state.currentPuzzle.level || '쉬움';
  el.difficultyPill.textContent = level;
  el.difficultyPill.className = `difficulty-pill diff-${level}`;

  syncUrlWithStage(state.stageIndex);
  renderNormalMode(state);
}

function renderNormalMode(state) {
  el.targetDesc.textContent = `${state.currentPuzzle.length}글자 (${state.currentPuzzle.targetTiles.length}개 타일)`;
  el.stageHintText.textContent = getWordChosungHint(state.currentPuzzle.answer);

  if (state.hintUsed) {
    el.stageHintBox.classList.remove('hidden');
    el.btnToggleHint.title = '초성 힌트 확인 (사용됨 -1점)';
  } else {
    el.stageHintBox.classList.add('hidden');
    el.btnToggleHint.title = '초성 힌트 확인 (사용 시 -1점)';
  }

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

function clearInsertionStyles() {
  document.querySelectorAll('.draggable-tile-card').forEach(c => {
    c.classList.remove('dragging', 'drag-over', 'insert-left', 'insert-right');
  });
}

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

  if (clientY <= firstRect.bottom + 10 && clientX < firstRect.left + 20) {
    return { targetCard: firstCard, cardIndex: 0, insertSide: 'left', toIndex: 0 };
  }

  if (
    (clientY >= lastRect.top - 15 && clientX > lastRect.right - 20) ||
    clientY > lastRect.bottom + 5
  ) {
    const lastIndex = cards.length - 1;
    return { targetCard: lastCard, cardIndex: lastIndex, insertSide: 'right', toIndex: lastIndex };
  }

  const elemBelow = document.elementFromPoint(clientX, clientY);
  const directCard = elemBelow ? elemBelow.closest('.draggable-tile-card') : null;

  if (directCard) {
    const cardIndex = parseInt(directCard.getAttribute('data-index'), 10);
    const rect = directCard.getBoundingClientRect();
    const isRight = clientX > (rect.left + rect.width / 2);
    let toIndex = !isRight ? (draggedIndex < cardIndex ? cardIndex - 1 : cardIndex) : (draggedIndex < cardIndex ? cardIndex : cardIndex + 1);

    return {
      targetCard: directCard,
      cardIndex,
      insertSide: isRight ? 'right' : 'left',
      toIndex: Math.max(0, Math.min(toIndex, cards.length - 1))
    };
  }

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
    let toIndex = !isRight ? (draggedIndex < cardIndex ? cardIndex - 1 : cardIndex) : (draggedIndex < cardIndex ? cardIndex : cardIndex + 1);

    return {
      targetCard: closestCard,
      cardIndex,
      insertSide: isRight ? 'right' : 'left',
      toIndex: Math.max(0, Math.min(toIndex, cards.length - 1))
    };
  }

  return null;
}

function renderTilesTrack(state) {
  el.tilesTrack.innerHTML = '';

  const lastGuess = state.guesses && state.guesses.length > 0 ? state.guesses[state.guesses.length - 1] : null;
  const isMatchLastGuess = lastGuess && lastGuess.tiles &&
    lastGuess.tiles.length === state.activeTiles.length &&
    lastGuess.tiles.every((c, i) => c === state.activeTiles[i].char);

  state.activeTiles.forEach((tile, index) => {
    const card = document.createElement('div');
    const isSelected = state.selectedTileIndex === index;
    const canRotate = isRotatable(tile.char);

    let statusClass = '';
    if (isMatchLastGuess && lastGuess.feedback && lastGuess.feedback[index]) {
      statusClass = `status-${lastGuess.feedback[index]}`;
    }

    card.className = `draggable-tile-card ${canRotate ? 'rotatable-tile' : ''} ${statusClass}`.trim();
    card.setAttribute('draggable', 'true');
    card.setAttribute('data-index', String(index));

    card.innerHTML = `
      <span class="tile-index-badge">${index + 1}</span>
      <span class="tile-char-text">${tile.char}</span>
      ${canRotate ? '<button class="tile-rotate-btn" title="타일 회전 (🔄)" aria-label="회전">🔄</button>' : ''}
    `;

    // Click/Tap on the tile chip ALWAYS rotates if rotatable (NO tap-to-swap)!
    card.addEventListener('click', () => {
      if (hasDraggedThisTouch) {
        hasDraggedThisTouch = false;
        return;
      }
      if (canRotate) {
        triggerHaptic(12);
        sound.playTileRotate();
        gameState.rotateTileAt(index);
      } else {
        triggerHaptic(8);
      }
    });

    card.addEventListener('touchstart', (e) => {
      const touch = e.touches[0];
      touchStartX = touch.clientX;
      touchStartY = touch.clientY;
      touchActiveTile = card;
      draggedIndex = index;
      hasDraggedThisTouch = false;
    }, { passive: true });

    card.addEventListener('touchmove', (e) => {
      if (!touchActiveTile || draggedIndex === null) return;
      const touch = e.touches[0];
      const dist = Math.hypot(touch.clientX - touchStartX, touch.clientY - touchStartY);

      if (dist > 6) {
        hasDraggedThisTouch = true;
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

      if (hasDraggedThisTouch) {
        const changedTouch = e.changedTouches[0];
        const placement = getDropPlacement(changedTouch.clientX, changedTouch.clientY, draggedIndex);

        if (placement && placement.toIndex !== draggedIndex && placement.toIndex >= 0 && placement.toIndex < gameState.activeTiles.length) {
          triggerHaptic(20);
          sound.playTileCombine();
          gameState.moveTile(draggedIndex, placement.toIndex);
        }
      }

      clearInsertionStyles();
      draggedIndex = null;
      touchActiveTile = null;
    });

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
    // Automatically submit score to Supabase background
    autoSubmitScoreToLeaderboard();
  } else {
    sound.playTileCombine();
  }
}

function renderHistory(state) {
  const attempts = state.guesses.length;
  let penaltyBadge = '';
  if (attempts > 5) {
    penaltyBadge = ' <span class="penalty-badge" title="5회 초과 시 -1점 감점">⚠️ 5회 초과 (-1점)</span>';
  }
  el.historyCount.innerHTML = `${attempts}회 제출${penaltyBadge}`;

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
    row.className = 'history-item';

    const tilesHtml = entry.tiles.map((tile, i) => {
      const status = entry.feedback[i];
      return `
        <div class="history-chip ${status}" title="${tile}">
          <span class="chip-char">${tile}</span>
        </div>
      `;
    }).join('');

    row.innerHTML = `
      <span class="history-attempt-num">#${attemptNum}</span>
      <div class="history-tiles-box">
        ${tilesHtml}
      </div>
    `;

    el.historyList.appendChild(row);
  });
}

function renderStageSelectGrid() {
  el.stagesGrid.innerHTML = '';

  STAGES_100.forEach((stage, index) => {
    if (currentStageFilter !== 'all' && stage.level !== currentStageFilter) {
      return;
    }

    const isCleared = gameState.clearedStages.includes(index);
    const isCurrent = gameState.stageIndex === index;

    const card = document.createElement('div');
    card.className = `stage-card ${isCleared ? 'cleared' : ''} ${isCurrent ? 'current' : ''}`;
    card.innerHTML = `
      <span class="num">${index + 1}</span>
      <span class="level-tag">${stage.level}</span>
    `;

    card.addEventListener('click', () => {
      triggerHaptic(15);
      sound.playTileClick();
      gameState.loadStage(index, true);
      el.stageSelectModal.classList.add('hidden');
    });

    el.stagesGrid.appendChild(card);
  });
}

function showRoundClearModal(state) {
  el.roundClearTitle.textContent = '정답입니다!';
  el.roundClearWord.textContent = `정답: ${state.currentPuzzle.answer}`;

  const earned = state.lastEarnedPoints || (state.currentPuzzle.points || state.currentPuzzle.length);
  const details = state.lastPenaltyDetails;

  let breakdownText = '';
  if (details) {
    const parts = [`기본 +${details.basePoints}점`];
    if (details.hintPenalty) parts.push(`힌트 -1점`);
    if (details.attemptPenalty) parts.push(`5회 초과 -1점`);
    breakdownText = parts.join(' · ');
  } else {
    breakdownText = `기본 +${earned}점`;
  }

  el.awardedPoints.innerHTML = `+${earned}점 <span class="points-breakdown">(${breakdownText})</span>`;
  el.roundClearModal.classList.remove('hidden');
  autoSubmitScoreToLeaderboard();
}

function showGameOverModal(state) {
  el.finalScoreText.textContent = `${state.score}점`;
  el.gameOverModal.classList.remove('hidden');
  autoSubmitScoreToLeaderboard();
}

/* =========================================================================
   Leaderboard / Supabase Integration
   ========================================================================= */

function lbHeaders() {
  return {
    apikey: SUPABASE_ANON_KEY,
    Authorization: `Bearer ${SUPABASE_ANON_KEY}`
  };
}

async function fetchLeaderboard() {
  const res = await fetch(
    `${SUPABASE_URL}/rest/v1/card_chess_leaderboard?nickname=like.wordgame:*&select=id,nickname,rating,wins,updated_at&order=rating.desc,updated_at.asc&limit=20`,
    { headers: lbHeaders() }
  );
  if (!res.ok) throw new Error('리더보드를 불러오지 못했습니다');
  const rows = await res.json();
  return rows.map(r => ({
    id: r.id,
    nickname: (r.nickname || '').replace(/^wordgame:/, ''),
    score: r.rating || 0,
    cleared_stages: r.wins || 0,
    created_at: r.updated_at
  }));
}

async function submitScoreToSupabase(nickname, score, clearedStagesCount) {
  const dbNick = 'wordgame:' + nickname;
  const fetchRes = await fetch(
    `${SUPABASE_URL}/rest/v1/card_chess_leaderboard?nickname=eq.${encodeURIComponent(dbNick)}`,
    { headers: lbHeaders() }
  );
  const existing = await fetchRes.json();

  if (Array.isArray(existing) && existing.length > 0) {
    const cur = existing[0];
    if (score > cur.rating || clearedStagesCount > cur.wins) {
      const patchRes = await fetch(
        `${SUPABASE_URL}/rest/v1/card_chess_leaderboard?id=eq.${cur.id}`,
        {
          method: 'PATCH',
          headers: Object.assign({}, lbHeaders(), { 'Content-Type': 'application/json' }),
          body: JSON.stringify({
            rating: Math.max(score, cur.rating),
            wins: Math.max(clearedStagesCount, cur.wins),
            updated_at: new Date().toISOString()
          })
        }
      );
      if (!patchRes.ok) throw new Error('점수 갱신 실패');
    }
  } else {
    const postRes = await fetch(
      `${SUPABASE_URL}/rest/v1/card_chess_leaderboard`,
      {
        method: 'POST',
        headers: Object.assign({}, lbHeaders(), {
          'Content-Type': 'application/json',
          Prefer: 'return=minimal'
        }),
        body: JSON.stringify({
          nickname: dbNick,
          rating: score,
          wins: clearedStagesCount
        })
      }
    );
    if (!postRes.ok) throw new Error('점수 등록 실패');
  }
}

async function autoSubmitScoreToLeaderboard() {
  const nickname = localStorage.getItem(NICKNAME_KEY) || getOrCreateStoredNickname(NICKNAME_KEY);
  if (!nickname || gameState.score <= 0) return;

  try {
    await submitScoreToSupabase(nickname, gameState.score, gameState.clearedStages.length);
  } catch (e) {
    console.warn('Auto score submission failed silently:', e);
  }
}

async function openLeaderboardModal() {
  el.leaderboardModal.classList.remove('hidden');
  if (el.leaderboardStatusMsg) el.leaderboardStatusMsg.textContent = '⚡ 최신 점수 자동 등록 중...';
  await autoSubmitScoreToLeaderboard();
  if (el.leaderboardStatusMsg) el.leaderboardStatusMsg.textContent = '';
  await loadLeaderboardUI();
}

async function loadLeaderboardUI() {
  if (!el.leaderboardList) return;
  el.leaderboardList.innerHTML = '<div class="empty-history-placeholder">불러오는 중...</div>';

  try {
    const rows = await fetchLeaderboard();
    if (!rows || rows.length === 0) {
      el.leaderboardList.innerHTML = '<div class="empty-history-placeholder">아직 등록된 랭킹 기록이 없습니다.</div>';
      return;
    }

    const currentNick = localStorage.getItem(NICKNAME_KEY) || '';

    el.leaderboardList.innerHTML = rows.map((r, idx) => {
      const rank = idx + 1;
      const rankClass = rank <= 3 ? `top-${rank}` : '';
      const isMe = r.nickname === currentNick;

      return `
        <div class="lb-row-item ${isMe ? 'my-row' : ''}">
          <span class="lb-rank ${rankClass}">${rank}</span>
          <span class="lb-nick">${r.nickname}</span>
          <span class="lb-score">${r.score || 0}점</span>
          <span class="lb-stages">${r.cleared_stages || 0}단계</span>
        </div>
      `;
    }).join('');
  } catch (e) {
    el.leaderboardList.innerHTML = '<div class="empty-history-placeholder">랭킹 정보를 가져오지 못했습니다.</div>';
  }
}

async function handleScoreSubmit() {
  const nickname = el.leaderboardNicknameInput.value.trim() || getOrCreateStoredNickname(NICKNAME_KEY);
  localStorage.setItem(NICKNAME_KEY, nickname);

  if (gameState.score <= 0) {
    if (el.leaderboardStatusMsg) el.leaderboardStatusMsg.textContent = '1단계 이상 클리어해야 점수를 등록할 수 있습니다.';
    return;
  }

  el.btnSubmitScore.disabled = true;
  if (el.leaderboardStatusMsg) el.leaderboardStatusMsg.textContent = '등록 중...';

  try {
    await submitScoreToSupabase(nickname, gameState.score, gameState.clearedStages.length);
    if (el.leaderboardStatusMsg) el.leaderboardStatusMsg.textContent = '✅ 점수가 성공적으로 등록되었습니다!';
    await loadLeaderboardUI();
  } catch (e) {
    if (el.leaderboardStatusMsg) el.leaderboardStatusMsg.textContent = '점수 등록에 실패했습니다. 다시 시도해 주세요.';
  } finally {
    el.btnSubmitScore.disabled = false;
  }
}

// Start App
init();
