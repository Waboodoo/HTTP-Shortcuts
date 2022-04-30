package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import io.reactivex.Single
import io.reactivex.SingleEmitter

abstract class BaseVariableType {

    abstract fun resolveValue(context: Context, variable: VariableModel): Single<String>

    companion object {

        internal fun createDialogBuilder(
            context: Context,
            variable: VariableModel,
            emitter: SingleEmitter<String>,
        ) =
            DialogBuilder(context)
                .runIf(variable.title.isNotEmpty()) {
                    title(variable.title)
                }
                .dismissListener {
                    emitter.cancel()
                }

        internal fun Single<String>.storeValueIfNeeded(variable: VariableModel, variablesRepository: VariableRepository): Single<String> =
            runIf(variable.rememberValue) {
                flatMap { resolvedValue ->
                    variablesRepository.setVariableValue(variable.id, resolvedValue)
                        .toSingle { resolvedValue }
                }
            }
    }
}
