package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs

interface ActionType {
    val type: String

    fun getActionRunnable(args: JsFunctionArgs): ActionRunnable<*>

    fun getAlias(): ActionAlias? = null
}
