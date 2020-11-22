package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.StringUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
import java.util.concurrent.TimeUnit

class RemoteEditActivity : BaseActivity() {

    private val viewModel: RemoteEditViewModel by bindViewModel()

    private val instructions: TextView by bindView(R.id.instructions)
    private val instructionsList: TextView by bindView(R.id.instructions_list)
    private val uploadButton: Button by bindView(R.id.button_remote_edit_upload)
    private val downloadButton: Button by bindView(R.id.button_remote_edit_download)
    private val deviceIdView: TextView by bindView(R.id.remote_edit_device_id)
    private val passwordInput: EditText by bindView(R.id.input_password)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_edit)

        deviceIdView.text = viewModel.deviceId
        passwordInput.setText(viewModel.password)

        instructionsList.text = StringUtils.getOrderedList(
            listOf(
                getString(R.string.instructions_remote_edit_step_1),
                getString(R.string.instructions_remote_edit_step_2),
                getString(R.string.instructions_remote_edit_step_3, "<b>${viewModel.editorAddress}</b>"),
                getString(R.string.instructions_remote_edit_step_4),
            )
                .map(HTMLUtil::getHTML)
        )

        passwordInput.observeTextChanges()
            .subscribe {
                updateViews()
            }
            .attachTo(destroyer)

        passwordInput.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .subscribe {
                viewModel.password = it.toString()
            }
            .attachTo(destroyer)

        uploadButton.setOnClickListener {
            upload()
        }
        downloadButton.setOnClickListener {
            download()
        }

        updateViews()
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
            .doOnEvent { _ ->
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
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(EXTRA_CHANGES_IMPORTED, true)
                    })
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
        uploadButton.isEnabled = passwordInput.text.isNotEmpty()
        downloadButton.isEnabled = passwordInput.text.isNotEmpty()
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, RemoteEditActivity::class.java)

    companion object {
        const val EXTRA_CHANGES_IMPORTED = "changes_imported"
    }

}
