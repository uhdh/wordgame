package com.gamehub.wordgame.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamehub.wordgame.net.LeaderboardApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RankingUiState(
    val loading: Boolean = false,
    val rows: List<LeaderboardApi.RankRow> = emptyList(),
    val nickname: String = "",
    val statusMessage: String? = null,
    val error: String? = null
)

class RankingViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsStore = SettingsStore(application)
    private val _state = MutableStateFlow(RankingUiState())
    val state: StateFlow<RankingUiState> = _state.asStateFlow()

    private suspend fun ensureNickname(): String {
        val current = settingsStore.settingsFlow.first().nickname
        if (current.isNotBlank()) return current
        val generated = randomNickname()
        settingsStore.setNickname(generated)
        return generated
    }

    fun setNickname(name: String) {
        viewModelScope.launch {
            settingsStore.setNickname(name)
            _state.update { it.copy(nickname = name) }
        }
    }

    /** Mirrors autoSubmitScoreToLeaderboard + loadLeaderboardUI from app.js. */
    fun refresh(score: Int, clearedStagesCount: Int) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val nickname = ensureNickname()
            try {
                if (score > 0) {
                    LeaderboardApi.submitScore(nickname, score, clearedStagesCount)
                }
                val rows = LeaderboardApi.fetchTop()
                _state.update { it.copy(loading = false, rows = rows, nickname = nickname) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, nickname = nickname, error = "랭킹 정보를 가져오지 못했습니다") }
            }
        }
    }

    /** 점수 등록은 [refresh]가 항상 자동으로 처리한다. 여기서는 닉네임만 바꾸고, 바뀐 이름으로 즉시 재등록한다. */
    fun changeNickname(name: String, score: Int, clearedStagesCount: Int) {
        val trimmed = name.trim()
        if (trimmed.isBlank() || trimmed == _state.value.nickname) return
        viewModelScope.launch {
            settingsStore.setNickname(trimmed)
            _state.update { it.copy(nickname = trimmed, statusMessage = "닉네임이 변경되었습니다") }
            if (score > 0) {
                try {
                    LeaderboardApi.submitScore(trimmed, score, clearedStagesCount)
                    val rows = LeaderboardApi.fetchTop()
                    _state.update { it.copy(rows = rows) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = "랭킹 정보를 가져오지 못했습니다") }
                }
            }
        }
    }
}
