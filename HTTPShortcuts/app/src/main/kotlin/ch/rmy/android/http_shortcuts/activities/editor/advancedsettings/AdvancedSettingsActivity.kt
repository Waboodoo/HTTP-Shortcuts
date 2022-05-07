package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.os.Bundle
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeChecked
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setSubtitle
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.databinding.ActivityAdvancedSettingsBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import javax.inject.Inject

class AdvancedSettingsActivity : BaseActivity() {

    @Inject
    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

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
        binding.inputFollowRedirects
            .observeChecked()
            .subscribe(viewModel::onFollowRedirectsChanged)
            .attachTo(destroyer)
        binding.inputAcceptCertificates
            .observeChecked()
            .subscribe(viewModel::onAcceptAllCertificatesChanged)
            .attachTo(destroyer)
        binding.inputAcceptCookies
            .observeChecked()
            .subscribe(viewModel::onAcceptCookiesChanged)
            .attachTo(destroyer)

        binding.inputProxyHost.observeTextChanges()
            .subscribe {
                viewModel.onProxyHostChanged(binding.inputProxyHost.rawString)
            }
            .attachTo(destroyer)
        binding.inputProxyPort.observeTextChanges()
            .subscribe {
                viewModel.onProxyPortChanged(it.toString().toIntOrNull())
            }
            .attachTo(destroyer)
        binding.inputSsid.observeTextChanges()
            .subscribe {
                viewModel.onWifiSsidChanged(it.toString())
            }
            .attachTo(destroyer)

        binding.inputTimeout.setOnClickListener {
            viewModel.onTimeoutButtonClicked()
        }

        VariableViewUtils.bindVariableViews(binding.inputProxyHost, binding.variableButtonProxyHost, variablePlaceholderProvider)
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            binding.inputFollowRedirects.isChecked = viewState.followRedirects
            binding.inputAcceptCertificates.isChecked = viewState.acceptAllCertificates
            binding.inputAcceptCookies.isChecked = viewState.acceptCookies
            binding.inputTimeout.setSubtitle(viewState.timeoutSubtitle)
            binding.inputProxyHost.rawString = viewState.proxyHost
            binding.inputProxyPort.setTextSafely(viewState.proxyPort)
            binding.inputSsid.setTextSafely(viewState.wifiSsid)
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(AdvancedSettingsActivity::class.java)
}
