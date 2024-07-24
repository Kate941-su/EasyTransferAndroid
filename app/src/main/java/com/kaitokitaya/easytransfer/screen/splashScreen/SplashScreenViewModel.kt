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
    private val checkStorageAccessPermission: VoidCallback,
    private val checkManageExternalStorage: VoidCallback,
) : ViewModel() {

    private val _isShowDialogFlow = MutableStateFlow(false)
    val isShowDialog: StateFlow<Boolean> = _isShowDialogFlow.asStateFlow()


    fun checkIsGranted() {

    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val result = checkManageExternalStorage()
        } else {
            checkStorageAccessPermission()
        }
        // Check storage access permissions
        viewModelScope.launch {

        }
    }
}