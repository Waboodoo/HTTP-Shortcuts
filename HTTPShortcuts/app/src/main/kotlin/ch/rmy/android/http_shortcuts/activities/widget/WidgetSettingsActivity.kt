package ch.rmy.android.http_shortcuts.activities.widget

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityWidgetSettingsBinding
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class WidgetSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityWidgetSettingsBinding

    private val viewModel: WidgetSettingsViewModel by bindViewModel()

    override fun onCreate() {
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
        viewModel.viewState.observe(this) { viewState ->
            binding.widgetLabel.visible = viewState.showLabel
            binding.inputLabelColor.isEnabled = viewState.showLabel
            binding.widgetIcon.setIcon(viewState.shortcutIcon)
            binding.widgetLabel.text = viewState.shortcutName
            binding.inputLabelColor.subtitle = viewState.labelColorFormatted
            binding.widgetLabel.setTextColor(viewState.labelColor)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is WidgetSettingsEvent.ShowLabelColorPicker -> showLabelColorPicker(event.initialColor)
            else -> super.handleEvent(event)
        }
    }

    private fun showLabelColorPicker(initialColor: Int) {
        ColorPickerDialog.Builder(context)
            .setPositiveButton(
                R.string.dialog_ok,
                ColorEnvelopeListener { envelope, fromUser ->
                    if (fromUser) {
                        viewModel.onLabelColorSelected(envelope.color)
                    }
                },
            )
            .setNegativeButton(R.string.dialog_cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .apply {
                colorPickerView.setInitialColor(initialColor)
            }
            .show()
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

    class IntentBuilder : BaseIntentBuilder(WidgetSettingsActivity::class.java) {

        fun shortcut(shortcut: LauncherShortcut) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcut.id)
            intent.putExtra(EXTRA_SHORTCUT_NAME, shortcut.name)
            intent.putExtra(EXTRA_SHORTCUT_ICON, shortcut.icon.toString())
        }
    }

    companion object {
        const val EXTRA_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_id"
        const val EXTRA_SHORTCUT_NAME = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_name"
        const val EXTRA_SHORTCUT_ICON = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_icon"
        const val EXTRA_SHOW_LABEL = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.show_label"
        const val EXTRA_LABEL_COLOR = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.label_color"

        fun getShortcutId(intent: Intent): String? =
            intent.getStringExtra(EXTRA_SHORTCUT_ID)

        fun getLabelColor(intent: Intent): String? =
            intent.getStringExtra(EXTRA_LABEL_COLOR)

        fun shouldShowLabel(intent: Intent): Boolean =
            intent.getBooleanExtra(EXTRA_SHOW_LABEL, true)
    }
}
