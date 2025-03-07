package ch.rmy.android.http_shortcuts.activities.editor.mqttmessages

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.activities.editor.mqttmessages.models.MqttMessagesListItem
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.utils.MqttUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class MqttMessagesViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) : BaseViewModel<Unit, MqttMessagesViewState>(application) {

    private var isFinishing: Boolean = false
    private var persistJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override suspend fun initialize(data: Unit): MqttMessagesViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        return MqttMessagesViewState(
            messageItems = MqttUtil.getMessagesFromBody(shortcut.bodyContent)
                .mapIndexed { index, message ->
                    MqttMessagesListItem(id = index, topic = message.topic, payload = String(message.payload))
                },
        )
    }

    fun onMessageMoved(messageId1: Int, messageId2: Int) = runAction {
        updateMessages(viewState.messageItems.swapped(messageId1, messageId2) { id })
    }

    fun onAddMessageButtonClicked() = runAction {
        updateDialogState(MqttMessagesDialogState.AddMessage)
    }

    fun onDialogConfirmed(key: String, value: String) = runAction {
        when (val dialogState = viewState.dialogState) {
            is MqttMessagesDialogState.AddMessage -> onAddMessageDialogConfirmed(key, value)
            is MqttMessagesDialogState.EditMessage -> onEditMessageDialogConfirmed(dialogState.id, key, value)
            else -> Unit
        }
    }

    private suspend fun ViewModelScope<MqttMessagesViewState>.onAddMessageDialogConfirmed(topic: String, payload: String) {
        updateDialogState(null)
        val messages = viewState.messageItems
        updateMessages(messages.plus(MqttMessagesListItem(messages.generateMessageId(), topic, payload)))
    }

    private fun List<MqttMessagesListItem>.generateMessageId(): Int =
        (maxOfOrNull { it.id } ?: 0) + 1

    private suspend fun ViewModelScope<MqttMessagesViewState>.onEditMessageDialogConfirmed(messageId: Int, topic: String, payload: String) {
        updateDialogState(null)
        updateMessages(
            viewState.messageItems
                .map { message ->
                    if (message.id == messageId) {
                        MqttMessagesListItem(messageId, topic, payload)
                    } else {
                        message
                    }
                },
        )
    }

    fun onRemoveMessageButtonClicked() = runAction {
        val messageId = (viewState.dialogState as? MqttMessagesDialogState.EditMessage)?.id ?: skipAction()
        updateDialogState(null)
        updateMessages(
            viewState.messageItems.filter { message ->
                message.id != messageId
            },
        )
    }

    fun onMessageClicked(id: Int) = runAction {
        viewState.messageItems
            .firstOrNull { message ->
                message.id == id
            }
            ?.let { message ->
                updateDialogState(
                    MqttMessagesDialogState.EditMessage(
                        id = message.id,
                        topic = message.topic,
                        payload = message.payload,
                    ),
                )
            }
    }

    fun onBackPressed() = runAction {
        if (isFinishing) {
            skipAction()
        }
        isFinishing = true
        persistJob?.join()
        closeScreen()
    }

    fun onDismissDialog() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateMessages(messages: List<MqttMessagesListItem>) {
        updateViewState {
            copy(
                messageItems = messages,
            )
        }
        schedulePersisting()
    }

    private fun schedulePersisting() {
        if (isFinishing) {
            return
        }
        persistJob = viewModelScope.launch(Dispatchers.Default) {
            delay(300.milliseconds)
            with(getCurrentViewState()) {
                temporaryShortcutRepository.setBodyContent(
                    MqttUtil.getBodyFromMessages(messageItems.map { MqttUtil.Message(it.topic, it.payload.toByteArray()) }),
                )
            }
        }
    }

    private suspend fun updateDialogState(dialogState: MqttMessagesDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}
