package ch.rmy.android.http_shortcuts.variables.types

import android.graphics.Color
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import javax.inject.Inject

class ColorType
@Inject
constructor(
    private val variablesRepository: VariableRepository,
) : VariableType {
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle): String {
        val value = dialogHandle.showDialog(
            ExecuteDialogState.ColorPicker(
                title = variable.title.toLocalizable(),
                initialColor = getInitialColor(variable),
            ),
        )
            .colorIntToHexString()
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

    private fun getInitialColor(variable: Variable): Int =
        variable.takeIf { it.rememberValue }?.realValue?.hexStringToColorInt()
            ?: Color.WHITE
}
