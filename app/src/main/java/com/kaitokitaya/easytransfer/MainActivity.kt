package com.kaitokitaya.easytransfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaitokitaya.easytransfer.mainScreen.MainScreen
import com.kaitokitaya.easytransfer.mainScreen.MainScreenViewModel
import com.kaitokitaya.easytransfer.router.AppRouter
import com.kaitokitaya.easytransfer.ui.theme.EasyTransferTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val mainScreenViewModel = MainScreenViewModel()
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

