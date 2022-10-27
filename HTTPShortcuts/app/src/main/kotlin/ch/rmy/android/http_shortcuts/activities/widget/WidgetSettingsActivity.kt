package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityWidgetSettingsBinding
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

class WidgetSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityWidgetSettingsBinding

    private val viewModel: WidgetSettingsViewModel by bindViewModel()

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(
            WidgetSettingsViewModel.InitData(
                shortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID)!!,
                shortcutName = intent.getStringExtra(EXTRA_SHORTCUT_NAME)!!,
                shortcutIcon = ShortcutIcon.fromName(intent.getStringExtra(EXTRA_SHORTCUT_ICON)!!),
            ),
        )
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityWidgetSettingsBinding.inflate(layoutInflater))
        setTitle(R.string.title_configure_widget)
    }

    private fun initUserInputBindings() {
        binding.inputLabelColor.setOnClickListener {
            viewModel.onLabelColorInputClicked()
        }

        binding.inputShowLabel.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onShowLabelChanged(isChecked)
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.widgetLabel.isVisible = viewState.showLabel
            binding.inputLabelColor.isEnabled = viewState.showLabel
            binding.widgetIcon.setIcon(viewState.shortcutIcon)
            binding.widgetLabel.text = viewState.shortcutName
            binding.inputLabelColor.subtitle = viewState.labelColorFormatted
            binding.widgetLabel.setTextColor(viewState.labelColor)
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.widget_settings_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create_widget -> consume { viewModel.onCreateButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    object OpenWidgetSettings : BaseActivityResultContract<IntentBuilder, OpenWidgetSettings.Result?>(::IntentBuilder) {

        private const val EXTRA_SHOW_LABEL = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.show_label"
        private const val EXTRA_LABEL_COLOR = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.label_color"

        override fun parseResult(resultCode: Int, intent: Intent?): Result? =
            if (resultCode == Activity.RESULT_OK && intent != null) {
                Result(
                    shortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID)!!,
                    labelColor = intent.getStringExtra(EXTRA_LABEL_COLOR)!!,
                    showLabel = intent.getBooleanExtra(EXTRA_SHOW_LABEL, true),
                )
            } else null

        fun createResult(shortcutId: ShortcutId, labelColor: String, showLabel: Boolean): Intent =
            createIntent {
                putExtra(EXTRA_SHORTCUT_ID, shortcutId)
                putExtra(EXTRA_LABEL_COLOR, labelColor)
                putExtra(EXTRA_SHOW_LABEL, showLabel)
            }

        data class Result(
            val shortcutId: ShortcutId,
            val labelColor: String,
            val showLabel: Boolean,
        )
    }

    class IntentBuilder : BaseIntentBuilder(WidgetSettingsActivity::class) {

        fun shortcut(shortcut: LauncherShortcut) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcut.id)
            intent.putExtra(EXTRA_SHORTCUT_NAME, shortcut.name)
            intent.putExtra(EXTRA_SHORTCUT_ICON, shortcut.icon.toString())
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_id"
        private const val EXTRA_SHORTCUT_NAME = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_name"
        private const val EXTRA_SHORTCUT_ICON = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_icon"
    }
}
