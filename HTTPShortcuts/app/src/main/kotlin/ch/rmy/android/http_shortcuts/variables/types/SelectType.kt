package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.extensions.addOrRemove
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class SelectType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(context: Context, variable: VariableModel): String {
        val value = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<String> { continuation ->
                createDialogBuilder(activityProvider.getActivity(), variable, continuation)
                    .run {
                        if (isMultiSelect(variable)) {
                            val selectedOptions = mutableListOf<String>()
                            runFor(variable.options!!) { option ->
                                checkBoxItem(name = option.labelOrValue, checked = { option.id in selectedOptions }) { isChecked ->
                                    selectedOptions.addOrRemove(option.id, isChecked)
                                }
                                    .positive(R.string.dialog_ok) {
                                        continuation.resume(
                                            selectedOptions
                                                .mapNotNull { optionId ->
                                                    variable.options!!.find { it.id == optionId }
                                                }
                                                .joinToString(getSeparator(variable)) { option ->
                                                    option.value
                                                }
                                        )
                                    }
                            }
                        } else {
                            runFor(variable.options!!) { option ->
                                item(name = option.labelOrValue) {
                                    continuation.resume(option.value)
                                }
                            }
                        }
                    }
                    .showOrElse {
                        continuation.cancel()
                    }
            }
        }
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

    companion object {
        const val KEY_MULTI_SELECT = "multi_select"
        const val KEY_SEPARATOR = "separator"

        fun isMultiSelect(variable: VariableModel) =
            variable.dataForType[KEY_MULTI_SELECT]?.toBoolean() ?: false

        fun getSeparator(variable: VariableModel) =
            variable.dataForType[KEY_SEPARATOR] ?: ","
    }
}
