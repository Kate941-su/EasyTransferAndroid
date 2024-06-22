package com.kaitokitaya.easytransfer.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaitokitaya.easytransfer.R
import com.kaitokitaya.easytransfer.originalType.VoidCallback

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    MainPage(
        onTapStart = { viewModel.onTapStart() }
    )
}

@Composable
fun MainPage(
    onTapStart: VoidCallback
) {
    Scaffold { innerPadding ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Button(onClick = onTapStart, colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)) {
                Text(text = "start")
            }
            IconButton(onClick = {  }) {
                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.baseline_wifi_24), contentDescription = "wifi")
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MainPage(
        onTapStart = {}
    )
}