package com.kaitokitaya.easytransfer.httpServer

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.kaitokitaya.easytransfer.MainActivity
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.Url
import kotlinx.coroutines.launch

class HttpClient(private val activity: ComponentActivity) {
    val client = HttpClient(CIO)

    fun get(url: String) {
        activity.lifecycleScope.launch {
            client.get(urlString = url)
        }
    }

}