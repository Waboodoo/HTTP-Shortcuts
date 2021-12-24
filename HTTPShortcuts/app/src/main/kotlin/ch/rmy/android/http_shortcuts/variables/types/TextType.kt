package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.InputType
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.reactivex.Single

open class TextType : BaseVariableType() {

    private val variablesRepository = VariableRepository()

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<String> { emitter ->
            createDialogBuilder(context, variable, emitter)
                .textInput(
                    prefill = variable.value?.takeIf { variable.rememberValue } ?: "",
                    inputType = InputType.TYPE_CLASS_TEXT or (if (variable.isMultiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0),
                    callback = emitter::onSuccess,
                )
                .showIfPossible()
        }
            .storeValueIfNeeded(variable, variablesRepository)
}
