package com.kaitokitaya.easytransfer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaitokitaya.easytransfer.httpServer.ConnectiveManagerWrapper
import com.kaitokitaya.easytransfer.mainScreen.MainScreen
import com.kaitokitaya.easytransfer.mainScreen.MainScreenViewModel
import com.kaitokitaya.easytransfer.router.AppRouter
import com.kaitokitaya.easytransfer.ui.theme.EasyTransferTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

        } else {

        }
    }

    // TODO: If permission is not granted, transfer setting screen to get granted.
    private fun startStorageAccessPermissionRequest() {
        val readExternalStoragePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            Timber.tag(TAG).d("${Manifest.permission.READ_EXTERNAL_STORAGE} is granted")
        }

        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            Timber.tag(TAG).d("${Manifest.permission.WRITE_EXTERNAL_STORAGE} is granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        setContent {
            val connectiveManagerWrapper = ConnectiveManagerWrapper(context = this)
            val navController = rememberNavController()
            val mainScreenViewModel = MainScreenViewModel(
                connectiveManagerWrapper = connectiveManagerWrapper,
                startStorageAccessPermissionRequest = { startStorageAccessPermissionRequest() }
            )
            EasyTransferTheme {
                // TODO: In product version, I have to change from Main to Splash
                NavHost(navController = navController, startDestination = AppRouter.Main.PATH) {
                    composable(AppRouter.Splash.PATH) {
                        MainScreen(
                            viewModel = mainScreenViewModel
                        )
                    }
                    composable(AppRouter.Main.PATH) {
                        MainScreen(
                            viewModel = mainScreenViewModel
                        )
                    }
                    composable(AppRouter.ServerDetail.PATH) {
                        MainScreen(
                            viewModel = mainScreenViewModel
                        )
                    }
                }
            }
        }
    }


}

