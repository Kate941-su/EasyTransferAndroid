package com.kaitokitaya.easytransfer.httpServer

import android.util.Log
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import timber.log.Timber

sealed class ServerStatus {
    data object Active : ServerStatus()
    data object Inactive : ServerStatus()
}

object HttpServer {

    private var server: NettyApplicationEngine? = null
    private const val TAG = "HttpServer"
    const val PORT = 8080
    fun start(): ServerStatus {
        try {
            server = embeddedServer(Netty, port = PORT) {
                routing {
                    get("/") {
                        call.respondText("Hello, World!", ContentType.Text.Html)
                    }
                }
            }
            server?.start(wait = false)
            Timber.tag(TAG).d("Ktor server started on port $PORT")
            return ServerStatus.Active
        } catch (e: Throwable) {
            // TODO: If needed I have to implement proper error handling.
            Timber.tag(TAG).d("Failed to start server instance cause of '$e'")
            return ServerStatus.Inactive
        }
    }

    fun stop(): ServerStatus {
        try {
            server?.stop(1000, 10000)
            return ServerStatus.Inactive
        } catch (e: Throwable) {
            Timber.tag(TAG).d("Failed to stop server instance cause of '$e'")
            return ServerStatus.Active
        }
    }
}