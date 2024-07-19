package com.kaitokitaya.easytransfer.httpServer

import android.os.Environment
import androidx.core.content.ContextCompat
import com.kaitokitaya.easytransfer.fileHandler.FileHandler
import com.kaitokitaya.easytransfer.screen.mainScreen.model.ServerStatus
import com.kaitokitaya.easytransfer.util.Util
import io.ktor.events.EventDefinition
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.*
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.uri
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.response.responseType
import io.ktor.server.routing.routing
import io.netty.handler.codec.DefaultHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.css.Align
import kotlinx.css.Border
import kotlinx.css.BorderCollapse
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.CssBuilder
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.Margin
import kotlinx.css.Outline
import kotlinx.css.Overflow
import kotlinx.css.Padding
import kotlinx.css.TableLayout
import kotlinx.css.TextAlign
import kotlinx.css.a
import kotlinx.css.alignItems
import kotlinx.css.background
import kotlinx.css.backgroundColor
import kotlinx.css.body
import kotlinx.css.border
import kotlinx.css.borderCollapse
import kotlinx.css.borderRadius
import kotlinx.css.borderSpacing
import kotlinx.css.color
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.fontSize
import kotlinx.css.h3
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.marginRight
import kotlinx.css.outline
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.tableLayout
import kotlinx.css.textAlign
import kotlinx.css.width
import kotlinx.html.ButtonType
import kotlinx.html.FormEncType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.caption
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.head
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.input
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
import java.io.File
import java.nio.file.Files

// TODO: Use hilt
class HttpServer(private val connectiveManagerWrapper: ConnectiveManagerWrapper, private val httpClient: HttpClient) {
    companion object {
        const val PORT = 8080
    }

    private var server: NettyApplicationEngine? = null
    private val TAG = "HttpServer"
    private val responseLogs = mutableListOf<String>()
    private val rootPath = Environment.getExternalStorageDirectory().absolutePath
    private var absDirPathList = mutableListOf<String>()
    private var absFilePathList = mutableListOf<String>()
    private val entireTreeMap = mutableMapOf<String, List<String>>()

    private var invalidFileName: String? = null
    private var isFileEmpty = false

    private val _isNeedRefreshFlow = MutableStateFlow(false)
    val isNeedRefresh: MutableStateFlow<Boolean> get() = _isNeedRefreshFlow

    init {
        connectiveManagerWrapper.checkNetworkCapabilities()
        Timber.tag(TAG).d(absDirPathList.toString())
    }

    fun changeIsNeedRefresh(isNeedRefresh: Boolean) {
        _isNeedRefreshFlow.update {
            isNeedRefresh
        }
    }

    private fun initialize() {
        absDirPathList = mutableListOf()
        absFilePathList = mutableListOf()
        absDirPathList.add(rootPath)
        getAllDirectoryPath(Environment.getExternalStorageDirectory().absolutePath)
    }

    private fun getAllDirectoryPath(path: String) {
        val fileList = FileHandler.getAllFilesFromAbsPath(path)
        entireTreeMap[path] = fileList?.let { list -> list.map { it.absolutePath } } ?: emptyList()
        fileList?.forEach {
            val absPath = it.absolutePath
            if (it.isDirectory) {
                absDirPathList.add(absPath)
                getAllDirectoryPath(absPath)
            } else {
                absFilePathList.add(absPath)
            }
        }
    }


    fun start(): ServerStatus {
        initialize()
        try {
            server = embeddedServer(Netty, port = PORT) {
                callLoggingModule()
                routingModule()
                eventSubscriptionModule()
            }
            server?.start(wait = false)
            Timber.tag(TAG).d("Ktor server started on port $PORT")
            return ServerStatus.Working
        } catch (e: Throwable) {
            // TODO: If needed I have to implement proper error handling.
            Timber.tag(TAG).d("Failed to start server instance cause of '$e'")
            return ServerStatus.Standby
        }
    }

