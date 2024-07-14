package com.kaitokitaya.easytransfer.component

import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import com.kaitokitaya.easytransfer.originalType.VoidCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewPage(url: String, title: String, onTapBackArrow: VoidCallback) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = title) }, navigationIcon = {
                IconButton(onClick = onTapBackArrow) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Arrow back")
                }
            })
        }
    ) { innerPadding ->
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