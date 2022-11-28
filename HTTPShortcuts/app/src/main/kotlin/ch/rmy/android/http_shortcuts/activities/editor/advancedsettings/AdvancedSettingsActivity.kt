package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
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
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.databinding.ActivityAdvancedSettingsBinding
import kotlinx.coroutines.launch

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
        binding.inputProxyType.setItemsFromPairs(
            PROXY_TYPES.map {
                it.first.type to it.second
            }
        )
    }

    private fun initUserInputBindings() {
        binding.inputFollowRedirects.doOnCheckedChanged(viewModel::onFollowRedirectsChanged)
        binding.inputAcceptCertificates.doOnCheckedChanged(viewModel::onAcceptAllCertificatesChanged)
        binding.inputAcceptCookies.doOnCheckedChanged(viewModel::onAcceptCookiesChanged)

        lifecycleScope.launch {
            binding.inputProxyType.selectionChanges.collect {
                viewModel.onProxyTypeChanged(ProxyType.parse(it))
            }
        }
        binding.inputProxyHost.doOnTextChanged {
            viewModel.onProxyHostChanged(binding.inputProxyHost.rawString)
        }
        binding.inputProxyPort.doOnTextChanged {
            viewModel.onProxyPortChanged(it.toString().toIntOrNull())
        }
        binding.inputProxyUsername.doOnTextChanged {
            viewModel.onProxyUsernameChanged(binding.inputProxyUsername.rawString)
        }
        binding.inputProxyPassword.doOnTextChanged {
            viewModel.onProxyPasswordChanged(binding.inputProxyPassword.rawString)
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
        binding.variableButtonProxyUsername.setOnClickListener {
            viewModel.onProxyUsernameVariableButtonClicked()
        }
        binding.variableButtonProxyPassword.setOnClickListener {
            viewModel.onProxyPasswordVariableButtonClicked()
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.inputFollowRedirects.isChecked = viewState.followRedirects
            binding.inputAcceptCertificates.isChecked = viewState.acceptAllCertificates
            binding.inputAcceptCookies.isChecked = viewState.acceptCookies
            binding.inputTimeout.setSubtitle(viewState.timeoutSubtitle)
            binding.inputProxyType.selectedItem = viewState.proxyType.type
            binding.inputProxyHost.rawString = viewState.proxyHost
            binding.inputProxyPort.setTextSafely(viewState.proxyPort)
            binding.inputProxyUsername.rawString = viewState.proxyUsername
            binding.inputProxyPassword.rawString = viewState.proxyPassword
            binding.inputSsid.setTextSafely(viewState.wifiSsid)
            binding.proxyUsernameContainer.isVisible = viewState.usernameAndPasswordVisible
            binding.proxyPasswordContainer.isVisible = viewState.usernameAndPasswordVisible
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is AdvancedSettingsEvent.InsertVariablePlaceholderIntoProxyHost -> {
                binding.inputProxyHost.insertVariablePlaceholder(event.variablePlaceholder)
            }
            is AdvancedSettingsEvent.InsertVariablePlaceholderIntoProxyUsername -> {
                binding.inputProxyUsername.insertVariablePlaceholder(event.variablePlaceholder)
            }
            is AdvancedSettingsEvent.InsertVariablePlaceholderIntoProxyPassword -> {
                binding.inputProxyPassword.insertVariablePlaceholder(event.variablePlaceholder)
            }
            else -> super.handleEvent(event)
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(AdvancedSettingsActivity::class)

    companion object {
        private val PROXY_TYPES = listOf(
            ProxyType.HTTP to "HTTP",
            ProxyType.SOCKS to "SOCKS",
        )
    }
}
