package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.os.Bundle
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeChecked
import ch.rmy.android.framework.extensions.setSubtitle
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityExecutionSettingsBinding

class ExecutionSettingsActivity : BaseActivity() {

    private val viewModel: ExecutionSettingsViewModel by bindViewModel()

    private lateinit var binding: ActivityExecutionSettingsBinding

    override fun onCreated(savedState: Bundle?) {
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
            binding.inputLauncherShortcut.isVisible = viewState.launcherShortcutOptionVisible
            binding.inputLauncherShortcut.isChecked = viewState.launcherShortcut
            binding.inputQuickTileShortcut.isVisible = viewState.quickSettingsTileShortcutOptionVisible
            binding.inputQuickTileShortcut.isChecked = viewState.quickSettingsTileShortcut
            binding.inputWaitForConnection.isChecked = viewState.waitForConnection
            binding.inputDelay.setSubtitle(viewState.delaySubtitle)
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(ExecutionSettingsActivity::class.java)
}
