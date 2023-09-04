package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable

interface ActionType {
    val type: String

    fun getActionRunnable(actionDTO: ActionData): ActionRunnable<*>

    fun getAlias(): ActionAlias? = null
}
