package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogResult
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.request.userAgent
import io.ktor.response.respond
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotterknife.bindView
import java.io.InputStream

class RemoteEditActivity : BaseActivity() {

    private val viewModel: RemoteEditViewModel by bindViewModel()

    private var server: ApplicationEngine? = null

    private val ipAddressView: TextView by bindView(R.id.ip_address)
    private val instructions: TextView by bindView(R.id.instructions)
    private val requestLog: TextView by bindView(R.id.request_log)
    private val requestLogContainer: ScrollView by bindView(R.id.request_log_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_edit)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        instructions.text = HTMLUtil.format(getString(
            R.string.instructions_remote_edit,
            "<b>$REMOTE_EDIT_ADDRESS</b>",
        ))

        RemoteEditWarningDialog(context)
            .showIfNeeded()
            .subscribe { dialogResult ->
                if (dialogResult == DialogResult.CANCELED) {
                    finish()
                } else {
                    setUp()
                }
            }

        // TODO: Subscribe IntentFilter to observe network and IP address changes

        viewModel.events.observe(this, { events ->
            requestLog.text = events.joinToString("\n\n")
            requestLogContainer.fullScroll(ScrollView.FOCUS_DOWN)
        })
    }

    private fun setUp() {
        server = createServer()
        startServer()
        updateIPAddressView()
    }

    private fun startServer() {
        server?.let {
            it.start(wait = false)
            logEvent(getString(R.string.request_log_shortcuts_server_started))
        }
    }

    private fun createServer(): ApplicationEngine =
        embeddedServer(Netty, SERVER_PORT) {
            install(CORS) {
                anyHost()
            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            routing {
                get("/base") {
                    call.respondTextWriter(ContentType.Application.Json) {
                        export(this)
                        logEvent(getString(R.string.request_log_shortcuts_loaded), call.request.userAgent())
                    }
                }
                post("/base") {
                    val importStatus = import(call.request.receiveChannel().toInputStream())
                    call.respond(mapOf(
                        "status" to "success",
                        "updatedShortcuts" to importStatus.importedShortcuts,
                    ))
                    logEvent(getString(R.string.request_log_shortcuts_saved), call.request.userAgent())
                }
            }
        }

    private fun logEvent(event: String, userAgent: String? = null) {
        runOnUiThread {
            viewModel.onApiEvent(if (userAgent.isNullOrEmpty()) {
                event
            } else {
                "$event ($userAgent)"
            })
        }
    }

    private fun updateIPAddressView() {
        ipAddressView.text = server
            ?.let {
                NetworkUtil.getIPV4Address(context)
            }
            ?: "-"
    }

    private fun export(writer: Appendable) =
        Exporter(context).export(writer)

    private fun import(inputStream: InputStream) =
        Importer(context).import(inputStream)

    override fun onStart() {
        super.onStart()
        updateIPAddressView()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }

    private fun stopServer() {
        server?.let {
            it.stop(1000, 1000)
            logEvent(getString(R.string.request_log_shortcuts_server_stopped))
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, RemoteEditActivity::class.java)

    companion object {

        private const val SERVER_PORT = 9274

        private const val REMOTE_EDIT_ADDRESS = "http-shortcuts.rmy.ch/edit"

    }

}
