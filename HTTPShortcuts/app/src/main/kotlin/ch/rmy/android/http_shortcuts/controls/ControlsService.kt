package ch.rmy.android.http_shortcuts.controls

import android.app.PendingIntent
import android.os.Build
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.ControlAction
import android.service.controls.templates.StatelessTemplate
import androidx.annotation.RequiresApi
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.controls.ControlEditorActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import org.reactivestreams.FlowAdapters
import java.util.concurrent.Flow
import java.util.function.Consumer
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.R)
class ControlsService : ControlsProviderService() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    override fun onCreate() {
        super.onCreate()
        getApplicationComponent().inject(this)
    }

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> =
        shortcutRepository.getShortcuts()
            .flattenAsFlowable { shortcuts ->
                shortcuts.map { shortcut ->
                    Control.StatelessBuilder(shortcut.id, createPendingIntent(shortcut.id))
                        .setTitle(shortcut.name)
                        .setSubtitle(shortcut.description)
                        .setDeviceType(DeviceTypes.TYPE_UNKNOWN) // TODO
                        .build()
                }
            }
            .let(FlowAdapters::toFlowPublisher)

    private fun createPendingIntent(shortcutId: ShortcutId) =
        PendingIntent.getActivity(
            context,
            CONTROL_REQUEST_CODE,
            ControlEditorActivity.IntentBuilder(shortcutId).build(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    override fun createPublisherFor(controlIds: List<String>): Flow.Publisher<Control> =
        shortcutRepository.getShortcuts()
            .flattenAsFlowable { shortcuts ->
                shortcuts
                    .filter { shortcut ->
                        shortcut.id in controlIds
                    }
                    .map { shortcut ->
                        Control.StatefulBuilder(shortcut.id, createPendingIntent(shortcut.id))
                            .setTitle(shortcut.name)
                            .setSubtitle(shortcut.description)
                            .setDeviceType(DeviceTypes.TYPE_UNKNOWN) // TODO
                            .setStatusText("Status") // TODO
                            .setStatus(Control.STATUS_OK)
                            .setControlTemplate(StatelessTemplate(""))
                            .build()
                    }
            }
            .let(FlowAdapters::toFlowPublisher)

    override fun performControlAction(controlId: String, action: ControlAction, consumer: Consumer<Int>) {
        ExecuteActivity.IntentBuilder(controlId)
            .startActivity(context)
        consumer.accept(ControlAction.RESPONSE_OK)
    }

    companion object {
        private const val CONTROL_REQUEST_CODE = 42
    }
}
