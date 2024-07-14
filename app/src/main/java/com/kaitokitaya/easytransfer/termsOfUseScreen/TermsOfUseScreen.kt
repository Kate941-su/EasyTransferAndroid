package com.kaitokitaya.easytransfer.termsOfUseScreen

import androidx.compose.runtime.Composable
import com.kaitokitaya.easytransfer.component.WebViewPage
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import com.kaitokitaya.easytransfer.router.AppRouter

@Composable
fun TermsOfUseScreen(url: String, onTapBackArrow: VoidCallback) {
    WebViewPage(url = url, title = AppRouter.TermsOfUseRouter.name, onTapBackArrow = onTapBackArrow)
}