    fun stop(): ServerStatus {
        try {
            server?.stop(1000, 10000)
            changeIsNeedRefresh(isNeedRefresh = false)
            return ServerStatus.Standby
        } catch (e: Throwable) {
            Timber.tag(TAG).d("Failed to stop server instance cause of '$e'")
            return ServerStatus.Working
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

    private fun getRelativePath(absPath: String): String {
        val tempAbsPath = absPath.replace(Regex("/storage/emulated/0"), "")
        return tempAbsPath.replace(Regex("/storage/emulated/0/"), "")
    }

    private fun Application.routingModule() {
        routing {
            absFilePathList.forEach { absFilePath ->
                val relativePath = getRelativePath(absPath = absFilePath)
                // For downloading files
                get(relativePath) {
                    val file = File(absFilePath)
                    if (file.exists()) {
                        call.response.header(HttpHeaders.ContentDisposition, "attachment")
                        call.respondFile(file)
                    }
                }
            }


            absDirPathList.forEach { absPath ->
                val relativePath = getRelativePath(absPath = absPath)
                // For uploading files
                post("$relativePath/upload") {
                    Timber.tag(TAG)
                        .d("Upload Path => http://${connectiveManagerWrapper.getIPAddress()}:8080/$relativePath/upload")
                    val request = call.receiveMultipart()
                    request.forEachPart {
                        when (it) {
                            is PartData.FileItem -> {
                                val fileBytes = it.streamProvider().readBytes()
                                if (fileBytes.isEmpty()) {
                                    isFileEmpty = true
                                    invalidFileName = null
                                    call.respondRedirect(
                                        "http://${connectiveManagerWrapper.getIPAddress()}:8080/${
                                            getRelativePath(
                                                absPath = absPath
                                            )
                                        }"
                                    )
                                    return@forEachPart
                                } else {
                                    isFileEmpty = false
                                }
                                val fileName =
                                    if (it.originalFileName != null) FileHandler.convertFileName(fileName = it.originalFileName!!) else "uploaded_file"
                                val file = File(absPath, fileName)
                                try {
                                    file.outputStream().use { output ->
                                        output.write(fileBytes)
                                    }
                                    absFilePathList.add(file.absolutePath)
                                    invalidFileName = null
                                    changeIsNeedRefresh(isNeedRefresh = true)
                                } catch (e: Throwable) {
                                    assert(false) {
                                        Timber.tag(TAG).d("Could not download file data to storage.")
                                        invalidFileName = fileName
                                        call.respondRedirect(
                                            "http://${connectiveManagerWrapper.getIPAddress()}:8080/${
                                                getRelativePath(
                                                    absPath = absPath
                                                )
                                            }"
                                        )
                                    }
                                }
                            }

                            else -> {
                                Timber.tag(TAG).d("NO ITEMS")
                            }
                        }
                        it.dispose()
                    }
                    call.respondRedirect(
                        "http://${connectiveManagerWrapper.getIPAddress()}:8080/${getRelativePath(absPath = absPath)}"
                    )
                }

                // For showing directories and files
                get(relativePath) {
                    call.respondHtml(HttpStatusCode.OK) {
                        val fileList = FileHandler.getAllFilesFromAbsPath(absPath)
                        fileList?.let { updatedPathList ->
                            entireTreeMap[absPath]?.let { cachedPathList ->
                                if (updatedPathList.size != cachedPathList.size) {
                                    _isNeedRefreshFlow.value = true
                                }
                            }
                        }
                        head {
                            link(rel = "stylesheet", href = "/styles.css", type = "text/css")
                            link(
                                rel = "stylesheet",
                                href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css"
                            )
                            script {
                                unsafe {
                                    raw(
                                        """
                                    function onDownloadClick(path) {
                                        console.log(path)
                                        fetch(`/?download=${"$"}{encodeURIComponent(path)}`)
                                    }
                                """
                                    )
                                }
                            }
                        }
                        body {
                            div(classes = "table_component") {
                                h1 { if (relativePath == "") +"/" else +relativePath }
                                invalidFileName?.let {
                                    h3(classes = "red_string") { +"$it is invalid file format." }
                                }
                                if (isFileEmpty) {
                                    h3(classes = "red_string") { +"File is empty." }
                                }
                                table {
                                    thead {
                                        tr {
                                            th { +"Name" }
                                            th { +"Last modified" }
                                            th { +"Size" }
                                            th { +"" }
                                        }

                                    }
                                    tbody {

                                        tr {
                                            td {
                                                a(
                                                    href = "http://${connectiveManagerWrapper.getIPAddress()}:8080/${
                                                        FileHandler.getParentPath(
                                                            relativePath
                                                        )
                                                    } "
                                                ) {
                                                    +"Parent directory"
                                                }
                                            }
                                        }
                                        fileList?.forEach {
                                            tr {
                                                td {
                                                    if (it.isDirectory) {
                                                        a(
                                                            href = "http://${connectiveManagerWrapper.getIPAddress()}:8080/${
                                                                getRelativePath(
                                                                    it.absolutePath
                                                                )
                                                            }",
                                                            classes = "directory"
                                                        ) {
                                                            +it.name
                                                        }
                                                    } else {
                                                        val displayFileName =
                                                            FileHandler.getShortName(input = it.name, maxLength = 25)
                                                        +displayFileName
                                                    }
                                                }

                                                td {
                                                    +Util.getDataTime(epoch = it.lastModified())
                                                }
                                                td {
                                                    if (it.isDirectory) {
                                                        +"-"
                                                    } else {
                                                        val fileSize = Files.size(it.toPath()).toDouble() / 1000000
                                                        +"${fileSize}MB"
                                                    }
                                                }
                                                td {
                                                    if (it.isFile) {
                                                        a(
                                                            href = "http://${connectiveManagerWrapper.getIPAddress()}:8080/${
                                                                getRelativePath(
                                                                    it.absolutePath
                                                                )
                                                            }",
                                                        ) {
                                                            i(classes = "fa-solid fa-file-arrow-down") {}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        tr {
                                            td {
                                                form(
                                                    method = FormMethod.post,
                                                    encType = FormEncType.multipartFormData,
                                                    action = "http://${connectiveManagerWrapper.getIPAddress()}:8080/${
                                                        getRelativePath(
                                                            absPath = absPath
                                                        )
                                                    }/upload",
                                                ) {
                                                    input(
                                                        type = InputType.file,
                                                        name = "file",
                                                        classes = "clear-decoration"
                                                    ) {}
                                                    input(type = InputType.submit, classes = "clear-decoration") {
                                                        value = "Upload"
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
            }
            get("/styles.css") {
                call.respondCss {
                    body {
                        backgroundColor = Color.white
                        margin = Margin(10.px)
                    }
                    rule("h1") {
                        color = Color.black
                    }
                    rule(".file") {
                        color = Color.black
                    }
                    rule(".directory") {
                        color = Color.blue
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

                    rule(".icon_button") {
                        display = Display.inlineFlex
                        alignItems = Align.center
                        padding = Padding(vertical = LinearDimension("10px"), horizontal = LinearDimension("20px"))
                        fontSize = LinearDimension("18px")
                        color = Color.blue
                        border = Border.none
                        outline = Outline.none
                        background = "transparent"
                        cursor = Cursor.pointer
                    }

                    rule(".icon-button i") {
                        marginRight = LinearDimension("8px")
                    }
                    rule(".red_string") {
                        color = Color.red
                    }
                }
            }
        }
    }
}