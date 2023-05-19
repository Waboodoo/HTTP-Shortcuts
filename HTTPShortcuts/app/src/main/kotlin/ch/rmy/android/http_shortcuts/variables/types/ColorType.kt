package ch.rmy.android.http_shortcuts.variables.types

import android.graphics.Color
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import javax.inject.Inject

class ColorType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String {
        val result = dialogHandle.showDialog(
            ExecuteDialogState.ColorPicker(
                title = variable.title.toLocalizable(),
                initialColor = getInitialColor(variable),
            ),
        )
        val value = (result as Int).colorIntToHexString()
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

    private fun getInitialColor(variable: Variable): Int =
        variable.takeIf { it.rememberValue }?.value?.hexStringToColorInt()
            ?: Color.WHITE
}
