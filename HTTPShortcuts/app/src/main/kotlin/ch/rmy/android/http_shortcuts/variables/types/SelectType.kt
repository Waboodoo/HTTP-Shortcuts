package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class SelectType : BaseVariableType() {

    private val variablesRepository = VariableRepository()

    override fun resolveValue(context: Context, variable: VariableModel): Single<String> =
        Single
            .create<String> { emitter ->
                createDialogBuilder(context, variable, emitter)
                    .run {
                        if (isMultiSelect(variable)) {
                            val selectedOptions = mutableSetOf<String>()
                            runFor(variable.options!!) { option ->
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
                            runFor(variable.options!!) { option ->
                                item(name = option.labelOrValue) {
                                    emitter.onSuccess(option.value)
                                }
                            }
                        }
                    }
                    .showIfPossible()
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .storeValueIfNeeded(variable, variablesRepository)

    companion object {
        const val KEY_MULTI_SELECT = "multi_select"
        const val KEY_SEPARATOR = "separator"

        fun isMultiSelect(variable: VariableModel) =
            variable.dataForType[KEY_MULTI_SELECT]?.toBoolean() ?: false

        fun getSeparator(variable: VariableModel) =
            variable.dataForType[KEY_SEPARATOR] ?: ","
    }
}
