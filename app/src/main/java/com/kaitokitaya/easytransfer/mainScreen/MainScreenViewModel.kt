package com.kaitokitaya.easytransfer.mainScreen

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaitokitaya.easytransfer.httpServer.ConnectiveManagerWrapper
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import com.kaitokitaya.easytransfer.mainScreen.model.ServerStatus
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class MainScreenViewModel(
    private val connectiveManagerWrapper: ConnectiveManagerWrapper,
    private val httpServer: HttpServer,
    val startStorageAccessPermissionRequest: VoidCallback
) : ViewModel() {
    private val _ipAddressFlow = MutableStateFlow<String?>(null)
    var ipAddress: StateFlow<String?> = _ipAddressFlow.asStateFlow()

    private val _serverStatusFlow = MutableStateFlow<ServerStatus>(ServerStatus.Standby)
    var serverStatus: StateFlow<ServerStatus> = _serverStatusFlow.asStateFlow()

    fun onStart() {
        viewModelScope.launch {
            onLoading()
            delay(100)
            _serverStatusFlow.update {
                ServerStatus.Launching
                httpServer.start()
            }
        }
        _ipAddressFlow.update {
            // TODO: No internet handling
            connectiveManagerWrapper.getIPAddress()
        }
    }

    fun onLoading() {
        _serverStatusFlow.update {
            when (it) {
                ServerStatus.Working -> ServerStatus.Shutdown
                ServerStatus.Standby -> ServerStatus.Launching
                else -> it
            }
        }
    }

    fun onStop() {
        viewModelScope.launch {
            onLoading()
            delay(100)
            _serverStatusFlow.update {
                httpServer.stop()
            }
        }
        _ipAddressFlow.update {
            null
        }
    }

    fun getDirectoryItem(): List<File>? {
        val root = Environment.getExternalStorageDirectory().absolutePath
        val directory = File(root)
        val directories = directory.listFiles()?.toList()
        return directories
    }
}