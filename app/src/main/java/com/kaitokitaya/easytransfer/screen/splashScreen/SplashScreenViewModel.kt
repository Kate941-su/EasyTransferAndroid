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
    private val storageAccessPermissionCallback: VoidCallback,
    private val manageStoragePermissionCallback: VoidCallback,
    private val onFinishedLaunching: VoidCallback
) : ViewModel() {

    private val _isShowDialogFlow = MutableStateFlow(false)
    val isShowDialog: StateFlow<Boolean> = _isShowDialogFlow.asStateFlow()
    init {
        storageAccessPermissionCallback()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkStorageManagePermission()
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun checkStorageManagePermission() {
        if (Environment.isExternalStorageManager()) {
            _isShowDialogFlow.update {
                true
            }
        }
    }

    fun onTapConfirm() {
        manageStoragePermissionCallback()
    }

}