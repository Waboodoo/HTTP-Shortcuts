package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class GetClipboardContentAction : BaseAction() {

    @Inject
    lateinit var clipboardUtil: ClipboardUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun executeForValue(executionContext: ExecutionContext): Single<out Any> =
        Single.fromCallable {
            clipboardUtil.getFromClipboard()
                ?.toString()
                ?.takeUnlessEmpty()
                ?: NO_RESULT
        }
            .subscribeOn(AndroidSchedulers.mainThread())
}
