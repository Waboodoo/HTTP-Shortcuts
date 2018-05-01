package ch.rmy.android.http_shortcuts.actions.types

import ch.rmy.android.http_shortcuts.actions.ActionDTO

object ActionFactory {

    // TODO: Allow to disable/hide actions that are not available (e.g. because of missing hardware or OS features

    fun fromDTO(actionDTO: ActionDTO) = when (actionDTO.type) {
        ToastAction.TYPE -> ToastAction(actionDTO)
        VibrateAction.TYPE -> VibrateAction(actionDTO)
        else -> UnknownAction(actionDTO)
    }

}