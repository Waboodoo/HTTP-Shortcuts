package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import kotlinx.coroutines.CancellationException

class PromptColorAction(
    private val initialColor: String?,
) : BaseAction() {

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): String? =
        try {
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.ColorPicker(
                    initialColor = initialColor?.trimStart('#')?.hexStringToColorInt(),
                )
            )
                .colorIntToHexString()
        } catch (e: CancellationException) {
            null
        }
}
