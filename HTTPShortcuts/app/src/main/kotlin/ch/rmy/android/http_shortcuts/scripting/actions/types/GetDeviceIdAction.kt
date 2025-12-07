package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.data.settings.Settings
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class GetDeviceIdAction
@Inject
constructor(
    private val settings: Settings,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext) =
        settings.deviceId
}
