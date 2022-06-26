package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.graphics.Color
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.extensions.cancel
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ColorType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun resolveValue(context: Context, variable: VariableModel): Single<String> =
        Single.create<String> { emitter ->
            ColorPickerDialog.Builder(context)
                .runIf(variable.title.isNotEmpty()) {
                    setTitle(variable.title)
                }
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
            .subscribeOn(AndroidSchedulers.mainThread())
            .storeValueIfNeeded(variable, variablesRepository)

    private fun getInitialColor(variable: VariableModel): Int =
        if (variable.rememberValue && variable.value?.length == 6) {
            val color = variable.value?.toIntOrNull(16) ?: Color.BLACK
            color + 0xff000000.toInt()
        } else Color.BLACK
}
