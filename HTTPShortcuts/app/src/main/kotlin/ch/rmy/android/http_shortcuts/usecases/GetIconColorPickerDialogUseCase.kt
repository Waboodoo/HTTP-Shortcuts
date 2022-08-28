package ch.rmy.android.http_shortcuts.usecases

import android.graphics.Color
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.ColorPickerFactory
import ch.rmy.android.http_shortcuts.utils.Settings
import javax.inject.Inject

class GetIconColorPickerDialogUseCase
@Inject
constructor(
    private val settings: Settings,
    private val colorPickerFactory: ColorPickerFactory,
) {
    operator fun invoke(icon: ShortcutIcon, onDismissed: () -> Unit, onColorSelected: (ShortcutIcon) -> Unit): DialogState? =
        if (icon is ShortcutIcon.BuiltInIcon && icon.tint != null) {
            DialogState.create {
                colorPickerFactory.createColorPicker(
                    onColorPicked = { color ->
                        settings.previousIconColor = color
                        onColorSelected(icon.withTint(color))
                    },
                    onDismissed = onDismissed,
                    initialColor = settings.previousIconColor ?: Color.BLACK,
                )
            }
        } else {
            onColorSelected(icon)
            null
        }
}
