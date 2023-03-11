package ch.rmy.android.http_shortcuts.variables.types

import android.text.InputType
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class NumberType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(variable: Variable): String {
        val value = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<String> { continuation ->
                createDialogBuilder(activityProvider.getActivity(), variable, continuation)
                    .textInput(
                        prefill = variable.value?.takeIf { variable.rememberValue }?.toIntOrNull()?.toString() ?: "",
                        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED,
                        allowEmpty = false,
                        callback = continuation::resume,
                    )
                    .showOrElse {
                        continuation.cancel()
                    }
            }
        }
            .let(::sanitize)
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

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
