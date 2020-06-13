package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ClipboardUtil
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class CopyToClipboardAction(data: Map<String, String>) : BaseAction() {

    private val text: String = data[KEY_TEXT] ?: ""

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable
            .fromAction {
                text
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        ClipboardUtil.copyToClipboard(executionContext.context, it)
                    }
            }
            .subscribeOn(AndroidSchedulers.mainThread())

    companion object {

        const val KEY_TEXT = "text"

    }

}