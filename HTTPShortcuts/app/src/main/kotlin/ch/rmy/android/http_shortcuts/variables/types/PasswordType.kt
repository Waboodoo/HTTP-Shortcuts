package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.InputType
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class PasswordType : TextType() {

    override fun resolveValue(context: Context, variable: VariableModel): Single<String> =
        Single.create<String> { emitter ->
            createDialogBuilder(context, variable, emitter)
                .textInput(
                    prefill = variable.value?.takeIf { variable.rememberValue } ?: "",
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    callback = emitter::onSuccess,
                )
                .showIfPossible()
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .storeValueIfNeeded(variable, variablesRepository)
}
