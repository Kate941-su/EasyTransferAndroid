package com.kaitokitaya.easytransfer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        val httpClient = HttpClient(activity = this)
        val httpServer = HttpServer(connectiveManagerWrapper = connectiveManagerWrapper, httpClient = httpClient)
        setContent {
            val navController = rememberNavController()
            val mainScreenViewModel = MainScreenViewModel(
                connectiveManagerWrapper = connectiveManagerWrapper,
                httpServer = httpServer,
                startStorageAccessPermissionRequest = {}
            )
            val splashScreenViewModel = SplashScreenViewModel(
                storageAccessPermissionCallback = {
                    // Demand all files access
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        startStorageAccessPermissionRequest()
                    } else {
                        startStorageAccessPermissionRequestLaterModel()
                    }
                },
                manageStoragePermissionCallback = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        requestManageExternalStoragePermission()
                    }
                },
                onFinishedLaunching = { navController.navigate(AppRouter.Main.path) }
            )
            EasyTransferTheme {
                // TODO: In product version, I have to change from Main to Splash
                NavHost(navController = navController, startDestination = AppRouter.Main.path) {
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

    override fun onStart() {
        super.onStart()

    }
}

