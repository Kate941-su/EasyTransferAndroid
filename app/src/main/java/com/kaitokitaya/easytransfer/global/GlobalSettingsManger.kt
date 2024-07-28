package com.kaitokitaya.easytransfer.global

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GlobalSettingsManger {
    private val _globalSettingsFlow = MutableStateFlow(GlobalSettings(appServiceState = AppServiceState.Initializing))
    val globalSettings: StateFlow<GlobalSettings> = _globalSettingsFlow.asStateFlow()

    fun changeAppServiceState(state: AppServiceState) {
        _globalSettingsFlow.update {
            it.copy(appServiceState = state)
        }
    }
}