package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.models.VariableModel

class UUIDType : BaseVariableType() {

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(context: Context, variable: VariableModel) =
        UUIDUtils.newUUID()
}
