package com.kaitokitaya.easytransfer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.kaitokitaya.easytransfer.global.AppServiceState
import com.kaitokitaya.easytransfer.global.GlobalSettingsProvider
import com.kaitokitaya.easytransfer.screen.howToUseScreen.HowToUseScreen
import com.kaitokitaya.easytransfer.httpServer.ConnectiveManagerWrapper
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import com.kaitokitaya.easytransfer.screen.informationScreen.InformationScreen
import com.kaitokitaya.easytransfer.screen.mainScreen.MainScreen
import com.kaitokitaya.easytransfer.screen.mainScreen.MainScreenViewModel
import com.kaitokitaya.easytransfer.screen.privacyPolicyScreen.PrivacyPolicyScreen
import com.kaitokitaya.easytransfer.router.AppRouter
import com.kaitokitaya.easytransfer.screen.termsOfUseScreen.TermsOfUseScreen
import com.kaitokitaya.easytransfer.ui.theme.EasyTransferTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : ComponentActivity() {
    private val earlierModelPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    private val laterModelPermissions = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO,
    )

    private val mainScreenViewModel = MainScreenViewModel(
        checkPermissions = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkManageExternalStoragePermission()
            } else {
                checkAndRequestStorageAccessPermission()
            }
        },
        requestPermissions = { requestPermissions() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backgroundScope = CoroutineScope(Dispatchers.IO)

        // Admob
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
        }

        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        val connectiveManagerWrapper = ConnectiveManagerWrapper(context = this)
        // When calling `registerCallback()` in initializer it will crash on `onCreate()` phase.
        connectiveManagerWrapper.registerCallback()

        val httpServer = HttpServer(connectiveManagerWrapper = connectiveManagerWrapper)
        mainScreenViewModel.initialize(
            server = httpServer,
            connectiveManager = connectiveManagerWrapper,
        )
        setContent {
            val navController = rememberNavController()
            EasyTransferTheme {
                NavHost(navController = navController, startDestination = AppRouter.Main.path) {
                    composable(AppRouter.Main.path) {
                        MainScreen(
                            viewModel = mainScreenViewModel,
                            onTapHowToUse = { navController.navigate(AppRouter.HowToUseRouter.path) },
                            onTapInformation = { navController.navigate(AppRouter.InformationRouter.path) }
                        )
                    }
                    composable(AppRouter.HowToUseRouter.path) {
                        HowToUseScreen(
                            url = "https://kaito-kitaya.gitbook.io/how-to-use-easytransfer/2.-how-to-use-this-app",
                            onTapBackToMain = { navController.navigate(AppRouter.Main.path) }
                        )
                    }
                    composable(AppRouter.InformationRouter.path) {
                        val activity = this@MainActivity
                        val versionName = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
                        InformationScreen(
                            versionName = versionName,
                            onTapBackToMain = { navController.navigate(AppRouter.Main.path) },
                            onTapTermOfUse = { navController.navigate(AppRouter.TermsOfUseRouter.path) },
                            onTapPrivacyPolicy = { navController.navigate(AppRouter.PrivacyPolicyRouter.path) })
                    }
                    composable(AppRouter.TermsOfUseRouter.path) {
                        TermsOfUseScreen(
                            url = "https://kaito-kitaya.gitbook.io/how-to-use-easytransfer/4.-terms-of-use",
                            onTapBackArrow = { navController.navigate(AppRouter.InformationRouter.path) }
                        )
                    }
                    composable(AppRouter.PrivacyPolicyRouter.path) {
                        PrivacyPolicyScreen(
                            url = "https://kaito-kitaya.gitbook.io/how-to-use-easytransfer/3.-privacy-policy",
                            onTapBackArrow = { navController.navigate(AppRouter.InformationRouter.path) }
                        )
                    }
                }
            }
        }
    }

    private fun onGrantedPermission() {
        GlobalSettingsProvider.setAppServiceState(AppServiceState.FullAccess)
        // Background executing
        Intent(this, ForegroundService::class.java).also {
            ContextCompat.startForegroundService(this, it)
        }
    }


    // Show dialog on the screen
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            onGrantedPermission()
        } else {
            GlobalSettingsProvider.setAppServiceState(AppServiceState.Unavailable)
        }
    }

    // Move to settings screen in Android
    @RequiresApi(Build.VERSION_CODES.R)
    private val manageAccessStorageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                onGrantedPermission()
            } else {
                GlobalSettingsProvider.setAppServiceState(AppServiceState.Unavailable)
            }
        }

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val unGrantedPermissions = earlierModelPermissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (unGrantedPermissions.isEmpty()) {
                onGrantedPermission()
            } else {
                GlobalSettingsProvider.setAppServiceState(AppServiceState.Unavailable)
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    fun checkManageExternalStoragePermission() {
        if (!Environment.isExternalStorageManager()) {
            GlobalSettingsProvider.setAppServiceState(AppServiceState.Unavailable)
        } else {
            onGrantedPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestManageExternalStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:${applicationContext.packageName}")
        manageAccessStorageLauncher.launch(intent)
    }

    private fun openSettingsStorageAccessPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${applicationContext.packageName}")
        storagePermissionLauncher.launch(intent)
    }

    // Use before Build code R
    private fun checkAndRequestStorageAccessPermission() {
        val unGrantedPermissions = earlierModelPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        when {
            unGrantedPermissions.isEmpty() -> {
                onGrantedPermission()
            }
            // shouldShowRequestPermissionRationale shows when you denied to grant the permission
            unGrantedPermissions.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) } -> {
                GlobalSettingsProvider.setAppServiceState(AppServiceState.Unavailable)
            }

            else -> {
                requestPermissionArray()
            }
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestManageExternalStoragePermission()
        } else {
            openSettingsStorageAccessPermission()
        }
    }

    private fun requestPermissionArray() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionArray(laterModelPermissions)
        } else {
            requestPermissionArray(earlierModelPermissions)
        }
    }

    private fun requestPermissionArray(permissionList: Array<String>) {
        requestPermissionLauncher.launch(permissionList)
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }
}

