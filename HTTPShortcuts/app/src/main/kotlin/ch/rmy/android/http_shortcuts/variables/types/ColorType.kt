package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.mapIf
import io.reactivex.Single
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener

internal class ColorType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = false

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<String> { emitter ->
            val dialog = ChromaDialog.Builder()
                .initialColor(getInitialColor(variable))
                .colorMode(ColorMode.RGB)
                .onColorSelected(object : ColorSelectListener {
                    override fun onColorSelected(color: Int) {
                        if (variable.isValid) {
                            val colorFormatted = String.format("%06x", color and 0xffffff)
                            emitter.onSuccess(colorFormatted)
                        }
                    }
                })
                .create()

            dialog.show((context as AppCompatActivity).supportFragmentManager, "ChromaDialog")

            // The following hack is needed because the ChromaDialog library does not have a method to register a dismiss listener
            dialog.dialog?.setOnDismissListener {
                emitter.cancel()
            }
        }
            .mapIf(variable.rememberValue) {
                it.flatMap { variableValue ->
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

    override fun createEditorFragment() = TextEditorFragment()

}
