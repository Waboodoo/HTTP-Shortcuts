package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import org.jdeferred2.Promise

abstract class BaseAction(val action: ActionDTO) {

    val id = action.id

    abstract fun getTitle(context: Context): CharSequence

    abstract fun getDescription(context: Context): CharSequence

    open fun perform(context: Context, shortcutId: Long, variableValues: Map<String, String>): Promise<Unit, Exception, Unit> =
            try {
                performBlocking(context, shortcutId, variableValues)
                PromiseUtils.resolve(Unit)
            } catch (e: Exception) {
                PromiseUtils.reject(e)
            }

    protected open fun performBlocking(context: Context, shortcutId: Long, variableValues: Map<String, String>) {

    }

}