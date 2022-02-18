package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.cancel
import io.reactivex.Single
import io.reactivex.SingleEmitter

abstract class BaseVariableType {

    abstract fun resolveValue(context: Context, variable: Variable): Single<String>

    companion object {

        internal fun createDialogBuilder(
            context: Context,
            variable: Variable,
            emitter: SingleEmitter<String>,
        ) =
            DialogBuilder(context)
                .mapIf(variable.title.isNotEmpty()) {
                    title(variable.title)
                }
                .dismissListener {
                    emitter.cancel()
                }

        internal fun Single<String>.storeValueIfNeeded(variable: Variable, variablesRepository: VariableRepository): Single<String> =
            mapIf(variable.rememberValue) {
                flatMap { resolvedValue ->
                    variablesRepository.setVariableValue(variable.id, resolvedValue)
                        .toSingle { resolvedValue }
                }
            }
    }
}
