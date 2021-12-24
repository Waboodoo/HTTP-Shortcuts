package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class CopyToClipboardAction(private val text: String) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable
            .fromAction {
                text
                    .takeUnlessEmpty()
                    ?.let {
                        ClipboardUtil.copyToClipboard(executionContext.context, it)
                    }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
}
