package com.kaitokitaya.easytransfer.mainScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaitokitaya.easytransfer.R
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import com.kaitokitaya.easytransfer.originalType.VoidCallback

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    val ipAddress = viewModel.ipAddress.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.startStorageAccessPermissionRequest()
    }
    MainPage(
        ipAddress = ipAddress.value,
        onTapStart = { viewModel.onTapStart() },
        onTapStop = { viewModel.onTapStop() }
    )
}

@Composable
fun MainPage(
    ipAddress: String?,
    onTapStart: VoidCallback,
    onTapStop: VoidCallback,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                Button(onClick = onTapStart, colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)) {
                    Text(text = "start")
                }
                Button(onClick = onTapStop, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text(text = "stop")
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_wifi_24),
                        contentDescription = "wifi"
                    )
                }
            }
            if (ipAddress != null) {
                Text(text = "Your IP Adress is: $ipAddress:${HttpServer.PORT}")
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MainPage(
        ipAddress = "192.168.1.22:",
        onTapStart = {},
        onTapStop = {},
    )
}