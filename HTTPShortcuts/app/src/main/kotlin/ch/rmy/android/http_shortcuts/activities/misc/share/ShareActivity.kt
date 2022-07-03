package ch.rmy.android.http_shortcuts.activities.misc.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.framework.utils.RxUtils
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.utils.FileUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ShareActivity : BaseActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    private val viewModel: ShareViewModel by bindViewModel()

    override fun onCreated(savedState: Bundle?) {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        if (savedState == null) {
            cacheFiles(getFileUris()) { cachedFiles ->
                viewModel.initialize(ShareViewModel.InitData(text, title, cachedFiles))
            }
        }
        initViewModelBindings()
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    private fun getFileUris(): List<Uri> =
        if (intent.action == Intent.ACTION_SEND) {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { listOf(it) }
        } else {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }
            ?: emptyList()

    private fun cacheFiles(fileUris: List<Uri>, action: (List<Uri>) -> Unit) {
        if (fileUris.isEmpty()) {
            action(emptyList())
            return
        }
        val context = applicationContext
        RxUtils.single {
            cacheSharedFiles(context, fileUris)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action) { e ->
                if (!isFinishing) {
                    showToast(R.string.error_generic)
                    logException(e)
                    finishWithoutAnimation()
                }
            }
            .attachTo(destroyer)
    }

    companion object {
        private fun cacheSharedFiles(context: Context, fileUris: List<Uri>): List<Uri> =
            fileUris
                .map { fileUri ->
                    context.contentResolver.openInputStream(fileUri)!!
                        .use { stream ->
                            FileUtil.createCacheFile(context, createCacheFileName())
                                .also { file ->
                                    FileUtil.getFileName(context.contentResolver, fileUri)
                                        ?.let { fileName ->
                                            FileUtil.putCacheFileOriginalName(file, fileName)
                                        }
                                    stream.copyTo(context.contentResolver.openOutputStream(file)!!)
                                }
                        }
                }

        private fun createCacheFileName() = "shared_${newUUID()}"
    }
}
