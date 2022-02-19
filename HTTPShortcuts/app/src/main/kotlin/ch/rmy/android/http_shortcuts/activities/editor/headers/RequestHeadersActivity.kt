package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityRequestHeadersBinding
import ch.rmy.android.http_shortcuts.dialogs.KeyValueDialog
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
class RequestHeadersActivity : BaseActivity() {

    private val viewModel: RequestHeadersViewModel by bindViewModel()
    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityRequestHeadersBinding

    private lateinit var adapter: RequestHeadersAdapter
    private var isDraggingEnabled = false

    override fun onCreate() {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityRequestHeadersBinding.inflate(layoutInflater))
        setTitle(R.string.section_request_headers)

        adapter = RequestHeadersAdapter(variablePlaceholderProvider)

        val manager = LinearLayoutManager(context)
        binding.headerList.layoutManager = manager
        binding.headerList.setHasFixedSize(true)
        binding.headerList.adapter = adapter

        binding.buttonAddHeader.applyTheme(themeHelper)
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        adapter.userEvents
            .subscribe { event ->
                when (event) {
                    is RequestHeadersAdapter.UserEvent.HeaderClicked -> viewModel.onHeaderClicked(event.id)
                }
            }
            .attachTo(destroyer)

        binding.buttonAddHeader.setOnClickListener {
            viewModel.onAddHeaderButtonClicked()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? RequestHeadersAdapter.HeaderViewHolder)?.headerId },
        )
        dragOrderingHelper.attachTo(binding.headerList)
        dragOrderingHelper.movementSource
            .subscribe { (headerId1, headerId2) ->
                viewModel.onHeaderMoved(headerId1, headerId2)
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            adapter.items = viewState.headerItems
            isDraggingEnabled = viewState.isDraggingEnabled
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is RequestHeadersEvent.ShowAddHeaderDialog -> {
                showAddDialog()
            }
            is RequestHeadersEvent.ShowEditHeaderDialog -> {
                showEditDialog(event.headerId, event.key, event.value)
            }
            else -> super.handleEvent(event)
        }
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
            .subscribe { event ->
                when (event) {
                    is KeyValueDialog.Event.DataChangedEvent -> {
                        viewModel.onAddHeaderDialogConfirmed(event.data.first, event.data.second)
                    }
                    else -> Unit
                }
            }
            .attachTo(destroyer)
    }

    private fun showEditDialog(headerId: String, key: String, value: String) {
        KeyValueDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_custom_header_edit),
            keyLabel = getString(R.string.label_custom_header_key),
            valueLabel = getString(R.string.label_custom_header_value),
            data = key to value,
            suggestions = SUGGESTED_KEYS,
            keyValidator = { validateHeaderName(context, it) },
            valueValidator = { validateHeaderValue(context, it) },
        )
            .show(context)
            .subscribe { event ->
                when (event) {
                    is KeyValueDialog.Event.DataChangedEvent -> {
                        viewModel.onEditHeaderDialogConfirmed(headerId, event.data.first, event.data.second)
                    }
                    is KeyValueDialog.Event.DataRemovedEvent -> {
                        viewModel.onRemoveHeaderButtonClicked(headerId)
                    }
                }
            }
            .attachTo(destroyer)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(RequestHeadersActivity::class.java)

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
