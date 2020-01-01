package ch.rmy.android.http_shortcuts.activities.editor.miscsettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeChecked
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.SimpleOnSeekBarChangeListener
import ch.rmy.android.http_shortcuts.views.PanelButton
import kotterknife.bindView

class MiscSettingsActivity : BaseActivity() {

    private val viewModel: MiscSettingsViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }

    private val requireConfirmationCheckBox: CheckBox by bindView(R.id.input_require_confirmation)
    private val launcherShortcutCheckBox: CheckBox by bindView(R.id.input_launcher_shortcut)
    private val delayView: PanelButton by bindView(R.id.input_delay)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_misc_settings)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        requireConfirmationCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setRequireConfirmation(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        launcherShortcutCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setLauncherShortcut(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        delayView.setOnClickListener {
            showDelayDialog()
        }
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
        })
    }

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        requireConfirmationCheckBox.isChecked = shortcut.requireConfirmation
        launcherShortcutCheckBox.isVisible = LauncherShortcutManager.supportsLauncherShortcuts()
        launcherShortcutCheckBox.isChecked = shortcut.launcherShortcut
        delayView.subtitle = viewModel.getDelaySubtitle(shortcut)
    }

    private fun showDelayDialog() {
        // TODO: Move this out into its own class
        val shortcut = shortcutData.value ?: return
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)

        val slider = view.findViewById<SeekBar>(R.id.slider)
        val label = view.findViewById<TextView>(R.id.slider_value)

        slider.max = DELAY_MAX

        slider.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                label.text = viewModel.getDelayText(progressToDelay(progress))
            }
        })
        label.text = viewModel.getDelayText(shortcut.delay)
        slider.progress = delayToProgress(shortcut.delay)

        DialogBuilder(context)
            .title(R.string.label_delay_execution)
            .view(view)
            .positive(R.string.dialog_ok) {
                viewModel.setDelay(progressToDelay(slider.progress))
                    .subscribe()
                    .attachTo(destroyer)
            }
            .showIfPossible()
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, MiscSettingsActivity::class.java)

    companion object {

        private const val DELAY_MAX = 10 * 60

        private fun delayToProgress(delay: Int) = delay / 1000

        private fun progressToDelay(progress: Int) = progress * 1000

    }

}