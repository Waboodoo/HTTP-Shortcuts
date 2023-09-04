package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.activities.execute.ExternalRequests
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class ScanBarcodeAction
@Inject
constructor(
    private val externalRequests: ExternalRequests,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext): String? =
        externalRequests.scanBarcode()
}
