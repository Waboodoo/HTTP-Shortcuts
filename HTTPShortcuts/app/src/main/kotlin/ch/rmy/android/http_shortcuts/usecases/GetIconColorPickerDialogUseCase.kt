package ch.rmy.android.http_shortcuts.usecases

import android.graphics.Color
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import javax.inject.Inject

class GetIconColorPickerDialogUseCase
@Inject
constructor() {
    operator fun invoke(icon: ShortcutIcon, onDismissed: () -> Unit, onColorSelected: (ShortcutIcon) -> Unit): DialogState? =
        if (icon is ShortcutIcon.BuiltInIcon && icon.tint != null) {
            DialogState.create {
                ColorPickerDialog.Builder(context)
                    .setPositiveButton(
                        R.string.dialog_ok,
                        ColorEnvelopeListener { envelope, fromUser ->
                            if (fromUser) {
                                onColorSelected(icon.withTint(envelope.color))
                            }
                        },
                    )
                    .setNegativeButton(R.string.dialog_cancel) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .setOnDismissListener {
                        onDismissed()
                    }
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true)
                    .setBottomSpace(12)
                    .apply {
                        colorPickerView.setInitialColor(Color.BLACK)
                    }
                    .create()
            }
        } else {
            onColorSelected(icon)
            null
        }
}
