package com.kaitokitaya.easytransfer.mainScreen

import androidx.lifecycle.ViewModel
import com.kaitokitaya.easytransfer.httpServer.HttpServer

class MainScreenViewModel: ViewModel() {
    fun onTapStart() {
        HttpServer.start()
    }
}