package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import io.reactivex.Single

internal class SelectType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = true

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<String> { emitter ->
            createDialogBuilder(context, variable, emitter)
                .mapFor(variable.options!!) { builder, option ->
                    builder.item(option.labelOrValue) {
                        emitter.onSuccess(option.value)
                    }
                }
                .showIfPossible()
        }
            .flatMap { resolvedValue ->
                Commons.setVariableValue(variable.id, resolvedValue)
                    .toSingle { resolvedValue }
            }

    override fun createEditorFragment() = SelectEditorFragment()

}
