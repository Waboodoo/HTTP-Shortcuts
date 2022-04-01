package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityRemoteEditBinding

class RemoteEditActivity : BaseActivity() {

    private val viewModel: RemoteEditViewModel by bindViewModel()

    private lateinit var binding: ActivityRemoteEditBinding

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityRemoteEditBinding.inflate(layoutInflater))
    }

    private fun initUserInputBindings() {
        binding.inputPassword
            .observeTextChanges()
            .subscribe {
                viewModel.onPasswordChanged(it.toString())
            }
            .attachTo(destroyer)

        binding.buttonRemoteEditUpload.setOnClickListener {
            viewModel.onUploadButtonClicked()
        }
        binding.buttonRemoteEditDownload.setOnClickListener {
            viewModel.onDownloadButtonClicked()
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.instructionsList.setText(viewState.instructions)
            binding.remoteEditDeviceId.text = viewState.deviceId
            binding.inputPassword.setTextSafely(viewState.password)
            binding.buttonRemoteEditUpload.isEnabled = viewState.canUpload
            binding.buttonRemoteEditDownload.isEnabled = viewState.canDownload
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.remote_edit_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_change_remote_host -> consume {
                viewModel.onChangeRemoteHostButtonClicked()
            }
            else -> super.onOptionsItemSelected(item)
        }

    object OpenRemoteEditor : BaseActivityResultContract<IntentBuilder, Boolean>(::IntentBuilder) {
        private const val EXTRA_CHANGES_IMPORTED = "changes_imported"

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            intent?.getBooleanExtra(EXTRA_CHANGES_IMPORTED, false) ?: false

        fun createResult(changesImported: Boolean) =
            createIntent {
                putExtra(EXTRA_CHANGES_IMPORTED, changesImported)
            }
    }

    class IntentBuilder : BaseIntentBuilder(RemoteEditActivity::class.java)
}
