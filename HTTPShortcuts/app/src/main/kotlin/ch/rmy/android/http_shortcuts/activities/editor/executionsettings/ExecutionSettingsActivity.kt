package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.TextView
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeChecked
import ch.rmy.android.framework.extensions.setSubtitle
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.SimpleOnSeekBarChangeListener
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityExecutionSettingsBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
class ExecutionSettingsActivity : BaseActivity() {

    private val viewModel: ExecutionSettingsViewModel by bindViewModel()

    private lateinit var binding: ActivityExecutionSettingsBinding

    override fun onCreate() {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityExecutionSettingsBinding.inflate(layoutInflater))
        setTitle(R.string.label_execution_settings)
    }

    private fun initUserInputBindings() {
        binding.inputRequireConfirmation
            .observeChecked()
            .subscribe(viewModel::onRequireConfirmationChanged)
            .attachTo(destroyer)
        binding.inputLauncherShortcut
            .observeChecked()
            .subscribe(viewModel::onLauncherShortcutChanged)
            .attachTo(destroyer)
        binding.inputQuickTileShortcut
            .observeChecked()
            .subscribe(viewModel::onQuickSettingsTileShortcutChanged)
            .attachTo(destroyer)
        binding.inputWaitForConnection
            .observeChecked()
            .subscribe(viewModel::onWaitForConnectionChanged)
            .attachTo(destroyer)
        binding.inputDelay.setOnClickListener {
            viewModel.onDelayButtonClicked()
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.inputRequireConfirmation.isChecked = viewState.requireConfirmation
            binding.inputLauncherShortcut.visible = viewState.launcherShortcutOptionVisible
            binding.inputLauncherShortcut.isChecked = viewState.launcherShortcut
            binding.inputQuickTileShortcut.visible = viewState.quickSettingsTileShortcutOptionVisible
            binding.inputQuickTileShortcut.isChecked = viewState.quickSettingsTileShortcut
            binding.inputWaitForConnection.isChecked = viewState.waitForConnection
            binding.inputDelay.setSubtitle(viewState.delaySubtitle)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ExecutionSettingsEvent.ShowDelayDialog -> {
                showDelayDialog(event.delay, event.getLabel)
            }
            else -> super.handleEvent(event)
        }
    }

    // TODO: Move this out into its own class?
    private fun showDelayDialog(delay: Duration, getDelayText: (Duration) -> Localizable) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)

        val slider = view.findViewById<SeekBar>(R.id.slider)
        val label = view.findViewById<TextView>(R.id.slider_value)

        slider.max = DELAY_OPTIONS.lastIndex

        slider.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                label.text = getDelayText(progressToDelay(progress)).localize(context)
            }
        })
        label.text = getDelayText(delay).localize(context)
        slider.progress = delayToProgress(delay)

        DialogBuilder(context)
            .title(R.string.label_delay_execution)
            .view(view)
            .positive(R.string.dialog_ok) {
                viewModel.onDelayChanged(progressToDelay(slider.progress))
            }
            .showIfPossible()
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(ExecutionSettingsActivity::class.java)

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
