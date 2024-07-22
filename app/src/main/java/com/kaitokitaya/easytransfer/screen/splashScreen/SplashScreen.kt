package com.kaitokitaya.easytransfer.screen.splashScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaitokitaya.easytransfer.R
import com.kaitokitaya.easytransfer.originalType.VoidCallback

@Composable
fun SplashScreen(viewModel: SplashScreenViewModel) {
    SplashPage()
}

@Composable
fun SplashPage() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier
        .fillMaxSize()
        .background(color = Color.White)
    ) {
        Image(painterResource(id = R.drawable.ic_main), contentDescription = "splash")
    }
}


@Preview(showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashPage()
}