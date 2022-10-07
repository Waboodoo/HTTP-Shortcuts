package ch.rmy.android.http_shortcuts.activities.settings.polling

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
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.databinding.ActivityPollingShortcutsBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme

class ShortcutPollingActivity : BaseActivity() {

    private val viewModel: ShortcutPollingViewModel by bindViewModel()

    private lateinit var binding: ActivityPollingShortcutsBinding
    private lateinit var adapter: PollingShortcutsAdapter

    private var isDraggingEnabled = false

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityPollingShortcutsBinding.inflate(layoutInflater))
        setTitle(R.string.title_polling_shortcuts)

        binding.buttonAddPollingShortcut.applyTheme(themeHelper)

        val manager = LinearLayoutManager(context)
        binding.shortcutPollingList.layoutManager = manager
        binding.shortcutPollingList.setHasFixedSize(true)

        adapter = PollingShortcutsAdapter()
        binding.shortcutPollingList.adapter = adapter
    }

    private fun initUserInputBindings() {
        adapter.userEvents
            .subscribe { event ->
                when (event) {
                    is PollingShortcutsAdapter.UserEvent.ShortcutClicked -> {
                        viewModel.onShortcutClicked(event.id)
                    }
                }
            }
            .attachTo(destroyer)

        binding.buttonAddPollingShortcut.setOnClickListener {
            viewModel.onAddButtonClicked()
        }
        initDragOrdering()
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? PollingShortcutsAdapter.ShortcutViewHolder)?.id },
        )
        dragOrderingHelper.attachTo(binding.shortcutPollingList)
        dragOrderingHelper.movementSource
            .subscribe { (shortcutId1, shortcutId2) ->
                viewModel.onShortcutMoved(shortcutId1, shortcutId2)
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.shortcuts
            isDraggingEnabled = viewState.isDraggingEnabled
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    class IntentBuilder : BaseIntentBuilder(ShortcutPollingActivity::class) {

        fun shortcutId(shortcutId: ShortcutId?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"
    }
}
