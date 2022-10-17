package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.os.Bundle
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.doOnCheckedChanged
import ch.rmy.android.framework.extensions.doOnTextChanged
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.setSubtitle
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.databinding.ActivityAdvancedSettingsBinding

class AdvancedSettingsActivity : BaseActivity() {

    private val viewModel: AdvancedSettingsViewModel by bindViewModel()

    private lateinit var binding: ActivityAdvancedSettingsBinding

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityAdvancedSettingsBinding.inflate(layoutInflater))
        setTitle(R.string.label_advanced_technical_settings)
    }

    private fun initUserInputBindings() {
        binding.inputFollowRedirects.doOnCheckedChanged(viewModel::onFollowRedirectsChanged)
        binding.inputAcceptCertificates.doOnCheckedChanged(viewModel::onAcceptAllCertificatesChanged)
        binding.inputAcceptCookies.doOnCheckedChanged(viewModel::onAcceptCookiesChanged)

        binding.inputProxyHost.doOnTextChanged {
            viewModel.onProxyHostChanged(binding.inputProxyHost.rawString)
        }
        binding.inputProxyPort.doOnTextChanged {
            viewModel.onProxyPortChanged(it.toString().toIntOrNull())
        }
        binding.inputSsid.doOnTextChanged {
            viewModel.onWifiSsidChanged(it.toString())
        }

        binding.inputTimeout.setOnClickListener {
            viewModel.onTimeoutButtonClicked()
        }
        binding.variableButtonProxyHost.setOnClickListener {
            viewModel.onProxyHostVariableButtonClicked()
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.inputFollowRedirects.isChecked = viewState.followRedirects
            binding.inputAcceptCertificates.isChecked = viewState.acceptAllCertificates
            binding.inputAcceptCookies.isChecked = viewState.acceptCookies
            binding.inputTimeout.setSubtitle(viewState.timeoutSubtitle)
            binding.inputProxyHost.rawString = viewState.proxyHost
            binding.inputProxyPort.setTextSafely(viewState.proxyPort)
            binding.inputSsid.setTextSafely(viewState.wifiSsid)
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is AdvancedSettingsEvent.InsertVariablePlaceholder -> {
                binding.inputProxyHost.insertVariablePlaceholder(event.variablePlaceholder)
            }
            else -> super.handleEvent(event)
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(AdvancedSettingsActivity::class)
}
