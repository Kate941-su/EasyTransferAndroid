package com.kaitokitaya.easytransfer.screen.howToUseScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kaitokitaya.easytransfer.component.WebViewPage
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import com.kaitokitaya.easytransfer.router.AppRouter

@Composable
fun HowToUseScreen(url: String, onTapBackToMain: VoidCallback) {
    WebViewPage(url = url, title = AppRouter.HowToUseRouter.name, onTapBackArrow = onTapBackToMain)
}

@Preview(showSystemUi = true)
@Composable
fun HowToUseScreenPreview() {
    WebViewPage(url = "", title = AppRouter.HowToUseRouter.name, onTapBackArrow = {})
}