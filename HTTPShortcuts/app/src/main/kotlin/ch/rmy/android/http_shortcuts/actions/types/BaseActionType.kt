package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.ActionAlias

abstract class BaseActionType(protected val context: Context) {

    abstract val type: String

    abstract val title: CharSequence

    abstract fun fromDTO(actionDTO: ActionDTO): BaseAction

    open fun getAlias(): ActionAlias? = null

}