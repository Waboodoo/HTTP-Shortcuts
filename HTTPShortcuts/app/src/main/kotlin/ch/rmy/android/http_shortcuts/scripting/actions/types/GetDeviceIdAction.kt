package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class GetDeviceIdAction
@Inject
constructor(
    private val deviceLocalPreferences: DeviceLocalPreferences,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext) =
        deviceLocalPreferences.deviceId
}
