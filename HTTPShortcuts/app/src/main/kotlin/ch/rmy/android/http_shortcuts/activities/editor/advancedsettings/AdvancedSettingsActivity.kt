package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.observeChecked
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.ClientCertUtil
import ch.rmy.android.http_shortcuts.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.utils.RxUtils
import ch.rmy.android.http_shortcuts.utils.SimpleOnSeekBarChangeListener
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import ch.rmy.android.http_shortcuts.views.PanelButton
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
import java.util.concurrent.TimeUnit

class AdvancedSettingsActivity : BaseActivity() {

    private val viewModel: AdvancedSettingsViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    private val waitForConnectionCheckBox: CheckBox by bindView(R.id.input_wait_for_connection)
    private val followRedirectsCheckBox: CheckBox by bindView(R.id.input_follow_redirects)
    private val acceptCertificatesCheckBox: CheckBox by bindView(R.id.input_accept_certificates)
    private val clientCertButton: PanelButton by bindView(R.id.button_client_cert)
    private val acceptCookiesCheckBox: CheckBox by bindView(R.id.input_accept_cookies)
    private val timeoutView: PanelButton by bindView(R.id.input_timeout)
    private val proxyHostView: VariableEditText by bindView(R.id.input_proxy_host)
    private val proxyHostVariableButton: VariableButton by bindView(R.id.variable_button_proxy_host)
    private val proxyPortView: EditText by bindView(R.id.input_proxy_port)
    private val ssidView: EditText by bindView(R.id.input_ssid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_settings)
        setTitle(R.string.label_advanced_technical_settings)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        waitForConnectionCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setWaitForConnection(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        followRedirectsCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setFollowRedirects(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        acceptCertificatesCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setAcceptAllCertificates(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        acceptCookiesCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setAcceptCookies(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)

        clientCertButton.setOnClickListener {
            onClientCertButtonClicked()
        }

        timeoutView.setOnClickListener {
            showTimeoutDialog()
        }

        VariableViewUtils.bindVariableViews(proxyHostView, proxyHostVariableButton, variablePlaceholderProvider)
            .attachTo(destroyer)
    }

    // TODO: This gets hackier and hackier. Let's refactor this, maybe let's use MVVI
    private var viewStatesInitialized = false

    private fun bindViewsToViewModel() {
        shortcutData.observe(this) {
            val shortcut = shortcutData.value ?: return@observe
            updateShortcutViews(shortcut, !viewStatesInitialized)
            viewStatesInitialized = true
        }
        bindTextChangeListener(proxyHostView) { shortcutData.value?.proxyHost ?: "" }
        bindTextChangeListener(proxyPortView) { shortcutData.value?.proxyPort?.toString() ?: "" }
        bindTextChangeListener(ssidView) { shortcutData.value?.wifiSsid ?: "" }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it.toString() != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setAdvancedSettings(proxyHostView.rawString, proxyPortView.text.toString().toIntOrNull(), ssidView.text.toString())

    private fun updateShortcutViews(shortcut: Shortcut, isInitial: Boolean) {
        waitForConnectionCheckBox.isChecked = shortcut.isWaitForNetwork
        followRedirectsCheckBox.isChecked = shortcut.followRedirects
        acceptCertificatesCheckBox.isChecked = shortcut.acceptAllCertificates
        clientCertButton.isEnabled = !shortcut.acceptAllCertificates
        clientCertButton.subtitle = viewModel.getClientCertSubtitle(shortcut)
        acceptCookiesCheckBox.isChecked = shortcut.acceptCookies
        timeoutView.subtitle = viewModel.getTimeoutSubtitle(shortcut)
        if (isInitial) {
            proxyHostView.rawString = shortcut.proxyHost ?: ""
            proxyPortView.setText(shortcut.proxyPort?.toString() ?: "")
            ssidView.setText(shortcut.wifiSsid)
        }
    }

    private fun onClientCertButtonClicked() {
        val shortcut = shortcutData.value ?: return
        if (shortcut.clientCertParams == null) {
            openClientCertDialog()
        } else {
            setClientCertParams(null)
        }
    }

    private fun openClientCertDialog() {
        DialogBuilder(context)
            .title(R.string.title_client_cert)
            .item(R.string.label_client_cert_from_os, descriptionRes = R.string.label_client_cert_from_os_subtitle) {
                promptForClientCertAlias()
            }
            .item(R.string.label_client_cert_from_file, descriptionRes = R.string.label_client_cert_from_file_subtitle) {
                openCertificateFilePicker()
            }
            .showIfPossible()
    }

    private fun promptForClientCertAlias() {
        ClientCertUtil.promptForAlias(this) { alias ->
            setClientCertParams(ClientCertParams.Alias(alias))
        }
    }

    private fun openCertificateFilePicker() {
        try {
            FilePickerUtil.createIntent(type = "application/x-pkcs12")
                .startActivity(this, REQUEST_SELECT_CERTIFICATE_FILE)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
        }
    }

    private fun setClientCertParams(clientCertParams: ClientCertParams?) {
        viewModel.setClientCertParams(clientCertParams)
            .subscribe()
            .attachTo(destroyer)
    }

    private fun showTimeoutDialog() {
        // TODO: Move this out into its own class
        val shortcut = shortcutData.value ?: return
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)

        val slider = view.findViewById<SeekBar>(R.id.slider)
        val label = view.findViewById<TextView>(R.id.slider_value)

        slider.max = TIMEOUT_OPTIONS.lastIndex

        slider.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                label.text = viewModel.getTimeoutText(progressToTimeout(progress))
            }
        })
        label.text = viewModel.getTimeoutText(shortcut.timeout)
        slider.progress = timeoutToProgress(shortcut.timeout)

        DialogBuilder(context)
            .title(R.string.label_timeout)
            .view(view)
            .positive(R.string.dialog_ok) {
                viewModel.setTimeout(progressToTimeout(slider.progress))
                    .subscribe()
                    .attachTo(destroyer)
            }
            .showIfPossible()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != RESULT_OK || intent == null) {
            return
        }
        when (requestCode) {
            REQUEST_SELECT_CERTIFICATE_FILE -> {
                onCertificateFileSelected(intent.data ?: return)
            }
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
                ::setClientCertParams,
            ) { e ->
                logException(e)
                showSnackbar(R.string.error_generic)
            }
            .attachTo(destroyer)
    }

    private fun copyCertificateFile(file: Uri): Single<String> =
        RxUtils.single {
            val fileName = "${UUIDUtils.newUUID()}.p12"
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

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, AdvancedSettingsActivity::class.java)

    companion object {

        private const val REQUEST_SELECT_CERTIFICATE_FILE = 1

        private val TIMEOUT_OPTIONS = arrayOf(
            500,
            1000,
            2000,
            3000,
            5000,
            8000,
            10000,
            15000,
            20000,
            25000,
            30000,
            45000,
            60000,
            90000,
            120000,
            180000,
            300000,
            450000,
            600000,
        )


        private fun timeoutToProgress(timeout: Int) = TIMEOUT_OPTIONS.indexOfFirst {
            it >= timeout
        }
            .takeUnless { it == -1 }
            ?: TIMEOUT_OPTIONS.lastIndex

        private fun progressToTimeout(progress: Int) = TIMEOUT_OPTIONS[progress]

    }

}