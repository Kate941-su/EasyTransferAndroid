package com.kaitokitaya.easytransfer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.kaitokitaya.easytransfer.screen.howToUseScreen.HowToUseScreen
import com.kaitokitaya.easytransfer.httpServer.ConnectiveManagerWrapper
import com.kaitokitaya.easytransfer.httpServer.HttpClient
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import com.kaitokitaya.easytransfer.screen.informationScreen.InformationScreen
import com.kaitokitaya.easytransfer.screen.mainScreen.MainScreen
import com.kaitokitaya.easytransfer.screen.mainScreen.MainScreenViewModel
import com.kaitokitaya.easytransfer.screen.privacyPolicyScreen.PrivacyPolicyScreen
import com.kaitokitaya.easytransfer.router.AppRouter
import com.kaitokitaya.easytransfer.screen.splashScreen.SplashScreen
import com.kaitokitaya.easytransfer.screen.splashScreen.SplashScreenViewModel
import com.kaitokitaya.easytransfer.screen.termsOfUseScreen.TermsOfUseScreen
import com.kaitokitaya.easytransfer.ui.theme.EasyTransferTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : CustomActivity() {
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
//        connectiveManagerWrapper = connectiveManagerWrapper,
//        httpServer = httpServer,
        startStorageAccessPermissionRequest = {}
    )
    private val splashScreenViewModel = SplashScreenViewModel(
        checkStorageAccessPermission = {
            checkAndRequestPermission()
        },
        checkManageExternalStorage = {
            checkManageExternalPermission()
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backgroundScope = CoroutineScope(Dispatchers.IO)

        // Admob
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
        }

        // Background executing
        Intent(this, ForegroundService::class.java).also {
            ContextCompat.startForegroundService(this, it)
        }

        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        val connectiveManagerWrapper = ConnectiveManagerWrapper(context = this)
        // When calling `registerCallback()` in initializer it will crash on `onCreate()` phase.
        connectiveManagerWrapper.registerCallback()
        val httpServer = HttpServer(connectiveManagerWrapper = connectiveManagerWrapper)
        mainScreenViewModel.initialize(httpServer, connectiveManagerWrapper)
        setContent {
            val navController = rememberNavController()
            EasyTransferTheme {
                NavHost(navController = navController, startDestination = AppRouter.Splash.path) {
                    composable(AppRouter.Splash.path) {
                        SplashScreen(
                            viewModel = splashScreenViewModel,
                        )
                    }
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


    // Show dialog on the screen
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.all { it.value }
    }

    // Move to settings screen in Android
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Toast.makeText(this, "Storage Permissions $result", Toast.LENGTH_SHORT).show()
        }

    @RequiresApi(Build.VERSION_CODES.R)
    fun checkManageExternalPermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    open fun requestManageExternalStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:${applicationContext.packageName}")
        activityResultLauncher.launch(intent)
    }

    // Use before Build code R
    private fun checkAndRequestPermission() {
        val unGrantedPermissions = laterModelPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        when {
            unGrantedPermissions.isEmpty() -> {

            }
            // shouldShowRequestPermissionRationale shows when you denied to grant the permission
            unGrantedPermissions.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) } -> {

            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionArray(laterModelPermissions)
                } else {
                    requestPermissionArray(earlierModelPermissions)
                }
            }
        }

    }

    private fun requestPermissionArray(permissionList: Array<String>) {
        requestPermissionLauncher.launch(permissionList)
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }


}

