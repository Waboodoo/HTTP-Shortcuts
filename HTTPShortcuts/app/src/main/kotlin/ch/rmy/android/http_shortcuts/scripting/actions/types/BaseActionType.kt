package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

abstract class BaseActionType {

    abstract val type: String

    abstract fun fromDTO(actionDTO: ActionDTO): BaseAction

    open fun getAlias(): ActionAlias? = null
}
