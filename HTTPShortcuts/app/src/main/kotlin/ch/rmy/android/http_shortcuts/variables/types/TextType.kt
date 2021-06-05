package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.InputType
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.mapIf
import io.reactivex.Single

open class TextType : BaseVariableType(), HasTitle {

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<String> { emitter ->
            createDialogBuilder(context, variable, emitter)
                .textInput(
                    prefill = variable.value?.takeIf { variable.rememberValue } ?: "",
                    inputType = InputType.TYPE_CLASS_TEXT or (if (variable.isMultiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0),
                ) { input ->
                    emitter.onSuccess(input)
                }
                .showIfPossible()
        }
            .mapIf(variable.rememberValue) {
                flatMap { resolvedValue ->
                    Commons.setVariableValue(variable.id, resolvedValue)
                        .toSingle { resolvedValue }
                }
            }

    override fun createEditorFragment() = TextEditorFragment()

}
