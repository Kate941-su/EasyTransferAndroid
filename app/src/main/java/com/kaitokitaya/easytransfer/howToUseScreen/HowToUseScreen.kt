package com.kaitokitaya.easytransfer.howToUseScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kaitokitaya.easytransfer.component.WebViewPage
import com.kaitokitaya.easytransfer.originalType.VoidCallback

@Composable
fun HowToUseScreen(url: String, onTapBackToMain: VoidCallback) {
    WebViewPage(url = url, onTapBackArrow = onTapBackToMain)
}

@Preview(showSystemUi = true)
@Composable
fun HowToUseScreenPreview() {
    WebViewPage(url = "", onTapBackArrow = {})
}