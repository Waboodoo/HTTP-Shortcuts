package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityRequestHeadersBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
class RequestHeadersActivity : BaseActivity() {

    private val viewModel: RequestHeadersViewModel by bindViewModel()
    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityRequestHeadersBinding

    private lateinit var adapter: RequestHeadersAdapter
    private var isDraggingEnabled = false

    override fun onCreated(savedState: Bundle?) {
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
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(RequestHeadersActivity::class.java)
}
