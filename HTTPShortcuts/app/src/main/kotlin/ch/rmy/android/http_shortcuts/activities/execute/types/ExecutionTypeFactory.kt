package ch.rmy.android.http_shortcuts.activities.execute.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class ExecutionTypeFactory
@Inject
constructor(
    context: Context,
) {
    private val entryPoint = EntryPointAccessors.fromApplication<ExecutionTypeFactoryEntryPoint>(context)

    fun createExecutionType(shortcutExecutionType: ShortcutExecutionType): ExecutionType =
        when (shortcutExecutionType) {
            ShortcutExecutionType.HTTP -> entryPoint.httpExecutionType()
            ShortcutExecutionType.BROWSER -> entryPoint.browserExecutionType()
            ShortcutExecutionType.MQTT -> entryPoint.mqttExecutionType()
            ShortcutExecutionType.WAKE_ON_LAN -> entryPoint.wakeOnLanExecutionType()
            ShortcutExecutionType.SCRIPTING,
            ShortcutExecutionType.TRIGGER,
            -> entryPoint.noopExecutionType()
        }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ExecutionTypeFactoryEntryPoint {
        fun httpExecutionType(): HttpExecutionType
        fun browserExecutionType(): BrowserExecutionType
        fun mqttExecutionType(): MqttExecutionType
        fun wakeOnLanExecutionType(): WakeOnLanExecutionType
        fun noopExecutionType(): NoopExecutionType
    }
}
