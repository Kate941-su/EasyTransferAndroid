package com.kaitokitaya.easytransfer.mainScreen

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaitokitaya.easytransfer.httpServer.ConnectiveManagerWrapper
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileFilter

class MainScreenViewModel(
    private val connectiveManagerWrapper: ConnectiveManagerWrapper,
    private val httpServer: HttpServer,
    val startStorageAccessPermissionRequest: VoidCallback
) : ViewModel() {
    private val _ipAddressFlow = MutableStateFlow<String?>(null)
    var ipAddress: StateFlow<String?> = _ipAddressFlow.asStateFlow()

    fun onTapStart() {
        httpServer.start()
        _ipAddressFlow.update {
            connectiveManagerWrapper.getIPAddress()
        }
    }

    fun onTapStop() {
        httpServer.stop()
        _ipAddressFlow.update {
            null
        }
    }

    fun getDirectoryItem():List<File>? {
        val root = Environment.getExternalStorageDirectory().absolutePath
        val directory = File(root)
//        val directories = directory.listFiles( FileFilter {
//            it.isDirectory
//        })?.toList()
        val directories = directory.listFiles().toList()
        return directories
    }
}