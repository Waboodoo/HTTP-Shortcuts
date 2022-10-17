package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.whileLifecycleActive
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.databinding.ActivityRequestHeadersBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import javax.inject.Inject

class RequestHeadersActivity : BaseActivity() {

    @Inject
    lateinit var adapter: RequestHeadersAdapter

    private val viewModel: RequestHeadersViewModel by bindViewModel()

    private lateinit var binding: ActivityRequestHeadersBinding

    private var isDraggingEnabled = false

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityRequestHeadersBinding.inflate(layoutInflater))
        setTitle(R.string.section_request_headers)

        val manager = LinearLayoutManager(context)
        binding.headerList.layoutManager = manager
        binding.headerList.setHasFixedSize(true)
        binding.headerList.adapter = adapter

        binding.buttonAddHeader.applyTheme(themeHelper)
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        whileLifecycleActive {
            adapter.userEvents.collect { event ->
                when (event) {
                    is RequestHeadersAdapter.UserEvent.HeaderClicked -> viewModel.onHeaderClicked(event.id)
                }
            }
        }

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
        whileLifecycleActive {
            dragOrderingHelper.movementSource.collect { (headerId1, headerId2) ->
                viewModel.onHeaderMoved(headerId1, headerId2)
            }
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            adapter.items = viewState.headerItems
            isDraggingEnabled = viewState.isDraggingEnabled
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(RequestHeadersActivity::class)
}
