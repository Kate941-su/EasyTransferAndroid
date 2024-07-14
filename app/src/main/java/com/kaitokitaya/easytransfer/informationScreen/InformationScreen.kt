package com.kaitokitaya.easytransfer.informationScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaitokitaya.easytransfer.component.WebViewPage
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import com.kaitokitaya.easytransfer.router.AppRouter

import java.security.Policy

@Composable
fun InformationScreen(
    onTapBackToMain: VoidCallback,
    onTapTermOfUse: VoidCallback,
    onTapPrivacyPolicy: VoidCallback
) {
    InformationPage(
        onTapBackToMain = onTapBackToMain,
        onTapPrivacyPolicy = onTapPrivacyPolicy,
        onTapTermOfUse = onTapTermOfUse,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationPage(
    onTapBackToMain: VoidCallback,
    onTapTermOfUse: VoidCallback,
    onTapPrivacyPolicy: VoidCallback,
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = AppRouter.Information.name) }, navigationIcon = {
            IconButton(onClick = onTapBackToMain) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ArrowBack")
            }
        })
    }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

        }
    }
}