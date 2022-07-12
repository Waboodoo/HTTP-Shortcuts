package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.DialogTextBinding
import ch.rmy.android.http_shortcuts.extensions.createDestroyer
import ch.rmy.android.http_shortcuts.extensions.reloadImageSpans
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class DialogAction(private val message: String, private val title: String) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )
        return if (finalMessage.isNotEmpty()) {
            Completable
                .create { emitter ->
                    val destroyer = emitter.createDestroyer()
                    val view = DialogTextBinding.inflate(LayoutInflater.from(executionContext.context))
                    val textView = view.text
                    textView.text = HTMLUtil.formatWithImageSupport(
                        finalMessage,
                        executionContext.context,
                        textView::reloadImageSpans,
                        destroyer,
                    )
                    textView.movementMethod = LinkMovementMethod.getInstance()
                    DialogBuilder(executionContext.context)
                        .title(title)
                        .view(view.root)
                        .positive(R.string.dialog_ok)
                        .dismissListener { emitter.onComplete() }
                        .show()
                }
                .subscribeOn(AndroidSchedulers.mainThread())
        } else {
            Completable.complete()
        }
    }
}
