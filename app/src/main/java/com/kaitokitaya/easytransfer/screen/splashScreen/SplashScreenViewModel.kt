package com.kaitokitaya.easytransfer.screen.splashScreen

import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SplashScreenViewModel(
    private val checkPermissionCallback: () -> Boolean,
    private val requestPermissionCallback: VoidCallback,
    private val checkManageExternalStorageCallback: () -> Boolean,
    private val requestManageExternalStorageCallback: VoidCallback,
    private val onFinishedLaunching: VoidCallback
) : ViewModel() {

    private val _isShowDialogFlow = MutableStateFlow(false)
    val isShowDialog: StateFlow<Boolean> = _isShowDialogFlow.asStateFlow()
    private var isAvailableAccess = false
    private var isAvailableManage = false
    init {
        // Check permissions if not granted ask to be able to get storage access.
        isAvailableAccess = checkPermissionCallback()
        isAvailableManage = checkManageExternalStorageCallback()
        if (!isAvailableAccess) {
            requestPermissionCallback()
        }
        isAvailableAccess = checkPermissionCallback()
        if (isAvailableAccess) {
            _isShowDialogFlow.update { true }
        }


        if (!isAvailableManage) {
            _isShowDialogFlow.update { true }
        }
    }

    fun onTapConfirm() {
        requestManageExternalStorageCallback()
    }

}