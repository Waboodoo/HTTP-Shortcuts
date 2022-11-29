package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.showOrElse
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ColorPickerFactory
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class PromptColorAction(
    private val initialColor: String?,
) : BaseAction() {

    @Inject
    lateinit var colorPickerFactory: ColorPickerFactory

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): String? =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<String> { continuation ->
                colorPickerFactory.createColorPicker(
                    onColorPicked = { color ->
                        continuation.resume(color.colorIntToHexString())
                    },
                    onDismissed = {
                        if (continuation.isActive) {
                            continuation.resume("")
                        }
                    },
                    initialColor = initialColor?.trimStart('#')?.hexStringToColorInt(),
                )
                    .showOrElse {
                        continuation.cancel()
                    }
            }
        }
            .takeUnlessEmpty()
            ?.removePrefix("-")
}
