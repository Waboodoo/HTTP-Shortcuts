package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityAdvancedSettingsBinding
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
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    private lateinit var binding: ActivityAdvancedSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityAdvancedSettingsBinding.inflate(layoutInflater))
        setTitle(R.string.label_advanced_technical_settings)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        binding.inputFollowRedirects
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setFollowRedirects(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        binding.inputAcceptCertificates
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setAcceptAllCertificates(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        binding.inputAcceptCookies
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setAcceptCookies(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)

        binding.buttonClientCert.setOnClickListener {
            onClientCertButtonClicked()
        }

        binding.inputTimeout.setOnClickListener {
            showTimeoutDialog()
        }

        VariableViewUtils.bindVariableViews(binding.inputProxyHost, binding.variableButtonProxyHost, variablePlaceholderProvider)
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
        bindTextChangeListener(binding.inputProxyHost) { shortcutData.value?.proxyHost ?: "" }
        bindTextChangeListener(binding.inputProxyPort) { shortcutData.value?.proxyPort?.toString() ?: "" }
        bindTextChangeListener(binding.inputSsid) { shortcutData.value?.wifiSsid ?: "" }
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
        viewModel.setAdvancedSettings(
            binding.inputProxyHost.rawString,
            binding.inputProxyPort.text.toString().toIntOrNull(),
            binding.inputSsid.text.toString(),
        )

    private fun updateShortcutViews(shortcut: Shortcut, isInitial: Boolean) {
        binding.inputFollowRedirects.isChecked = shortcut.followRedirects
        binding.inputAcceptCertificates.isChecked = shortcut.acceptAllCertificates
        binding.buttonClientCert.isEnabled = !shortcut.acceptAllCertificates
        binding.buttonClientCert.subtitle = viewModel.getClientCertSubtitle(shortcut)
        binding.inputAcceptCookies.isChecked = shortcut.acceptCookies
        binding.inputTimeout.subtitle = viewModel.getTimeoutSubtitle(shortcut)
        if (isInitial) {
            binding.inputProxyHost.rawString = shortcut.proxyHost ?: ""
            binding.inputProxyPort.setText(shortcut.proxyPort?.toString() ?: "")
            binding.inputSsid.setText(shortcut.wifiSsid)
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
        try {
            ClientCertUtil.promptForAlias(this) { alias ->
                setClientCertParams(ClientCertParams.Alias(alias))
            }
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
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
        label.text = viewModel.getTimeoutText(shortcut.timeout.milliseconds)
        slider.progress = timeoutToProgress(shortcut.timeout.milliseconds)

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
        )

        private fun timeoutToProgress(timeout: Duration) = TIMEOUT_OPTIONS.indexOfFirst {
            it >= timeout
        }
            .takeUnless { it == -1 }
            ?: TIMEOUT_OPTIONS.lastIndex

        private fun progressToTimeout(progress: Int) = TIMEOUT_OPTIONS[progress]
    }
}
