package ch.rmy.android.http_shortcuts.activities.history.usecases

import android.content.Context
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.history.HistoryListItem
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent.Companion.getEventData
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.http.HttpStatus
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent as HistoryEventModel

class MapEventsUseCase
@Inject
constructor() {

    operator fun invoke(events: List<HistoryEventModel>): List<HistoryListItem> {
        if (events.isEmpty()) {
            return listOf(HistoryListItem.EmptyState)
        }
        return events.mapNotNull { eventModel ->
            val event = eventModel.getEvent() ?: return@mapNotNull null
            HistoryListItem.HistoryEvent(
                id = eventModel.id,
                time = LocalDateTime.ofInstant(eventModel.eventTime, ZoneId.systemDefault()),
                title = event.getTitle(),
                detail = event.getDetail(),
                displayType = event.getDisplayType(),
            )
        }
    }

    private fun HistoryEventModel.getEvent(): HistoryEvent? =
        when (eventType) {
            HistoryEventType.SHORTCUT_TRIGGERED -> getEventData<HistoryEvent.ShortcutTriggered>()
            HistoryEventType.HTTP_REQUEST_SENT -> getEventData<HistoryEvent.HttpRequestSent>()
            HistoryEventType.HTTP_RESPONSE_RECEIVED -> getEventData<HistoryEvent.HttpResponseReceived>()
            HistoryEventType.NETWORK_ERROR -> getEventData<HistoryEvent.NetworkError>()
            HistoryEventType.ERROR -> getEventData<HistoryEvent.Error>()
            HistoryEventType.CUSTOM_EVENT -> getEventData<HistoryEvent.CustomEvent>()
            null -> null
        }

    private fun HistoryEvent.getTitle(): Localizable =
        when (this) {
            is HistoryEvent.ShortcutTriggered -> Localizable.create {
                it.getString(R.string.event_history_title_shortcut_triggered, shortcutName)
            }
            is HistoryEvent.HttpRequestSent -> Localizable.create {
                it.getString(R.string.event_history_title_http_request_sent, shortcutName)
            }
            is HistoryEvent.HttpResponseReceived -> Localizable.create {
                it.getString(
                    R.string.event_history_title_http_response_received,
                    shortcutName,
                    "$responseCode (${HttpStatus.getMessage(responseCode)})",
                )
            }
            is HistoryEvent.NetworkError -> Localizable.create {
                it.getString(R.string.event_history_title_network_error, shortcutName)
            }
            is HistoryEvent.Error -> Localizable.create {
                it.getString(R.string.event_history_title_execution_error, shortcutName)
            }
            is HistoryEvent.CustomEvent -> Localizable.create {
                title
            }
        }

    private fun HistoryEvent.getDetail(): Localizable? =
        tryOrLog {
            when (this) {
                is HistoryEvent.ShortcutTriggered -> trigger?.let {
                    Localizable.create { context ->
                        context.getString(R.string.label_prefix_event_history_trigger_origin, trigger.toHumanReadableString(context))
                    }
                }
                is HistoryEvent.HttpRequestSent -> "$method $url\n\n${formatHeaders(headers)}".toLocalizable()
                is HistoryEvent.HttpResponseReceived -> formatHeaders(headers).toLocalizable()
                is HistoryEvent.NetworkError -> error.toLocalizable()
                is HistoryEvent.Error -> error.toLocalizable()
                is HistoryEvent.CustomEvent -> message?.takeUnlessEmpty()?.toLocalizable()
            }
        }

    private fun formatHeaders(headers: Map<String, List<String>>): String =
        headers.entries.flatMap { headerList ->
            headerList.value.map { "${headerList.key}: ${heuristicallyRemoveSecrets(headerList.key, it)}" }
        }
            .joinToString(separator = "\n")

    private fun ShortcutTriggerType.toHumanReadableString(context: Context): String =
        when (this) {
            ShortcutTriggerType.DIALOG_RERUN -> context.getString(R.string.label_event_history_trigger_type_rerun_from_dialog)
            ShortcutTriggerType.WINDOW_RERUN -> context.getString(R.string.label_event_history_trigger_type_rerun_from_window)
            ShortcutTriggerType.MAIN_SCREEN -> context.getString(R.string.label_event_history_trigger_type_main_screen)
            ShortcutTriggerType.SHARE -> context.getString(R.string.label_event_history_trigger_type_share)
            ShortcutTriggerType.QUICK_SETTINGS_TILE -> context.getString(R.string.label_event_history_trigger_type_quick_settings_tile)
            ShortcutTriggerType.APP_SHORTCUT -> context.getString(R.string.label_event_history_trigger_type_app_shortcut)
            ShortcutTriggerType.HOME_SCREEN_SHORTCUT -> context.getString(R.string.label_event_history_trigger_type_home_screen_shortcut)
            ShortcutTriggerType.LEGACY_SHORTCUT -> context.getString(R.string.label_event_history_trigger_type_legacy_shortcut)
            ShortcutTriggerType.WIDGET -> context.getString(R.string.label_event_history_trigger_type_widget)
            ShortcutTriggerType.SCRIPTING -> context.getString(R.string.label_event_history_trigger_type_scripting)
            else -> name.lowercase().replace('_', ' ')
        }

    private fun HistoryEvent.getDisplayType(): HistoryListItem.HistoryEvent.DisplayType? =
        when (this) {
            is HistoryEvent.HttpResponseReceived -> if (isSuccess) {
                HistoryListItem.HistoryEvent.DisplayType.SUCCESS
            } else {
                HistoryListItem.HistoryEvent.DisplayType.FAILURE
            }
            is HistoryEvent.NetworkError -> HistoryListItem.HistoryEvent.DisplayType.FAILURE
            is HistoryEvent.Error -> HistoryListItem.HistoryEvent.DisplayType.FAILURE
            else -> null
        }

    private fun heuristicallyRemoveSecrets(key: String, value: String): String {
        val k = key.lowercase()
        if (SECRET_KEYWORDS.any { it in k }) {
            return value.redactAfter((value.length / 2).coerceIn(3, 8))
        }
        return value
    }

    private fun String.redactAfter(offset: Int): String {
        val prefix = take(offset)
        return prefix + asterisks(length - prefix.length)
    }

    private fun asterisks(count: Int): String =
        "*".repeat(count)

    companion object {
        internal val SECRET_KEYWORDS = setOf(
            "token",
            "login",
            "auth",
            "secret",
            "password",
            "passphrase",
        )
    }
}
