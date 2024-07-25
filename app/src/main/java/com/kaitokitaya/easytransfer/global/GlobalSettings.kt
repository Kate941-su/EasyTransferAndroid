package com.kaitokitaya.easytransfer.global

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object GlobalSettingsProvider {
    private val _globalSettingsFlow = MutableStateFlow(GlobalSettings.initialize())
    val globalSettingsState: StateFlow<GlobalSettings> = _globalSettingsFlow.asStateFlow()

    fun setAppServiceState(appServiceState: AppServiceState) {
        _globalSettingsFlow.update {
            _globalSettingsFlow.value.copy(appServiceState = appServiceState)
        }
    }
}

data class GlobalSettings(val appServiceState: AppServiceState) {
    companion object {
        fun initialize(): GlobalSettings {
            return GlobalSettings(
                appServiceState = AppServiceState.Initializing
            )
        }
    }
}

sealed class AppServiceState {
    data object Initializing : AppServiceState()
    data object Unavailable : AppServiceState()
    data object OnlyMedia : AppServiceState()
    data object FullAccess : AppServiceState()
}