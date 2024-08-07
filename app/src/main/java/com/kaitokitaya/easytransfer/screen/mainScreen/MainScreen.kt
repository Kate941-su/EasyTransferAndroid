package com.kaitokitaya.easytransfer.screen.mainScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DoubleState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.kaitokitaya.easytransfer.R
import com.kaitokitaya.easytransfer.adsComponents.AdmobBanner
import com.kaitokitaya.easytransfer.extensions.context.goToLink
import com.kaitokitaya.easytransfer.global.AppServiceState
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import com.kaitokitaya.easytransfer.screen.mainScreen.model.ServerStatus
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.css.label
import timber.log.Timber

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onTapHowToUse: VoidCallback,
    onTapInformation: VoidCallback,
) {
    val ipAddress = viewModel.ipAddress.collectAsState()
    val serverStatus = viewModel.serverStatus.collectAsState()
    val isNeedRefresh = viewModel.isNeedRefresh.collectAsState()
    val appServiceState = viewModel.appServiceState.collectAsState()

    val transition = updateTransition(targetState = isNeedRefresh.value, label = "")
    val animatedColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 1000) }, label = ""
    ) {
        if (it) Color(0xFFFF7043)
        else Color(0x00FF7043)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(appServiceState.value) {
        val dir = viewModel.getDirectoryItem()
        Timber.tag("MainScreen").d(dir?.toString())
    }

    MainPage(
        ipAddress = ipAddress.value,
        serverStatus = serverStatus.value,
        animatedColor = animatedColor,
        drawerState = drawerState,
        scope = scope,
        isNeedRefresh = isNeedRefresh.value,
        appServiceState = appServiceState.value,
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
        onTapHowToUse = onTapHowToUse,
        onTapInformation = onTapInformation,
        onTapGoSettings = { viewModel.onTapGoSettings() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    ipAddress: String?,
    serverStatus: ServerStatus,
    animatedColor: Color,
    drawerState: DrawerState,
    scope: CoroutineScope,
    isNeedRefresh: Boolean,
    appServiceState: AppServiceState,
    onTapRefresh: VoidCallback,
    onTapPowerButton: VoidCallback,
    onTapHowToUse: VoidCallback,
    onTapInformation: VoidCallback,
    onTapGoSettings: VoidCallback,
) {
    val modalWindowWidth = LocalConfiguration.current.screenWidthDp * 2 / 3
    val context = LocalContext.current
    ModalNavigationDrawer(drawerContent = {
        ModalDrawerSheet(
            modifier = if (drawerState.isOpen) Modifier.width(modalWindowWidth.dp) else Modifier.width(0.dp)
        ) {
            Text(
                text = "Menu",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                modifier = Modifier.padding(12.dp)
            )
            NavigationDrawerItem(label = { Text(text = "Main") }, selected = false, onClick = {
                scope.launch {
                    drawerState.close()
                }
            })
            NavigationDrawerItem(label = { Text(text = "How To Use") }, selected = false, onClick = {
                context.goToLink("https://kaito-kitaya.gitbook.io/how-to-use-easytransfer/2.-how-to-use-this-app")
            })
            NavigationDrawerItem(label = { Text(text = "Information") }, selected = false, onClick = onTapInformation)
        }
    }, drawerState = drawerState) {
        Scaffold(topBar = {
            when (appServiceState) {
                AppServiceState.Initializing -> {}
                else -> {
                    TopAppBar(title = {}, navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    })
                }
            }
        }) { innerPadding ->
            when (appServiceState) {
                AppServiceState.FullAccess, AppServiceState.OnlyMedia -> Column(
                    modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom
                ) {
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
                                    ServerStatus.Unavailable -> Color.Gray
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
                                    text = serverStatus.stateName, style = TextStyle(
                                        color = Color.Red, fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "IP address 👉")
                                if (ipAddress != null && serverStatus == ServerStatus.Working) {
                                    Text(
                                        text = "$ipAddress:${HttpServer.PORT}", style = TextStyle(
                                            color = Color.Red, fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            ElevatedButton(
                                onClick = { if (isNeedRefresh) onTapRefresh() },
                                colors = ButtonDefaults.buttonColors(animatedColor),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Refresh",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = if (isNeedRefresh) Modifier.alpha(1.0F) else Modifier.alpha(.2F)
                                )
                            }
                            if (serverStatus == ServerStatus.Unavailable) {
                                Box {
                                    Text(
                                        text = "Unable to provide service. \nCheck if you connected the device to Wi-Fi.",
                                        color = Color.Red,
                                        style = TextStyle(textAlign = TextAlign.Center),
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .align(alignment = Alignment.BottomCenter)
                        ) {
//                        AdmobBanner()
                        }
                    }
                }

                AppServiceState.Initializing -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_main), contentDescription = "Splash Screen")
                }

                AppServiceState.Unavailable -> Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = "You need to grant permissions to use this app.")
                    TextButton(onClick = onTapGoSettings) {
                        Text(text = "Tap to grant permissions")
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    val scope: CoroutineScope = rememberCoroutineScope()
    MainPage(
        ipAddress = "192.168.1.22:",
        serverStatus = ServerStatus.Standby,
        animatedColor = Color(0xFFFF7043),
//        drawerState = DrawerState(initialValue = DrawerValue.Open),
        drawerState = DrawerState(initialValue = DrawerValue.Closed),
        scope = scope,
        isNeedRefresh = false,
        appServiceState = AppServiceState.Unavailable,
        onTapRefresh = {},
        onTapPowerButton = {},
        onTapHowToUse = {},
        onTapInformation = {},
        onTapGoSettings = {},
    )
}