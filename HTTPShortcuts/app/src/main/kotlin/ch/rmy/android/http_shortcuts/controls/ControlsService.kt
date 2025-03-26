package ch.rmy.android.http_shortcuts.controls

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.ControlAction
import android.service.controls.templates.StatelessTemplate
import androidx.annotation.RequiresApi
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Flow
import java.util.function.Consumer
import javax.inject.Inject
import kotlinx.coroutines.jdk9.flowPublish

@RequiresApi(Build.VERSION_CODES.R)
@AndroidEntryPoint
class ControlsService : ControlsProviderService() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var executionStarter: ExecutionStarter

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control?> =
        createPublisher()

    override fun createPublisherFor(controlIds: List<String?>): Flow.Publisher<Control?> =
        createPublisher { (_, shortcut) ->
            shortcut.id in controlIds
        }

    private fun createPublisher(filter: ((Pair<Category, Shortcut>) -> Boolean)? = null): Flow.Publisher<Control?> =
        flowPublish<Control?> {
            val categories = categoryRepository.getCategories()
            val shortcutsByCategoryId = shortcutRepository.getShortcuts()
                .groupBy { it.categoryId }

            categories.flatMap { category ->
                (shortcutsByCategoryId[category.id] ?: emptyList())
                    .map { shortcut ->
                        category to shortcut
                    }
            }
                .runIfNotNull(filter) { predicate ->
                    filter(predicate)
                }
                .map { (category, shortcut) ->
                    Control.StatefulBuilder(shortcut.id, createPendingIntent())
                        .setTitle(shortcut.name)
                        .setSubtitle(shortcut.description)
                        .setDeviceType(shortcut.getDeviceType())
                        .setStatus(Control.STATUS_OK)
                        .setControlTemplate(StatelessTemplate(""))
                        .setZone(category.name)
                        .run {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                setAuthRequired(false)
                            } else {
                                this
                            }
                        }
                        .build()
                }
                .forEach { control ->
                    send(control)
                }
        }

    private fun Shortcut.getDeviceType(): Int {
        val name = icon.toString()
        if (name.contains("light") || name.contains("bright")) {
            return DeviceTypes.TYPE_LIGHT
        }
        return DeviceTypes.TYPE_UNKNOWN
    }

    private fun createPendingIntent(): PendingIntent =
        MainActivity.IntentBuilder()
            .build(context)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { intent ->
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)
            }

    override fun performControlAction(
        controlId: String,
        action: ControlAction,
        consumer: Consumer<Int?>,
    ) {
        executionStarter.execute(controlId, trigger = ShortcutTriggerType.QUICK_ACCESS_DEVICE_CONTROLS)
        consumer.accept(ControlAction.RESPONSE_OK)
    }
}
