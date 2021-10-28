package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.databinding.ActivityRequestHeadersBinding
import ch.rmy.android.http_shortcuts.dialogs.KeyValueDialog
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import io.reactivex.Completable

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

    private lateinit var binding: ActivityRequestHeadersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityRequestHeadersBinding.inflate(layoutInflater))
        setTitle(R.string.section_request_headers)

        initViews()
    }

    private fun initViews() {
        val adapter = destroyer.own(RequestHeaderAdapter(context, headers, variablePlaceholderProvider))

        val manager = LinearLayoutManager(context)
        binding.headerList.layoutManager = manager
        binding.headerList.setHasFixedSize(true)
        binding.headerList.adapter = adapter

        initDragOrdering()

        adapter.clickListener = { it.value?.let { header -> showEditDialog(header) } }
        binding.buttonAddHeader.applyTheme(themeHelper)
        binding.buttonAddHeader.setOnClickListener {
            showAddDialog()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { headers.size > 1 }
        dragOrderingHelper.attachTo(binding.headerList)
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
            suggestions = SUGGESTED_KEYS,
            keyValidator = { validateHeaderName(context, it) },
            valueValidator = { validateHeaderValue(context, it) },
        )
            .show(context)
            .flatMapCompletable { event ->
                when (event) {
                    is KeyValueDialog.Event.DataChangedEvent -> {
                        viewModel.updateHeader(headerId, event.data.first, event.data.second)
                    }
                    is KeyValueDialog.Event.DataRemovedEvent -> {
                        viewModel.removeHeader(headerId)
                    }
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
            suggestions = SUGGESTED_KEYS,
            keyValidator = { validateHeaderName(context, it) },
            valueValidator = { validateHeaderValue(context, it) },
        )
            .show(context)
            .flatMapCompletable { event ->
                when (event) {
                    is KeyValueDialog.Event.DataChangedEvent -> {
                        viewModel.addHeader(event.data.first, event.data.second)
                    }
                    else -> Completable.complete()
                }
            }
            .subscribe()
            .attachTo(destroyer)
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
            "Warning",
        )

        private fun validateHeaderName(context: Context, name: CharSequence): String? =
            name
                .firstOrNull { c ->
                    c <= '\u0020' || c >= '\u007f'
                }
                ?.let { invalidChar ->
                    context.getString(R.string.error_invalid_character, invalidChar)
                }

        private fun validateHeaderValue(context: Context, value: CharSequence): String? =
            value
                .firstOrNull { c ->
                    (c <= '\u001f' && c != '\t') || c >= '\u007f'
                }
                ?.let { invalidChar ->
                    context.getString(R.string.error_invalid_character, invalidChar)
                }

    }

}