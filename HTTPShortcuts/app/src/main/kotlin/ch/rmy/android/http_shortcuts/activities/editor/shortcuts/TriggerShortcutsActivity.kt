package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityTriggerShortcutsBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper

class TriggerShortcutsActivity : BaseActivity() {

    private val currentShortcutId: String? by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }

    private val viewModel: TriggerShortcutsViewModel by bindViewModel()
    
    private lateinit var binding: ActivityTriggerShortcutsBinding

    private val shortcutsData by lazy {
        viewModel.shortcuts
    }
    private val shortcutPlaceholderProvider by lazy {
        ShortcutPlaceholderProvider(shortcutsData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityTriggerShortcutsBinding.inflate(layoutInflater))
        setTitle(R.string.label_trigger_shortcuts)

        initViews()
    }

    private fun initViews() {
        binding.buttonAddTrigger.applyTheme(themeHelper)
        binding.buttonAddTrigger.setOnClickListener {
            openShortcutPickerForAdding()
        }

        val manager = LinearLayoutManager(context)
        binding.triggerShortcutsList.layoutManager = manager
        binding.triggerShortcutsList.setHasFixedSize(true)

        val adapter = ShortcutsAdapter(this, viewModel.triggerShortcuts)
        adapter.itemClickListener = ::showRemoveShortcutDialog
        binding.triggerShortcutsList.adapter = adapter

        initDragOrdering()
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { viewModel.triggerShortcuts.size > 1 }
        dragOrderingHelper.attachTo(binding.triggerShortcutsList)
        dragOrderingHelper.positionChangeSource
            .concatMapCompletable { (oldPosition, newPosition) ->
                viewModel.changeShortcutPosition(oldPosition, newPosition)
            }
            .subscribe(
                {},
                { e ->
                    logException(e)
                    showSnackbar(R.string.error_generic)
                },
            )
            .attachTo(destroyer)
    }

    private fun openShortcutPickerForAdding() {
        val shortcutIdsInUse = viewModel.triggerShortcuts.value?.map { it.id } ?: emptyList()
        val placeholders = shortcutPlaceholderProvider.placeholders
            .filter { it.id != currentShortcutId && it.id !in shortcutIdsInUse }

        if (placeholders.isEmpty()) {
            DialogBuilder(context)
                .title(R.string.title_add_trigger_shortcut)
                .message(R.string.error_add_trigger_shortcut_no_shortcuts)
                .positive(R.string.dialog_ok)
                .showIfPossible()
            return
        }

        DialogBuilder(context)
            .title(R.string.title_add_trigger_shortcut)
            .mapFor(placeholders) { shortcut ->
                item(name = shortcut.name, shortcutIcon = shortcut.icon) {
                    addShortcut(shortcut.id)
                }
            }
            .showIfPossible()
    }

    private fun addShortcut(shortcutId: String) {
        viewModel.addShortcut(shortcutId)
            .subscribe(
                {},
                { e ->
                    logException(e)
                    showSnackbar(R.string.error_generic)
                },
            )
            .attachTo(destroyer)
    }

    private fun showRemoveShortcutDialog(shortcut: ShortcutPlaceholder) {
        DialogBuilder(context)
            .title(R.string.title_remove_trigger_shortcut)
            .message(getString(R.string.message_remove_trigger_shortcut, shortcut.name))
            .positive(R.string.dialog_remove) {
                removeShortcut(shortcut.id)
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun removeShortcut(shortcutId: String) {
        viewModel.removeShortcut(shortcutId)
            .subscribe(
                {},
                { e ->
                    logException(e)
                    showSnackbar(R.string.error_generic)
                },
            )
            .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, TriggerShortcutsActivity::class.java) {

        fun shortcutId(shortcutId: String?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"
    }

}