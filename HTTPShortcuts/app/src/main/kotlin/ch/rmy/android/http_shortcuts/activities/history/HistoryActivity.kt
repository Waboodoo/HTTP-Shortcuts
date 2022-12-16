package ch.rmy.android.http_shortcuts.activities.history

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.whileLifecycleActive
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityHistoryBinding

class HistoryActivity : BaseActivity() {

    private val viewModel: HistoryViewModel by bindViewModel()

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter

    private var clearMenuItem: MenuItem? = null

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityHistoryBinding.inflate(layoutInflater))
        setTitle(R.string.title_event_history)

        adapter = HistoryAdapter()

        val manager = LinearLayoutManager(context)
        binding.historyEventList.layoutManager = manager
        binding.historyEventList.setHasFixedSize(true)
        binding.historyEventList.adapter = adapter

        enableAutoScrollToTop(manager)
    }

    private fun initUserInputBindings() {
        whileLifecycleActive {
            adapter.userEvents.collect { event ->
                when (event) {
                    is HistoryAdapter.UserEvent.HistoryEventLongPressed -> viewModel.onHistoryEventLongPressed(event.id)
                }
            }
        }
    }

    private fun enableAutoScrollToTop(manager: LinearLayoutManager) {
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0 && manager.findFirstCompletelyVisibleItemPosition() <= 0) {
                    binding.historyEventList.scrollToPosition(0)
                }
            }
        })
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.loadingIndicator.isVisible = false
            adapter.items = viewState.historyItems
            applyViewStateToMenuItems(viewState)
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_activity_menu, menu)
        clearMenuItem = menu.findItem(R.id.action_clear_history)
        viewModel.latestViewState?.let(::applyViewStateToMenuItems)
        return super.onCreateOptionsMenu(menu)
    }

    private fun applyViewStateToMenuItems(viewState: HistoryViewState) {
        clearMenuItem?.isVisible = viewState.isClearButtonVisible
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_clear_history -> consume(viewModel::onClearHistoryButtonClicked)
        else -> super.onOptionsItemSelected(item)
    }

    class IntentBuilder : BaseIntentBuilder(HistoryActivity::class)
}
