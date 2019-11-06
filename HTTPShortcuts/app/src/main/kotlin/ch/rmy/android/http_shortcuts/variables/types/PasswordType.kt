package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.InputType
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.mapIf
import io.reactivex.Single

class PasswordType : TextType() {

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<String> { emitter ->
            createDialogBuilder(context, variable, emitter)
                .textInput(
                    prefill = variable.value?.takeIf { variable.rememberValue } ?: "",
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                ) { input ->
                    emitter.onSuccess(input.toString())
                }
                .showIfPossible()
        }
            .mapIf(variable.rememberValue) {
                it.flatMap { resolvedValue ->
                    Commons.setVariableValue(variable.id, resolvedValue)
                        .toSingle { resolvedValue }
                }
            }


}
