package com.kaitokitaya.easytransfer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

open class CustomActivity : ComponentActivity() {

//    private val _isAvailableStorageAccessFlow = MutableStateFlow(false)
//    val isAvailableStorageAccess: MutableStateFlow<Boolean> get() = _isAvailableStorageAccessFlow

    companion object {
        private val TAG = CustomActivity::class.java.simpleName
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Timber.tag(TAG).d("Granted permissions => ${it.key} = ${it.value}")
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11 (R) or above
                if (Environment.isExternalStorageManager()) {
                    //Manage External Storage Permissions Granted
                    Timber.tag(TAG).d("onActivityResult: Manage External Storage Permissions Granted")
                } else {
                    Toast.makeText(this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    fun checkManageExternalPermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    open fun requestManageExternalStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:${applicationContext.packageName}")
        storageActivityResultLauncher.launch(intent)
    }

    fun checkStorageAccessPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissionRequestLaterModel()
        } else {
            checkStorageAccessPermissionLegacyModel()
        }
    }

    fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsLaterModel()
        } else {
            requestStoragePermissionsLegacyModel()
        }
    }

    private fun checkStorageAccessPermissionLegacyModel(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val writePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return readPermission or writePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissionsLegacyModel() {
        val requests = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        requestPermissionLauncher.launch(requests)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionRequestLaterModel(): Boolean {
        val imagePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_MEDIA_IMAGES
        )

        val audioPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_MEDIA_AUDIO
        )

        val videoPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_MEDIA_VIDEO
        )
        return imagePermission or audioPermission or videoPermission != PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionsLaterModel() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        )
    }

}