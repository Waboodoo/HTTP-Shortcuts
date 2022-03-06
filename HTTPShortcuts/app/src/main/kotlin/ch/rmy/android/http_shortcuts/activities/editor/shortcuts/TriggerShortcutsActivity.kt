package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.mapFor
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityTriggerShortcutsBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder

class TriggerShortcutsActivity : BaseActivity() {

    private val currentShortcutId: String? by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }

    private val viewModel: TriggerShortcutsViewModel by bindViewModel()

    private lateinit var binding: ActivityTriggerShortcutsBinding
    private lateinit var adapter: ShortcutsAdapter

    private var isDraggingEnabled = false

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(TriggerShortcutsViewModel.InitData(currentShortcutId))
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityTriggerShortcutsBinding.inflate(layoutInflater))
        setTitle(R.string.label_trigger_shortcuts)

        binding.buttonAddTrigger.applyTheme(themeHelper)

        val manager = LinearLayoutManager(context)
        binding.triggerShortcutsList.layoutManager = manager
        binding.triggerShortcutsList.setHasFixedSize(true)

        adapter = ShortcutsAdapter()
        binding.triggerShortcutsList.adapter = adapter
    }

    private fun initUserInputBindings() {
        adapter.userEvents
            .subscribe { event ->
                when (event) {
                    is ShortcutsAdapter.UserEvent.ShortcutClicked -> {
                        viewModel.onShortcutClicked(event.id)
                    }
                }
            }
            .attachTo(destroyer)

        binding.buttonAddTrigger.setOnClickListener {
            viewModel.onAddButtonClicked()
        }
        initDragOrdering()
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? ShortcutsAdapter.ShortcutViewHolder)?.shortcutId },
        )
        dragOrderingHelper.attachTo(binding.triggerShortcutsList)
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
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is TriggerShortcutsEvent.ShowShortcutPickerForAdding -> {
                showShortcutPickerForAdding(event.placeholders)
            }
            is TriggerShortcutsEvent.ShowRemoveShortcutDialog -> {
                showRemoveShortcutDialog(event.shortcutId, event.message)
            }
            else -> super.handleEvent(event)
        }
    }

    private fun showShortcutPickerForAdding(placeholders: List<ShortcutPlaceholder>) {
        DialogBuilder(context)
            .title(R.string.title_add_trigger_shortcut)
            .mapFor(placeholders) { shortcut ->
                item(name = shortcut.name, shortcutIcon = shortcut.icon) {
                    viewModel.onAddShortcutDialogConfirmed(shortcut.id)
                }
            }
            .showIfPossible()
    }

    private fun showRemoveShortcutDialog(shortcutId: String, message: Localizable) {
        DialogBuilder(context)
            .title(R.string.title_remove_trigger_shortcut)
            .message(message)
            .positive(R.string.dialog_remove) {
                viewModel.onRemoveShortcutDialogConfirmed(shortcutId)
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    class IntentBuilder : BaseIntentBuilder(TriggerShortcutsActivity::class.java) {

        fun shortcutId(shortcutId: String?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"
    }
}
