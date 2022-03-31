package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.content.ActivityNotFoundException
import android.os.Bundle
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setSubtitle
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.databinding.ActivityAuthenticationBinding
import ch.rmy.android.http_shortcuts.utils.ClientCertUtil
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils

class AuthenticationActivity : BaseActivity() {

    private val openFilePickerForCertificate = registerForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(viewModel::onCertificateFileSelected)
    }

    private val viewModel: AuthenticationViewModel by bindViewModel()
    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityAuthenticationBinding.inflate(layoutInflater))
        setTitle(R.string.section_authentication)
        binding.inputAuthenticationType.setItemsFromPairs(
            AUTHENTICATION_METHODS.map {
                it.first.type to getString(it.second)
            }
        )
    }

    private fun initUserInputBindings() {
        VariableViewUtils.bindVariableViews(binding.inputUsername, binding.variableButtonUsername, variablePlaceholderProvider)
            .attachTo(destroyer)
        VariableViewUtils.bindVariableViews(binding.inputPassword, binding.variableButtonPassword, variablePlaceholderProvider)
            .attachTo(destroyer)
        VariableViewUtils.bindVariableViews(binding.inputToken, binding.variableButtonToken, variablePlaceholderProvider)
            .attachTo(destroyer)

        binding.inputAuthenticationType
            .selectionChanges
            .subscribe {
                viewModel.onAuthenticationTypeChanged(ShortcutAuthenticationType.parse(it))
            }
            .attachTo(destroyer)

        binding.inputUsername.observeTextChanges()
            .subscribe {
                viewModel.onUsernameChanged(binding.inputUsername.rawString)
            }
            .attachTo(destroyer)

        binding.inputPassword.observeTextChanges()
            .subscribe {
                viewModel.onPasswordChanged(binding.inputPassword.rawString)
            }
            .attachTo(destroyer)

        binding.inputToken.observeTextChanges()
            .subscribe {
                viewModel.onTokenChanged(binding.inputToken.rawString)
            }
            .attachTo(destroyer)

        binding.buttonClientCert.setOnClickListener {
            viewModel.onClientCertButtonClicked()
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            binding.containerUsername.visible = viewState.isUsernameAndPasswordVisible
            binding.containerPassword.visible = viewState.isUsernameAndPasswordVisible
            binding.containerToken.visible = viewState.isTokenVisible
            binding.inputAuthenticationType.selectedItem = viewState.authenticationType.type
            binding.inputUsername.rawString = viewState.username
            binding.inputPassword.rawString = viewState.password
            binding.inputToken.rawString = viewState.token
            binding.buttonClientCert.isEnabled = viewState.isClientCertButtonEnabled
            binding.buttonClientCert.setSubtitle(viewState.clientCertSubtitle)
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is AuthenticationEvent.PromptForClientCertAlias -> {
                promptForClientCertAlias()
            }
            is AuthenticationEvent.OpenCertificateFilePicker -> {
                openCertificateFilePicker()
            }
            else -> super.handleEvent(event)
        }
    }

    private fun promptForClientCertAlias() {
        try {
            ClientCertUtil.promptForAlias(this) { alias ->
                viewModel.onClientCertParamsChanged(
                    ClientCertParams.Alias(alias)
                )
            }
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
        }
    }

    private fun openCertificateFilePicker() {
        try {
            openFilePickerForCertificate.launch("application/x-pkcs12")
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(AuthenticationActivity::class.java)

    companion object {

        private val AUTHENTICATION_METHODS = listOf(
            ShortcutAuthenticationType.NONE to R.string.authentication_none,
            ShortcutAuthenticationType.BASIC to R.string.authentication_basic,
            ShortcutAuthenticationType.DIGEST to R.string.authentication_digest,
            ShortcutAuthenticationType.BEARER to R.string.authentication_bearer,
        )
    }
}
