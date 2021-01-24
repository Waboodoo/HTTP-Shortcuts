package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.views.PanelButton
import kotterknife.bindView
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener

class WidgetSettingsActivity : BaseActivity() {

    private val shortcutId: String by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)!!
    }
    private val shortcutName: String by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_NAME)!!
    }
    private val shortcutIcon: String by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ICON)!!
    }

    private val iconView: IconView by bindView(R.id.widget_icon)
    private val labelView: TextView by bindView(R.id.widget_label)
    private val showLabelCheckbox: CheckBox by bindView(R.id.input_show_label)
    private val labelColorView: PanelButton by bindView(R.id.input_label_color)

    private val viewModel: WidgetSettingsViewModel by bindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_settings)
        setTitle(R.string.title_configure_widget)
        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        iconView.setIcon(shortcutIcon)
        labelView.text = shortcutName
        updateLabelColor()
        labelColorView.setOnClickListener {
            showColorPicker()
        }
    }

    private fun updateLabelColor() {
        labelColorView.subtitle = viewModel.labelColorFormatted
        labelView.setTextColor(viewModel.labelColor.value!!)
    }

    private fun bindViewsToViewModel() {
        viewModel.showLabel.observe(this) {
            labelView.visible = it
            labelColorView.isEnabled = it
        }
        viewModel.labelColor.observe(this) {
            updateLabelColor()
        }
        showLabelCheckbox.setOnCheckedChangeListener { _, _ ->
            viewModel.showLabel.value = showLabelCheckbox.isChecked
        }
    }

    private fun showColorPicker() {
        ChromaDialog.Builder()
            .initialColor(viewModel.labelColor.value!!)
            .colorMode(ColorMode.RGB)
            .onColorSelected(object : ColorSelectListener {
                override fun onColorSelected(color: Int) {
                    viewModel.labelColor.value = color
                }
            })
            .create()
            .show(supportFragmentManager, "ChromaDialog")
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
            intent.putExtra(EXTRA_SHORTCUT_ICON, shortcut.iconName)
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