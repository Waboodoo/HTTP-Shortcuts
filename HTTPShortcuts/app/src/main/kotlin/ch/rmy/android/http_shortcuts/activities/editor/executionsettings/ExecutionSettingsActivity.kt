package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityExecutionSettingsBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeChecked
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.tiles.QuickSettingsTileManager
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.SimpleOnSeekBarChangeListener
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ExecutionSettingsActivity : BaseActivity() {

    private val viewModel: ExecutionSettingsViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }

    private lateinit var binding: ActivityExecutionSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityExecutionSettingsBinding.inflate(layoutInflater))
        setTitle(R.string.label_execution_settings)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        binding.inputRequireConfirmation
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setRequireConfirmation(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        binding.inputLauncherShortcut
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setLauncherShortcut(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        binding.inputQuickTileShortcut
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setQuickSettingsTileShortcut(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        binding.inputWaitForConnection
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setWaitForConnection(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        binding.inputDelay.setOnClickListener {
            showDelayDialog()
        }
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this) {
            updateShortcutViews()
        }
    }

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        binding.inputRequireConfirmation.isChecked = shortcut.requireConfirmation
        binding.inputLauncherShortcut.visible = LauncherShortcutManager.supportsLauncherShortcuts()
        binding.inputLauncherShortcut.isChecked = shortcut.launcherShortcut
        binding.inputQuickTileShortcut.visible = QuickSettingsTileManager.supportsQuickSettingsTiles()
        binding.inputQuickTileShortcut.isChecked = shortcut.quickSettingsTileShortcut
        binding.inputWaitForConnection.isChecked = shortcut.isWaitForNetwork
        binding.inputDelay.subtitle = viewModel.getDelaySubtitle(shortcut)
    }

    private fun showDelayDialog() {
        // TODO: Move this out into its own class
        val shortcut = shortcutData.value ?: return
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)

        val slider = view.findViewById<SeekBar>(R.id.slider)
        val label = view.findViewById<TextView>(R.id.slider_value)

        slider.max = DELAY_OPTIONS.lastIndex

        slider.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                label.text = viewModel.getDelayText(progressToDelay(progress))
            }
        })
        label.text = viewModel.getDelayText(shortcut.delay.milliseconds)
        slider.progress = delayToProgress(shortcut.delay.milliseconds)

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

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ExecutionSettingsActivity::class.java)

    companion object {

        private val DELAY_OPTIONS = arrayOf(
            0.milliseconds,
            500.milliseconds,
            1.seconds,
            2.seconds,
            3.seconds,
            5.seconds,
            8.seconds,
            10.seconds,
            15.seconds,
            20.seconds,
            25.seconds,
            30.seconds,
            45.seconds,
            1.minutes,
            90.seconds,
            2.minutes,
            3.minutes,
            5.minutes,
            450.seconds,
            10.minutes,
            15.minutes,
            20.minutes,
            30.minutes,
            1.hours,
        )

        private fun delayToProgress(delay: Duration) = DELAY_OPTIONS
            .indexOfFirst {
                it >= delay
            }
            .takeUnless { it == -1 }
            ?: DELAY_OPTIONS.lastIndex

        private fun progressToDelay(progress: Int) = DELAY_OPTIONS[progress]
    }
}
