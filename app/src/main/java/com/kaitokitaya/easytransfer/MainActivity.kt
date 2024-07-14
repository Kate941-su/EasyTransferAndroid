package com.kaitokitaya.easytransfer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaitokitaya.easytransfer.howToUseScreen.HowToUseScreen
import com.kaitokitaya.easytransfer.httpServer.ConnectiveManagerWrapper
import com.kaitokitaya.easytransfer.httpServer.HttpClient
import com.kaitokitaya.easytransfer.httpServer.HttpServer
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
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // TODO: If permmision is no granted explain why it doesn't work.
    }

    // TODO: If permission is not granted, transfer setting screen to get granted.
    private fun startStorageAccessPermissionRequest() {
        val storagePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            )
        } else {
            Timber.tag(TAG).d("${Manifest.permission.READ_EXTERNAL_STORAGE} is granted")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startStorageAccessPermissionRequestLaterModel() {
        val storagePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                )
            )
        } else {
            Timber.tag(TAG).d("${Manifest.permission.READ_EXTERNAL_STORAGE} is granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        setContent {
            val connectiveManagerWrapper = ConnectiveManagerWrapper(context = this)
            val navController = rememberNavController()
            val httpClient = HttpClient(activity = this)
            val httpServer = HttpServer(connectiveManagerWrapper = connectiveManagerWrapper, httpClient = httpClient)
            val mainScreenViewModel = MainScreenViewModel(
                connectiveManagerWrapper = connectiveManagerWrapper,
                httpServer = httpServer,
                startStorageAccessPermissionRequest = {}
            )
            EasyTransferTheme {
                // TODO: In product version, I have to change from Main to Splash
                NavHost(navController = navController, startDestination = AppRouter.HowToUseRouter.path) {
                    composable(AppRouter.Splash.path) {
                        MainScreen(
                            viewModel = mainScreenViewModel
                        )
                    }
                    composable(AppRouter.Main.path) {
                        MainScreen(
                            viewModel = mainScreenViewModel
                        )
                    }
                    composable(AppRouter.HowToUseRouter.path) {
                        HowToUseScreen(
                            url = "https://kaito-kitaya.gitbook.io/worldinfo_terms_of_use"
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startStorageAccessPermissionRequest()
        } else {
            startStorageAccessPermissionRequestLaterModel()
        }
    }
}

