package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import org.jdeferred2.Promise

abstract class BaseAction(val id: String, val actionType: BaseActionType, val data: Map<String, String>) {

    fun toDTO() = ActionDTO(
            id = id,
            type = actionType.type,
            data = data
    )

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