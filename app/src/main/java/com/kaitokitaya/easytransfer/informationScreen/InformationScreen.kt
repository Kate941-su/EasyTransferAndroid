package com.kaitokitaya.easytransfer.informationScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaitokitaya.easytransfer.R
import com.kaitokitaya.easytransfer.component.InformationCard
import com.kaitokitaya.easytransfer.component.WebViewPage
import com.kaitokitaya.easytransfer.extensions.context.sendEmail
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import com.kaitokitaya.easytransfer.router.AppRouter
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Local


@Composable
fun InformationScreen(
    versionName: String,
    onTapBackToMain: VoidCallback,
    onTapTermOfUse: VoidCallback,
    onTapPrivacyPolicy: VoidCallback
) {
    InformationPage(
        versionName = versionName,
        onTapBackToMain = onTapBackToMain,
        onTapPrivacyPolicy = onTapPrivacyPolicy,
        onTapTermOfUse = onTapTermOfUse,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationPage(
    versionName: String,
    onTapBackToMain: VoidCallback,
    onTapTermOfUse: VoidCallback,
    onTapPrivacyPolicy: VoidCallback,
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = AppRouter.InformationRouter.name) }, navigationIcon = {
            IconButton(onClick = onTapBackToMain) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ArrowBack")
            }
        })
    }) { innerPadding ->
        val context = LocalContext.current
        val email = "kaito.kitaya.personal@gmail.com"
        Column(modifier = Modifier.padding(innerPadding)) {
            InformationCard(
                icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "version") },
                title = "Version",
                subTitle = versionName
            )
            InformationCard(
                icon = { Icon(imageVector = Icons.Default.Email, contentDescription = "contact") },
                title = "Contact",
                subTitle = email,
                onTapCard = { context.sendEmail(to = email, subject = "inquiry: ") }
            )
            InformationCard(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_checklist_rtl_24),
                        contentDescription = "terms of use",
                    )
                },
                title = "Terms of Use",
                onTapCard = onTapTermOfUse
            )
            InformationCard(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_privacy_tip_24),
                        contentDescription = "privacy policy",
                    )
                },
                title = "Privacy policy",
                onTapCard = onTapPrivacyPolicy
            )
        }
    }
}



@Preview(showSystemUi = true)
@Composable
fun InformationScreenPreview() {
    InformationPage(
        versionName = "v1.0.0",
        onTapBackToMain = { },
        onTapTermOfUse = { },
        onTapPrivacyPolicy = {})
}