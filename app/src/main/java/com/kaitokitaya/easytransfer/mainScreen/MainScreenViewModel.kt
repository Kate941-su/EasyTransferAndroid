package com.kaitokitaya.easytransfer.mainScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaitokitaya.easytransfer.httpServer.ConnectiveManagerWrapper
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainScreenViewModel(private val connectiveManagerWrapper: ConnectiveManagerWrapper): ViewModel() {
    private val _ipAddressFlow = MutableStateFlow<String?>(null)
    var ipAddress: StateFlow<String?> = _ipAddressFlow.asStateFlow()
    fun onTapStart() {
        HttpServer.start()
        _ipAddressFlow.update {
            connectiveManagerWrapper.getIPAddress()
        }
    }

    fun onTapStop() {
        HttpServer.stop()
        _ipAddressFlow.update {
            null
        }
    }




}