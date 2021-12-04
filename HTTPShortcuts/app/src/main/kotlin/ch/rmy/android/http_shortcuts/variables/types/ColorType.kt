package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.graphics.Color
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.mapIf
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import io.reactivex.Single

internal class ColorType : BaseVariableType() {

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<String> { emitter ->
            ColorPickerDialog.Builder(context)
                .setPositiveButton(
                    R.string.dialog_ok,
                    ColorEnvelopeListener { envelope, fromUser ->
                        if (fromUser && variable.isValid) {
                            val colorFormatted = String.format("%06x", envelope.color and 0xffffff)
                            emitter.onSuccess(colorFormatted)
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
                    if (!emitter.isDisposed) {
                        emitter.cancel()
                    }
                }
                .apply {
                    colorPickerView.setInitialColor(getInitialColor(variable))
                }
                .show()
        }
            .mapIf(variable.rememberValue) {
                flatMap { variableValue ->
                    Commons.setVariableValue(variable.id, variableValue)
                        .toSingle { variableValue }
                }
            }

    private fun getInitialColor(variable: Variable): Int {
        if (variable.rememberValue && variable.value!!.length == 6) {
            val color = variable.value?.toIntOrNull(16) ?: Color.BLACK
            return color + 0xff000000.toInt()
        }
        return Color.BLACK
    }

    override fun createEditorFragment() = ColorEditorFragment()
}
