package com.kaitokitaya.easytransfer.global

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GlobalSettings(val appServiceState: AppServiceState) {

}

sealed class AppServiceState {
    data object Waiting: AppServiceState()
    data object Unavailable : AppServiceState()
    data object OnlyMedia : AppServiceState()
    data object FullAccess : AppServiceState()
}