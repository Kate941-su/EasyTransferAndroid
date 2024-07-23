package com.kaitokitaya.easytransfer.screen.splashScreen

import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashScreenViewModel(
    private val checkPermissionCallback: () -> Boolean,
    private val requestPermissionCallback: VoidCallback,
    private val checkManageExternalStorageCallback: () -> Boolean,
    private val requestManageExternalStorageCallback: VoidCallback,
    private val onTapGoSettingsScreen: VoidCallback,
    private val onFinishedLaunching: VoidCallback
) : ViewModel() {

    private val _isShowDialogFlow = MutableStateFlow(false)
    val isShowDialog: StateFlow<Boolean> = _isShowDialogFlow.asStateFlow()

    init {
        // Check storage access permissions
        if (checkPermissionCallback()) {
            requestPermissionCallback()
        }
        if (checkManageExternalStorageCallback()) {
            requestManageExternalStorageCallback()
        }

        viewModelScope.launch {

        }

        onFinishedLaunching()
    }
}