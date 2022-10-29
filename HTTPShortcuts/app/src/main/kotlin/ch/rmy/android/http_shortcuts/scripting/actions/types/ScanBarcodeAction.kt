package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.activities.execute.ExternalRequests
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class ScanBarcodeAction : BaseAction() {

    @Inject
    lateinit var externalRequests: ExternalRequests

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): String? =
        externalRequests.scanBarcode()
}
