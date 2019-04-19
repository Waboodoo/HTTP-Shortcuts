package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        adapter.clickListener = ::showEditDialog
        addButton.setOnClickListener {
            showAddDialog()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper()
        dragOrderingHelper.attachTo(headerList)
        dragOrderingHelper.positionChangeSource
            .concatMapCompletable { (oldPosition, newPosition) ->
                viewModel.moveHeader(oldPosition, newPosition)
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun showEditDialog(headerData: LiveData<Header?>) {
        showToast("TODO")
    }

    private fun showAddDialog() {
        showToast("TODO")
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