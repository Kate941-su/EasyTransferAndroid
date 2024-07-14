package com.kaitokitaya.easytransfer.mainScreen

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaitokitaya.easytransfer.R
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import com.kaitokitaya.easytransfer.mainScreen.model.ServerStatus
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import timber.log.Timber

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    val ipAddress = viewModel.ipAddress.collectAsState()
    val serverStatus = viewModel.serverStatus.collectAsState()
    val isNeedRefresh = viewModel.isNeedRefresh.collectAsState()

    val transition = updateTransition(targetState = isNeedRefresh.value, label = "")
    val animatedColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 1000) },
        label = ""
    ) {
        if (it) Color(0xFFFF7043)
        else Color(0x00FF7043)
    }

    LaunchedEffect(Unit) {
        viewModel.startStorageAccessPermissionRequest()
        val dir = viewModel.getDirectoryItem()
        Timber.tag("MainScreen").d(dir?.toString())
    }

    MainPage(
        ipAddress = ipAddress.value,
        serverStatus = serverStatus.value,
        animatedColor = animatedColor,
        onTapRefresh = {
            viewModel.onRefresh()
        },
        onTapPowerButton = {
            if (serverStatus.value == ServerStatus.Working) {
                viewModel.onLoading()
                viewModel.onStop()
            } else if (serverStatus.value == ServerStatus.Standby) {
                viewModel.onLoading()
                viewModel.onStart()
            }
        },
    )
}

@Composable
fun MainPage(
    ipAddress: String?,
    serverStatus: ServerStatus,
    animatedColor: Color,
    onTapRefresh: VoidCallback,
    onTapPowerButton: VoidCallback,
) {
    Scaffold { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onTapPowerButton,
                        modifier = Modifier.size(100.dp),
                        containerColor = when (serverStatus) {
                            ServerStatus.Standby -> Color.Green
                            ServerStatus.Launching, ServerStatus.Shutdown, ServerStatus.Refresh -> Color.Yellow
                            ServerStatus.Working -> Color.Red
                        },
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_power_settings_new_24),
                            contentDescription = "",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Server Status 👉 ")
                        Text(
                            text = serverStatus.stateName,
                            style = TextStyle(
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "IP address 👉")
                        if (ipAddress != null) {
                            Text(
                                text = "$ipAddress:${HttpServer.PORT}",
                                style = TextStyle(
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    ElevatedButton(
                        onClick = onTapRefresh,
                        colors = ButtonDefaults.buttonColors(animatedColor),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(text = "Refresh")
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MainPage(
        ipAddress = "192.168.1.22:",
        serverStatus = ServerStatus.Standby,
        animatedColor = Color(0xFFFF7043),
        onTapRefresh = {},
        onTapPowerButton = {}
    )
}