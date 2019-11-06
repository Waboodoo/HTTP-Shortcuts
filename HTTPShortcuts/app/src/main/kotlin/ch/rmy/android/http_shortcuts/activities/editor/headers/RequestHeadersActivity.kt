package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.dialogs.KeyValueDialog
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.Completable
import kotterknife.bindView

class RequestHeadersActivity : BaseActivity() {

    private val viewModel: RequestHeadersViewModel by bindViewModel()
    private val variablesData by lazy {
        viewModel.variables
    }
    private val headers by lazy {
        viewModel.headers
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    private val headerList: RecyclerView by bindView(R.id.header_list)
    private val addButton: FloatingActionButton by bindView(R.id.button_add_header)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_headers)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        val adapter = destroyer.own(RequestHeaderAdapter(context, headers, variablePlaceholderProvider))

        val manager = LinearLayoutManager(context)
        headerList.layoutManager = manager
        headerList.setHasFixedSize(true)
        headerList.adapter = adapter

        initDragOrdering()

        adapter.clickListener = { it.value?.let { header -> showEditDialog(header) } }
        addButton.applyTheme(themeHelper)
        addButton.setOnClickListener {
            showAddDialog()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { headers.size > 1 }
        dragOrderingHelper.attachTo(headerList)
        dragOrderingHelper.positionChangeSource
            .concatMapCompletable { (oldPosition, newPosition) ->
                viewModel.moveHeader(oldPosition, newPosition)
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun showEditDialog(header: Header) {
        val headerId = header.id
        KeyValueDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_custom_header_edit),
            keyLabel = getString(R.string.label_custom_header_key),
            valueLabel = getString(R.string.label_custom_header_value),
            data = header.key to header.value,
            suggestions = SUGGESTED_KEYS
        )
            .show(context)
            .flatMapCompletable { event ->
                when (event) {
                    is KeyValueDialog.DataChangedEvent -> viewModel.updateHeader(headerId, event.data.first, event.data.second)
                    is KeyValueDialog.DataRemovedEvent -> viewModel.removeHeader(headerId)
                    else -> Completable.complete()
                }
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun showAddDialog() {
        KeyValueDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_custom_header_add),
            keyLabel = getString(R.string.label_custom_header_key),
            valueLabel = getString(R.string.label_custom_header_value),
            suggestions = SUGGESTED_KEYS
        )
            .show(context)
            .flatMapCompletable { event ->
                when (event) {
                    is KeyValueDialog.DataChangedEvent -> viewModel.addHeader(event.data.first, event.data.second)
                    else -> Completable.complete()
                }
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun bindViewsToViewModel() {

    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, RequestHeadersActivity::class.java)

    companion object {

        val SUGGESTED_KEYS = arrayOf(
            "Accept",
            "Accept-Charset",
            "Accept-Encoding",
            "Accept-Language",
            "Accept-Datetime",
            "Authorization",
            "Cache-Control",
            "Connection",
            "Cookie",
            "Content-Length",
            "Content-MD5",
            "Content-Type",
            "Date",
            "Expect",
            "Forwarded",
            "From",
            "Host",
            "If-Match",
            "If-Modified-Since",
            "If-None-Match",
            "If-Range",
            "If-Unmodified-Since",
            "Max-Forwards",
            "Origin",
            "Pragma",
            "Proxy-Authorization",
            "Range",
            "Referer",
            "TE",
            "User-Agent",
            "Upgrade",
            "Via",
            "Warning"
        )

    }

}