package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeChecked
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setSubtitle
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.extensions.showSnackbar
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.framework.utils.RxUtils
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.databinding.ActivityAdvancedSettingsBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.utils.ClientCertUtil
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import io.reactivex.Single

class AdvancedSettingsActivity : BaseActivity() {

    private val openFilePickerForCertificate = registerForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(::onCertificateFileSelected)
    }

    private val viewModel: AdvancedSettingsViewModel by bindViewModel()
    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityAdvancedSettingsBinding

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

        binding.buttonClientCert.setOnClickListener {
            viewModel.onClientCertButtonClicked()
        }

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
            binding.buttonClientCert.isEnabled = viewState.isClientCertButtonEnabled
            binding.buttonClientCert.setSubtitle(viewState.clientCertSubtitle)
            binding.inputAcceptCookies.isChecked = viewState.acceptCookies
            binding.inputTimeout.setSubtitle(viewState.timeoutSubtitle)
            binding.inputProxyHost.rawString = viewState.proxyHost
            binding.inputProxyPort.setTextSafely(viewState.proxyPort)
            binding.inputSsid.setTextSafely(viewState.wifiSsid)
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is AdvancedSettingsEvent.PromptForClientCertAlias -> {
                promptForClientCertAlias()
            }
            is AdvancedSettingsEvent.OpenCertificateFilePicker -> {
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

    private fun onCertificateFileSelected(file: Uri) {
        copyCertificateFile(file)
            .flatMap { fileName ->
                promptForPassword()
                    .map { password ->
                        ClientCertParams.File(fileName, password)
                    }
            }
            .subscribe(
                viewModel::onClientCertParamsChanged,
            ) { e ->
                logException(e)
                showSnackbar(R.string.error_generic)
            }
            .attachTo(destroyer)
    }

    private fun copyCertificateFile(file: Uri): Single<String> =
        RxUtils.single {
            val fileName = "${newUUID()}.p12"
            contentResolver.openInputStream(file)!!.use { inputStream ->
                context.openFileOutput(fileName, MODE_PRIVATE).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            fileName
        }

    private fun promptForPassword(): Single<String> =
        Single.create { emitter ->
            DialogBuilder(context)
                .title(R.string.title_client_cert_file_password)
                .textInput(
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                ) { input ->
                    emitter.onSuccess(input)
                }
                .dismissListener {
                    emitter.cancel()
                }
                .showIfPossible()
        }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(AdvancedSettingsActivity::class.java)
}
