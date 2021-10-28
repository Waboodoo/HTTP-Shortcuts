package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityWidgetSettingsBinding
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class WidgetSettingsActivity : BaseActivity() {

    private val shortcutId: String by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)!!
    }
    private val shortcutName: String by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_NAME)!!
    }
    private val shortcutIcon: ShortcutIcon by lazy {
        ShortcutIcon.fromName(intent.getStringExtra(EXTRA_SHORTCUT_ICON)!!)
    }

    private lateinit var binding: ActivityWidgetSettingsBinding

    private val viewModel: WidgetSettingsViewModel by bindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityWidgetSettingsBinding.inflate(layoutInflater))
        setTitle(R.string.title_configure_widget)
        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        binding.widgetIcon.setIcon(shortcutIcon)
        binding.widgetLabel.text = shortcutName
        updateLabelColor()
        binding.inputLabelColor.setOnClickListener {
            showColorPicker()
        }
    }

    private fun updateLabelColor() {
        binding.inputLabelColor.subtitle = viewModel.labelColorFormatted
        binding.widgetLabel.setTextColor(viewModel.labelColor.value!!)
    }

    private fun bindViewsToViewModel() {
        viewModel.showLabel.observe(this) {
            binding.widgetLabel.visible = it
            binding.inputLabelColor.isEnabled = it
        }
        viewModel.labelColor.observe(this) {
            updateLabelColor()
        }
        binding.inputShowLabel.setOnCheckedChangeListener { _, _ ->
            viewModel.showLabel.value = binding.inputShowLabel.isChecked
        }
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(context)
            .setPositiveButton(R.string.dialog_ok, ColorEnvelopeListener { envelope, fromUser ->
                if (fromUser) {
                    viewModel.labelColor.value = envelope.color
                }
            })
            .setNegativeButton(R.string.dialog_cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .apply {
                colorPickerView.setInitialColor(viewModel.labelColor.value!!)
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.widget_settings_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create_widget -> consume { onDone() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun onDone() {
        setResult(Activity.RESULT_OK, Intent()
            .putExtra(EXTRA_SHORTCUT_ID, shortcutId)
            .putExtra(EXTRA_SHOW_LABEL, viewModel.showLabel.value)
            .putExtra(EXTRA_LABEL_COLOR, viewModel.labelColorFormatted)
        )
        finish()
    }

    override val navigateUpIcon = R.drawable.ic_clear

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, WidgetSettingsActivity::class.java) {

        fun shortcut(shortcut: Shortcut) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcut.id)
            intent.putExtra(EXTRA_SHORTCUT_NAME, shortcut.name)
            intent.putExtra(EXTRA_SHORTCUT_ICON, shortcut.icon.toString())
        }

    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_id"
        private const val EXTRA_SHORTCUT_NAME = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_name"
        private const val EXTRA_SHORTCUT_ICON = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.shortcut_icon"
        private const val EXTRA_SHOW_LABEL = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.show_label"
        private const val EXTRA_LABEL_COLOR = "ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity.label_color"

        fun getShortcutId(intent: Intent): String? =
            intent.getStringExtra(EXTRA_SHORTCUT_ID)

        fun getLabelColor(intent: Intent): String? =
            intent.getStringExtra(EXTRA_LABEL_COLOR)

        fun shouldShowLabel(intent: Intent): Boolean =
            intent.getBooleanExtra(EXTRA_SHOW_LABEL, true)

    }

}