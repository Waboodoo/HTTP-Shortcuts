package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.graphics.Color
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ColorType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun resolveValue(context: Context, variable: VariableModel): Single<String> =
        Single.create<String> { emitter ->
            ColorPickerDialog.Builder(activityProvider.getActivity())
                .runIf(variable.title.isNotEmpty()) {
                    setTitle(variable.title)
                }
                .setPositiveButton(
                    R.string.dialog_ok,
                    ColorEnvelopeListener { envelope, fromUser ->
                        if (fromUser && variable.isValid) {
                            emitter.onSuccess(envelope.color.colorIntToHexString())
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
        variable.takeIf { it.rememberValue }?.value?.hexStringToColorInt()
            ?: Color.BLACK
}
