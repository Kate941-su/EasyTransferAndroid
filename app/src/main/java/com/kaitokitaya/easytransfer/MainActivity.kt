package com.kaitokitaya.easytransfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kaitokitaya.easytransfer.mainScreen.MainScreen
import com.kaitokitaya.easytransfer.mainScreen.MainScreenViewModel
import com.kaitokitaya.easytransfer.ui.theme.EasyTransferTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mainScreenViewModel = MainScreenViewModel()
        setContent {
            EasyTransferTheme {
                MainScreen(
                    viewModel = mainScreenViewModel
                )
            }
        }
    }
}

