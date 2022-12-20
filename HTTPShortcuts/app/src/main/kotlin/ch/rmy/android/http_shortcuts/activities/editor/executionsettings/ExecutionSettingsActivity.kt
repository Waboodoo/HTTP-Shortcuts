package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.os.Bundle
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.doOnCheckedChanged
import ch.rmy.android.framework.extensions.initialize
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
        binding.inputRequireConfirmation.doOnCheckedChanged(viewModel::onRequireConfirmationChanged)
        binding.inputLauncherShortcut.doOnCheckedChanged(viewModel::onLauncherShortcutChanged)
        binding.inputSecondaryLauncherShortcut.doOnCheckedChanged(viewModel::onSecondaryLauncherShortcutChanged)
        binding.inputQuickTileShortcut.doOnCheckedChanged(viewModel::onQuickSettingsTileShortcutChanged)
        binding.inputWaitForConnection.doOnCheckedChanged(viewModel::onWaitForConnectionChanged)
        binding.inputExcludeFromHistory.doOnCheckedChanged(viewModel::onExcludeFromHistoryChanged)
        binding.inputDelay.setOnClickListener {
            viewModel.onDelayButtonClicked()
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.inputRequireConfirmation.isChecked = viewState.requireConfirmation
            binding.inputLauncherShortcut.isVisible = viewState.launcherShortcutOptionVisible
            binding.inputLauncherShortcut.isChecked = viewState.launcherShortcut
            binding.inputSecondaryLauncherShortcut.isChecked = viewState.secondaryLauncherShortcut
            binding.inputQuickTileShortcut.isVisible = viewState.quickSettingsTileShortcutOptionVisible
            binding.inputQuickTileShortcut.isChecked = viewState.quickSettingsTileShortcut
            binding.inputWaitForConnection.isVisible = viewState.waitForConnectionOptionVisible
            binding.inputWaitForConnection.isChecked = viewState.waitForConnection
            binding.inputExcludeFromHistory.isChecked = viewState.excludeFromHistory
            binding.inputDelay.setSubtitle(viewState.delaySubtitle)
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(ExecutionSettingsActivity::class)
}
