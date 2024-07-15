package com.kaitokitaya.easytransfer.adsComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.kaitokitaya.easytransfer.constant.Constant

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            // on below line specifying ad view.
            AdView(context).apply {
                // on below line specifying ad size
                //adSize = AdSize.BANNER
                // on below line specifying ad unit id
                // currently added a test ad unit id.
                setAdSize(AdSize.BANNER)
                adUnitId = Constant.testAdmobId
                // calling load ad to load our ad.
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}