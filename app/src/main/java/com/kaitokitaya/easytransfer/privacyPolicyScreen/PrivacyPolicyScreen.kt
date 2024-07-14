package com.kaitokitaya.easytransfer.privacyPolicyScreen

import androidx.compose.runtime.Composable
import com.kaitokitaya.easytransfer.component.WebViewPage
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import com.kaitokitaya.easytransfer.router.AppRouter

@Composable
fun PrivacyPolicyScreen(url: String, onTapBackArrow: VoidCallback) {
    WebViewPage(url = url, title = AppRouter.PrivacyPolicyRouter.name, onTapBackArrow = onTapBackArrow)
}