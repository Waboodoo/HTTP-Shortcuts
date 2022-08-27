package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.graphics.Color
import ch.rmy.android.framework.extensions.showIfPossible
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ColorPickerFactory
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ColorType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    @Inject
    lateinit var colorPickerFactory: ColorPickerFactory

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun resolveValue(context: Context, variable: VariableModel): Single<String> =
        Single.create<String> { emitter ->
            colorPickerFactory.createColorPicker(
                onColorPicked = { color ->
                    emitter.onSuccess(color.colorIntToHexString())
                },
                onCanceled = {
                    if (!emitter.isDisposed) {
                        emitter.cancel()
                    }
                },
                title = variable.title.toLocalizable(),
                initialColor = getInitialColor(variable),
            )
                .showIfPossible()
                ?: run {
                    if (!emitter.isDisposed) {
                        emitter.cancel()
                    }
                }
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .storeValueIfNeeded(variable, variablesRepository)

    private fun getInitialColor(variable: VariableModel): Int =
        variable.takeIf { it.rememberValue }?.value?.hexStringToColorInt()
            ?: Color.BLACK
}
