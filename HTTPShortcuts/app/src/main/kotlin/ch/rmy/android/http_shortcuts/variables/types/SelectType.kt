package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.mapFor
import io.reactivex.Single

internal class SelectType : BaseVariableType(), HasTitle {

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single
            .create<String> { emitter ->
                createDialogBuilder(context, variable, emitter)
                    .run {
                        if (isMultiSelect(variable)) {
                            val selectedOptions = mutableSetOf<String>()
                            mapFor(variable.options!!) { option ->
                                checkBoxItem(name = option.labelOrValue) { isChecked ->
                                    if (isChecked) {
                                        selectedOptions.add(option.id)
                                    } else {
                                        selectedOptions.remove(option.id)
                                    }
                                }
                                    .positive(R.string.dialog_ok) {
                                        emitter.onSuccess(
                                            variable.options!!
                                                .filter { selectedOptions.contains(it.id) }
                                                .joinToString(getSeparator(variable)) { option ->
                                                    option.value
                                                }
                                        )
                                    }
                            }
                        } else {
                            mapFor(variable.options!!) { option ->
                                item(name = option.labelOrValue) {
                                    emitter.onSuccess(option.value)
                                }
                            }
                        }
                    }
                    .showIfPossible()
            }
            .flatMap { resolvedValue ->
                Commons.setVariableValue(variable.id, resolvedValue)
                    .toSingle { resolvedValue }
            }

    override fun createEditorFragment() = SelectEditorFragment()

    companion object {
        const val KEY_MULTI_SELECT = "multi_select"
        const val KEY_SEPARATOR = "separator"

        fun isMultiSelect(variable: Variable) =
            variable.dataForType[KEY_MULTI_SELECT]?.toBoolean() ?: false

        fun getSeparator(variable: Variable) =
            variable.dataForType[KEY_SEPARATOR] ?: ","
    }

}
