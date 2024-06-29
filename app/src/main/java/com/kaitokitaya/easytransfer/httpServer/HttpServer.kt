package com.kaitokitaya.easytransfer.httpServer

import com.kaitokitaya.easytransfer.fileHandler.FileHandler
import com.kaitokitaya.easytransfer.util.Util
import io.ktor.events.EventDefinition
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.response.responseType
import io.ktor.server.routing.routing
import io.ktor.server.util.getValue
import kotlinx.css.Border
import kotlinx.css.BorderCollapse
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.CssBuilder
import kotlinx.css.Cursor
import kotlinx.css.LinearDimension
import kotlinx.css.Margin
import kotlinx.css.Outline
import kotlinx.css.Overflow
import kotlinx.css.Padding
import kotlinx.css.TableLayout
import kotlinx.css.TextAlign
import kotlinx.css.background
import kotlinx.css.backgroundColor
import kotlinx.css.body
import kotlinx.css.border
import kotlinx.css.borderCollapse
import kotlinx.css.borderSpacing
import kotlinx.css.color
import kotlinx.css.cursor
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.outline
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.script
import kotlinx.css.tableLayout
import kotlinx.css.textAlign
import kotlinx.css.width
import kotlinx.html.ButtonType
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.caption
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.onClick
import kotlinx.html.script
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import timber.log.Timber
import java.nio.file.Files

sealed class ServerStatus {
    data object Active : ServerStatus()
    data object Inactive : ServerStatus()
}

// TODO: Use hilt
class HttpServer(private val connectiveManagerWrapper: ConnectiveManagerWrapper, private val httpClient: HttpClient) {

    companion object {
        const val PORT = 8080
    }

    init {
        val test = FileHandler.getAllList("/")
    }

    private var server: NettyApplicationEngine? = null
    private val TAG = "HttpServer"
    private val queryKey = "buttonText"


    private val logger by lazy { LoggerFactory.getLogger("ApplicationLogger") }
    private val requestLogs = mutableListOf<String>()
    private val responseLogs = mutableListOf<String>()

    var currentDirectory = "/"

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
            get(currentDirectory) {
                call.respondHtml(HttpStatusCode.OK) {
                    val nextDirectory = call.request.queryParameters[queryKey]
                    nextDirectory?.let {
                        currentDirectory = "$currentDirectory/$it"
                    }
                    val fileList = FileHandler.getAllList(currentDirectory)
                    head {
                        link(rel = "stylesheet", href = "/styles.css", type = "text/css")
                        script {
                            unsafe {
                                raw(
                                    """
                                    function onButtonClick() {
                                        const buttonText = document.getElementById("directoryButton").textContent;
                                        fetch(`/?buttonText=${"$"}{encodeURIComponent(buttonText)}`)
                                    }
                                """
                                )
                            }
                        }
                    }
                    body {
                        div(classes = "table_component") {
                            table {
                                caption {
                                    h1 { +currentDirectory }
                                }
                                thead {
                                    tr {
                                        th { +"Name" }
                                        th { +"Last modified" }
                                        th { +"Size" }
                                    }

                                }
                                tbody {
                                    fileList?.forEach {
                                        tr {
                                            td {
                                                if (it.isDirectory) {
                                                    button(
                                                        classes = "clear-decoration",
                                                        type = ButtonType.button,
                                                    ) {
                                                        id = "directoryButton"
                                                        onClick = "onButtonClick()"
                                                        +it.name
                                                    }
                                                } else {
                                                    +it.name
                                                }
                                            }

                                            td {
                                                +Util.getDataTime(epoch = it.lastModified())
                                            }
                                            td {
                                                if (it.isDirectory) {
                                                    +"-"
                                                } else {
                                                    +Files.size(it.toPath()).toString()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            get("/styles.css") {
                call.respondCss {
                    body {
                        backgroundColor = Color.white
                        margin = Margin(10.px)
                    }
                    rule("h1") {
                        color = Color.blue
                    }
                    rule(".file") {
                        color = Color.black
                    }

                    rule(".clear-decoration") {
                        color = Color.blue
                        border = Border.none
                        outline = Outline.none
                        background = "transparent"
                        cursor = Cursor.pointer
                    }

                    // Table Style
                    rule(".table_component") {
                        overflow = Overflow.auto
                        width = LinearDimension.fillAvailable
                    }
                    rule(".table_component table")
                    {
                        border = Border(style = BorderStyle.solid, width = LinearDimension("0px"), color = Color.white)
                        height = LinearDimension.fillAvailable
                        width = LinearDimension.fillAvailable
                        tableLayout = TableLayout.fixed
                        borderCollapse = BorderCollapse.collapse;
                        borderSpacing = LinearDimension("1px");
                        textAlign = TextAlign.left;
                    }

                    rule(".table_component caption") {
                        textAlign = TextAlign.left;
                    }

                    rule(".table_component th") {
                        border = Border(style = BorderStyle.solid, width = LinearDimension("0px"), color = Color.white)
                        color = Color.black
                        padding = Padding(LinearDimension("5px"))
                    }

                    rule(".table_component td") {
                        border = Border(style = BorderStyle.solid, width = LinearDimension("0px"), color = Color.white)
                        color = Color.black
                        padding = Padding(LinearDimension("5px"))
                    }

                }
            }
        }
    }
}