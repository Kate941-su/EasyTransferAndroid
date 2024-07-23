package com.kaitokitaya.easytransfer.global

data class GlobalSettings(val appServiceState: AppServiceState) {

}

sealed class AppServiceState {
    data object NoGranted : AppServiceState()
    data object OnlyMedia : AppServiceState()
    data object AllFiles : AppServiceState()
}