package com.kaitokitaya.easytransfer.screen.mainScreen

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaitokitaya.easytransfer.global.AppServiceState
import com.kaitokitaya.easytransfer.global.GlobalSettings
import com.kaitokitaya.easytransfer.global.GlobalSettingsProvider
import com.kaitokitaya.easytransfer.httpServer.ConnectiveManagerWrapper
import com.kaitokitaya.easytransfer.httpServer.HttpServer
import com.kaitokitaya.easytransfer.screen.mainScreen.model.ServerStatus
import com.kaitokitaya.easytransfer.originalType.VoidCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainScreenViewModel(
//    private val connectiveManagerWrapper: ConnectiveManagerWrapper,
//    private val httpServer: HttpServer,
    private val checkPermissions: VoidCallback,
    private val requestPermissions: VoidCallback,
) : ViewModel() {

    private lateinit var connectiveManagerWrapper: ConnectiveManagerWrapper
    private lateinit var httpServer: HttpServer

    private val _ipAddressFlow = MutableStateFlow<String?>(null)
    var ipAddress: StateFlow<String?> = _ipAddressFlow.asStateFlow()

    private val _serverStatusFlow = MutableStateFlow<ServerStatus>(ServerStatus.Standby)
    var serverStatus: StateFlow<ServerStatus> = _serverStatusFlow.asStateFlow()

    private val _isNeedRefreshFlow = MutableStateFlow<Boolean>(false)
    val isNeedRefresh: StateFlow<Boolean> = _isNeedRefreshFlow.asStateFlow()

    private val _appServiceStateFlow = MutableStateFlow<AppServiceState>(AppServiceState.Initializing)
    val appServiceState: StateFlow<AppServiceState> = _appServiceStateFlow.asStateFlow()

    companion object {
        private val TAG = MainScreenViewModel::class.java.simpleName
    }

    init {
        viewModelScope.launch {
            GlobalSettingsProvider.globalSettingsState.collect { state ->
                Timber.tag(TAG).d("Current state is $state")
                _appServiceStateFlow.update {
                    state.appServiceState
                }
            }
        }
    }

    fun initialize(server: HttpServer, connectiveManager: ConnectiveManagerWrapper) {
        checkPermissions()
        httpServer = server
        connectiveManagerWrapper = connectiveManager
        viewModelScope.launch {
            combine(
                httpServer.isNeedRefresh,
                connectiveManagerWrapper.isAvailable
            ) { isNeedRefreshValue, isAvailableValue ->
                Pair(
                    isNeedRefreshValue,
                    isAvailableValue
                )
            }.collect { (isNeedRefreshValue, isAvailableValue) ->
                _isNeedRefreshFlow.update {
                    isNeedRefreshValue
                }
                if (isAvailableValue) {
                    if (_serverStatusFlow.value == ServerStatus.Unavailable) {
                        _serverStatusFlow.update {
                            ServerStatus.Standby
                        }
                    }
                } else {
                    if (_serverStatusFlow.value != ServerStatus.Unavailable) {
                        _serverStatusFlow.update {
                            httpServer.gracefulUnavailable(_serverStatusFlow.value)
                        }
                    }
                }
            }
        }
    }

    fun onStart() {
        viewModelScope.launch {
            startServer()
        }
        _ipAddressFlow.update {
            connectiveManagerWrapper.getStringIpv4Address()
        }
    }

    fun onStop() {
        viewModelScope.launch {
            stopServer()
        }
        _ipAddressFlow.update {
            null
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _serverStatusFlow.update {
                ServerStatus.Refresh
            }
            stopServer()
            startServer()
            _serverStatusFlow.update {
                ServerStatus.Working
            }
            httpServer.changeIsNeedRefresh(isNeedRefresh = false)
        }
    }

    private suspend fun startServer() {
        onLoading()
        delay(100)
        _serverStatusFlow.update {
            ServerStatus.Launching
            httpServer.start()
        }
    }

    private suspend fun stopServer() {
        onLoading()
        delay(100)
        _serverStatusFlow.update {
            httpServer.stop()
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

    fun onTapGoSettings() {
        requestPermissions()
    }

    fun getDirectoryItem(): List<File>? {
        val root = Environment.getExternalStorageDirectory().absolutePath
        val directory = File(root)
        val directories = directory.listFiles()?.toList()
        return directories
    }
}