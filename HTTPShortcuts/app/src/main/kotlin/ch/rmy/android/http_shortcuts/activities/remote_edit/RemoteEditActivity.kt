package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.showSnackbar
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityRemoteEditBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.StringUtils
import ch.rmy.android.http_shortcuts.utils.Validation
import io.reactivex.android.schedulers.AndroidSchedulers

class RemoteEditActivity : BaseActivity() {

    private val viewModel: RemoteEditViewModel by bindViewModel()

    private lateinit var binding: ActivityRemoteEditBinding

    override fun onCreated(savedState: Bundle?) {
        binding = applyBinding(ActivityRemoteEditBinding.inflate(layoutInflater))

        binding.remoteEditDeviceId.text = viewModel.deviceId
        binding.inputPassword.setText(viewModel.password)
        updateInstructions()

        binding.inputPassword
            .observeTextChanges()
            .subscribe {
                updateViews()
            }
            .attachTo(destroyer)

        binding.inputPassword
            .observeTextChanges()
            .subscribe {
                viewModel.password = it.toString()
            }
            .attachTo(destroyer)

        binding.buttonRemoteEditUpload.setOnClickListener {
            upload()
        }
        binding.buttonRemoteEditDownload.setOnClickListener {
            download()
        }

        updateViews()
    }

    private fun updateInstructions() {
        binding.instructionsList.text = StringUtils.getOrderedList(
            listOf(
                getString(R.string.instructions_remote_edit_step_1),
                getString(R.string.instructions_remote_edit_step_2),
                getString(R.string.instructions_remote_edit_step_3, "<b>${viewModel.humanReadableEditorAddress}</b>"),
                getString(R.string.instructions_remote_edit_step_4),
            )
                .map(HTMLUtil::getHTML)
        )
    }

    private fun upload() {
        val progressDialog = ProgressDialog(context).apply {
            setMessage(context.getString(R.string.remote_edit_upload_in_progress))
            setCanceledOnTouchOutside(false)
        }
        viewModel.upload()
            .doOnSubscribe {
                progressDialog.show()
            }
            .doOnEvent {
                progressDialog.dismiss()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    showSnackbar(R.string.message_remote_edit_upload_successful)
                },
                { error ->
                    logException(error)
                    showMessageDialog(R.string.error_remote_edit_upload)
                }
            )
            .attachTo(destroyer)
    }

    private fun download() {
        val progressDialog = ProgressDialog(context).apply {
            setMessage(context.getString(R.string.remote_edit_download_in_progress))
            setCanceledOnTouchOutside(false)
        }
        viewModel.download()
            .doOnSubscribe {
                progressDialog.show()
            }
            .doOnEvent { _, _ ->
                progressDialog.dismiss()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    setResult(
                        Activity.RESULT_OK,
                        createIntent {
                            putExtra(EXTRA_CHANGES_IMPORTED, true)
                        },
                    )
                    showSnackbar(R.string.message_remote_edit_download_successful)
                },
                { error ->
                    if (error is ImportException) {
                        showMessageDialog(getString(R.string.error_remote_edit_download) + " " + error.message)
                    } else {
                        logException(error)
                        showMessageDialog(R.string.error_remote_edit_download)
                    }
                }
            )
            .attachTo(destroyer)
    }

    private fun updateViews() {
        binding.buttonRemoteEditUpload.isEnabled = binding.inputPassword.text.isNotEmpty()
        binding.buttonRemoteEditDownload.isEnabled = binding.inputPassword.text.isNotEmpty()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.remote_edit_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_change_remote_host -> consume {
                openChangeRemoteHostDialog()
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun openChangeRemoteHostDialog() {
        DialogBuilder(context)
            .title(R.string.title_change_remote_server)
            .textInput(
                prefill = viewModel.serverUrl,
                allowEmpty = false,
                callback = ::setRemoteHost,
            )
            .neutral(R.string.dialog_reset) {
                setRemoteHost("")
            }
            .showIfPossible()
    }

    private fun setRemoteHost(value: String) {
        if (value.isNotEmpty() && !Validation.isValidHttpUrl(value.toUri())) {
            DialogBuilder(context)
                .message(R.string.error_invalid_remote_edit_host_url)
                .positive(R.string.dialog_ok)
                .showIfPossible()
            return
        }
        viewModel.serverUrl = value
        updateInstructions()
    }

    class IntentBuilder : BaseIntentBuilder(RemoteEditActivity::class.java)

    companion object {
        const val EXTRA_CHANGES_IMPORTED = "changes_imported"
    }
}
