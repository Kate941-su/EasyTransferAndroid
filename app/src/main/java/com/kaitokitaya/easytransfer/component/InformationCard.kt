package com.kaitokitaya.easytransfer.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaitokitaya.easytransfer.originalType.VoidCallback

@Composable
fun InformationCard(
    icon: @Composable () -> Unit,
    title: String,
    subTitle: String? = null,
    onTapCard: VoidCallback? = null
) {
    Box(modifier = Modifier
        .padding(24.dp)
        .clickable {
            if (onTapCard != null) {
                onTapCard()
            }
        }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Box(modifier = Modifier.padding(end = 24.dp))
            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = if (onTapCard != null) Color.Blue else MaterialTheme.colorScheme.onBackground
                    )
                )
                subTitle?.let {
                    Text(text = it, style = TextStyle(color = Color.Gray))
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun InformationCardPreview() {
    InformationCard(
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "version",
                tint = Color.Blue,
                modifier = Modifier.size(32.dp)
            )
        },
        title = "Version",
        subTitle = "1.0.0"
    )
}