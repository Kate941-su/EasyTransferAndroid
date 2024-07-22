package com.kaitokitaya.easytransfer.screen.splashScreen

import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.kaitokitaya.easytransfer.originalType.VoidCallback

class SplashScreenViewModel(
    private val storageAccessPermissionCallback: VoidCallback,
    private val manageStoragePermissionCallback: VoidCallback,
    private val onFinishedLaunching: VoidCallback
) : ViewModel() {
    init {
        storageAccessPermissionCallback()
        checkPermissions()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun checkPermissions() {
        if (Environment.isExternalStorageManager()) {
//        showDialog => intent to Setting screen
        //            manageStoragePermissionCallback()
        }
    }
}