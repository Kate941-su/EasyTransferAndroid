package com.kaitokitaya.easytransfer.howToUseScreen

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HowToUseScreen(url: String) {
    WebViewPage(url = url)
}

@Composable
fun WebViewPage(url: String) {
    Scaffold { innerPadding ->
        AndroidView(
            factory = {
                WebView(it)
            },
            update = {
                it.webViewClient = WebViewClient()
                it.loadUrl(url)
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}