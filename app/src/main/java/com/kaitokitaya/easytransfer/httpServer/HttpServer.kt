package com.kaitokitaya.easytransfer.httpServer

import android.system.Os.link
import io.ktor.events.EventDefinition
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.response.responseType
import io.ktor.server.routing.routing
import kotlinx.css.Color
import kotlinx.css.CssBuilder
import kotlinx.css.Margin
import kotlinx.css.backgroundColor
import kotlinx.css.body
import kotlinx.css.color
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.h1
import kotlinx.html.link
import kotlinx.html.p
import kotlinx.html.title
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import timber.log.Timber

sealed class ServerStatus {
    data object Active : ServerStatus()
    data object Inactive : ServerStatus()
}

object HttpServer {

    private var server: NettyApplicationEngine? = null
    private const val TAG = "HttpServer"
    const val PORT = 8080

    private val logger by lazy { LoggerFactory.getLogger("ApplicationLogger") }
    private val requestLogs = mutableListOf<String>()
    private val responseLogs = mutableListOf<String>()

    fun start(): ServerStatus {
        try {
            server = embeddedServer(Netty, port = PORT) {
                callLoggingModule()
                routingModule()
                eventSubscriptionModule()
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

    private fun Application.callLoggingModule() {
        install(CallLogging) {
            // TODO: Logging with log4j
            level = Level.INFO
            filter { call -> call.request.httpMethod.value == "GET" }
            mdc("method") { call -> call.request.httpMethod.value }
            mdc("uri") { call -> call.request.uri }
            mdc("clientIP") { call -> call.request.origin.remoteHost }
        }
    }

    private fun Application.eventSubscriptionModule() {
        install(responsePlugin)
        environment.monitor.subscribe(ResponseSendEvent) { call ->
            val logEntry = "Header: ${call.response.headers}, " +
                    "Type: ${call.response.responseType},"
            val uri = call.request.uri
            val method = call.request.httpMethod.value
            val clientIP = call.request.origin.remoteHost
            Timber.tag(TAG).d("url: $uri, method: $method, clientIP: $clientIP")
            responseLogs.add(logEntry)
        }
    }

    private val ResponseSendEvent: EventDefinition<ApplicationCall> = EventDefinition()

    private val responsePlugin = createApplicationPlugin(name = "ResponsePlugin") {
        on(ResponseSent) { call ->
                this@createApplicationPlugin.application.environment.monitor.raise(ResponseSendEvent, call)
        }
    }

    private val ApplicationMonitoringPlugin = createApplicationPlugin(name = "ApplicationMonitoringPlugin") {
        on(ResponseSent) { call ->
            this@createApplicationPlugin.application.environment.monitor.raise(ResponseSendEvent, call)
        }
    }

    suspend inline fun ApplicationCall.respondCss(builder: CssBuilder.() -> Unit) {
        this.respondText(CssBuilder().apply(builder).toString(), ContentType.Text.CSS)
    }

    private fun Application.routingModule() {
        routing {
            get("/") {
                call.respondHtml {
                    head {
                        link(rel = "stylesheet", href = "/styles.css", type = "text/css")
                    }
                    body {
                        h1 { +"Hello, World!" }
                        p { +"This is a sample Ktor application with HTML and CSS." }
                        a ("https://www.google.co.jp/"){ +"Go to google" }
                    }
                }
            }
            get("/styles.css") {
                call.respondCss {
                    body {
                        backgroundColor = Color.darkBlue
                        margin = Margin(0.px)
                    }
                    rule("h1") {
                        color = Color.white
                    }
                }
            }
        }
    }
}