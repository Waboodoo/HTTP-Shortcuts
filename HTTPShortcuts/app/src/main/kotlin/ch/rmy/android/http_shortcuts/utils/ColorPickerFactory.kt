package ch.rmy.android.http_shortcuts.utils

import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import javax.inject.Inject

class ColorPickerFactory
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {

    fun createColorPicker(
        onColorPicked: (Int) -> Unit,
        onCanceled: () -> Unit,
        title: Localizable = Localizable.EMPTY,
        @ColorInt initialColor: Int? = null,
    ): AlertDialog {
        val activity = activityProvider.getActivity()
        return ColorPickerDialog.Builder(activity)
            .runIfNotNull(title.localize(activity).takeUnlessEmpty()) {
                setTitle(it)
            }
            .setPositiveButton(
                R.string.dialog_ok,
                ColorEnvelopeListener { envelope, fromUser ->
                    if (fromUser) {
                        onColorPicked(envelope.color)
                    }
                }
            )
            .setNegativeButton(R.string.dialog_cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .setOnDismissListener {
                onCanceled()
            }
            .applyIfNotNull(initialColor) {
                colorPickerView.setInitialColor(it)
            }
            .create()
    }
}
