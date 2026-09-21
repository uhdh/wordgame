package com.gamehub.wordgame.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SettingsStore(application)

    val settings: StateFlow<Settings> = store.settingsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())
    val onboarded: StateFlow<Boolean?> = store.onboardedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setOnboarded() = viewModelScope.launch { store.setOnboarded() }
    fun setSoundOn(on: Boolean) = viewModelScope.launch { store.setSoundOn(on) }
    fun setHapticOn(on: Boolean) = viewModelScope.launch { store.setHapticOn(on) }
    fun setDarkMode(on: Boolean) = viewModelScope.launch { store.setDarkMode(on) }
    fun setNickname(name: String) = viewModelScope.launch { store.setNickname(name) }
}
