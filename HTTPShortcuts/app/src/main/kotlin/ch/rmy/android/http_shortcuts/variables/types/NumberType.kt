package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.InputType
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class NumberType : TextType() {

    private val variablesRepository = VariableRepository()

    override fun resolveValue(context: Context, variable: VariableModel): Single<String> =
        Single.create<String> { emitter ->
            createDialogBuilder(context, variable, emitter)
                .textInput(
                    prefill = variable.value?.takeIf { variable.rememberValue }?.toIntOrNull()?.toString() ?: "",
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED,
                    allowEmpty = false,
                    callback = emitter::onSuccess,
                )
                .showIfPossible()
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .map(::sanitize)
            .storeValueIfNeeded(variable, variablesRepository)

    private fun sanitize(input: String) =
        input.trimEnd('.')
            .let {
                when {
                    it.startsWith("-.") -> "-0.${it.drop(2)}"
                    it.startsWith(".") -> "0$it"
                    it.isEmpty() || it == "-" -> "0"
                    else -> it
                }
            }
}
