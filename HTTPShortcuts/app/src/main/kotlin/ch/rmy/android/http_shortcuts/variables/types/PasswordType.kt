package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.InputType
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import io.reactivex.Single

class PasswordType : TextType() {

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<String> { emitter ->
            createDialogBuilder(context, variable, emitter)
                .toDialogBuilder()
                .input(null, if (variable.rememberValue) variable.value else "") { _, input ->
                    emitter.onSuccess(input.toString())
                }
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .showIfPossible()
        }
            .mapIf(variable.rememberValue) {
                it.flatMap { resolvedValue ->
                    Commons.setVariableValue(variable.id, resolvedValue)
                        .toSingle { resolvedValue }
                }
            }


}
