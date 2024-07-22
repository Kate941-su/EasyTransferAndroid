package com.kaitokitaya.easytransfer.screen.splashScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaitokitaya.easytransfer.R
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun SplashScreen(viewModel: SplashScreenViewModel) {
    val isShowDialog = viewModel.isShowDialog.collectAsState()
    var isShowIndicatePage by rememberSaveable {
        mutableStateOf<Boolean>(false)
    }
    SplashPage(
        isShowDialog = isShowDialog.value,
        isShowIndicatePage = isShowIndicatePage,
        onDismissAlert = { isShowIndicatePage = it },
        onConfirmAlert = { viewModel.onTapConfirm() },
    )
}

@Composable
fun SplashPage(
    isShowDialog: Boolean,
    isShowIndicatePage: Boolean,
    onDismissAlert: (Boolean) -> Unit,
    onConfirmAlert: VoidCallback,
) {
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        AlertDialog(onDismissRequest = {
           onDismissAlert(true)
        }, confirmButton = { onConfirmAlert() })
        if (isShowIndicatePage) {
            IndicateSettingsPage()
        } else {
            Image(painterResource(id = R.drawable.ic_main), contentDescription = "splash")
        }
    }
}

@Composable
fun IndicateSettingsPage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("You have to grant permissions to use this app please give the permissions to this app👇")
        TextButton(onClick = { }) {
            Text(text = "Go settings page")
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashPage(
        isShowDialog = true,
        isShowIndicatePage = true,
        onConfirmAlert = {},
        onDismissAlert = {},
    )
}