package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.extensions.ifTrue
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.extensions.toListOfObjects
import ch.rmy.android.http_shortcuts.extensions.toListOfStrings
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import org.json.JSONObject

class SendIntentAction(private val jsonData: String) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.fromAction {

            val parameters = JSONObject(jsonData)
            val intent = constructIntent(parameters)

            try {
                when (parameters.optString(KEY_TYPE).toLowerCase()) {
                    TYPE_ACTIVITY -> {
                        executionContext.context.startActivity(intent)
                    }
                    TYPE_SERVICE -> {
                        executionContext.context.startService(intent)
                    }
                    else -> {
                        executionContext.context.sendBroadcast(intent)
                    }
                }
            } catch (e: Exception) {
                if (e !is ActivityNotFoundException) {
                    logException(e)
                }
                throw ActionException { context ->
                    // TODO: Better explain and localize the error message
                    context.getString(R.string.error_action_type_send_intent_failed) + ": " + e.message
                }
            }
        }

    companion object {

        private const val KEY_TYPE = "type"
        private const val KEY_ACTION = "action"
        private const val KEY_CATEGORY = "category"
        private const val KEY_CATEGORIES = "categories"
        private const val KEY_DATA_URI = "dataUri"
        private const val KEY_DATA_TYPE = "dataType"
        private const val KEY_CLASS_NAME = "className"
        private const val KEY_PACKAGE_NAME = "packageName"
        private const val KEY_EXTRAS = "extras"
        private const val KEY_FLAG_CLEAR_TASK = "clearTask"
        private const val KEY_FLAG_EXCLUDE_FROM_RECENTS = "excludeFromRecents"
        private const val KEY_FLAG_NEW_TASK = "newTask"
        private const val KEY_FLAG_NO_HISTORY = "noHistory"

        private const val KEY_EXTRA_NAME = "name"
        private const val KEY_EXTRA_VALUE = "value"
        private const val KEY_EXTRA_TYPE = "type"

        private const val EXTRA_TYPE_STRING = "string"
        private const val EXTRA_TYPE_BOOLEAN = "boolean"
        private const val EXTRA_TYPE_INT = "int"
        private const val EXTRA_TYPE_LONG = "long"
        private const val EXTRA_TYPE_DOUBLE = "double"
        private const val EXTRA_TYPE_FLOAT = "float"

        private const val TYPE_ACTIVITY = "activity"
        private const val TYPE_SERVICE = "service"
        private const val TYPE_BROADCAST = "broadcast"

        fun constructIntent(parameters: JSONObject): Intent =
            Intent(parameters.optString(KEY_ACTION)).apply {
                parameters.optString(KEY_DATA_URI)
                    .takeUnlessEmpty()
                    ?.let { Uri.parse(it) }
                    ?.let { dataUri ->
                        parameters.optString(KEY_DATA_TYPE)
                            .takeUnlessEmpty()
                            ?.let { dataType ->
                                setDataAndType(dataUri, dataType)
                            }
                            ?: run {
                                setData(dataUri)
                            }
                    }
                parameters.optString(KEY_CATEGORY)
                    .takeUnlessEmpty()
                    ?.let { category ->
                        addCategory(category)
                    }
                parameters.optJSONArray(KEY_CATEGORIES)
                    ?.toListOfStrings()
                    ?.forEach { category ->
                        addCategory(category)
                    }
                parameters.optString(KEY_PACKAGE_NAME)
                    .takeUnlessEmpty()
                    ?.let { packageName ->
                        parameters.optString(KEY_CLASS_NAME)
                            .takeUnlessEmpty()
                            ?.let { className ->
                                setClassName(packageName, className)
                            }
                            ?: run {
                                `package` = packageName
                            }
                    }
                parameters.optBoolean(KEY_FLAG_CLEAR_TASK)
                    .ifTrue {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                parameters.optBoolean(KEY_FLAG_EXCLUDE_FROM_RECENTS)
                    .ifTrue {
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                parameters.optBoolean(KEY_FLAG_NEW_TASK)
                    .ifTrue {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                parameters.optBoolean(KEY_FLAG_NO_HISTORY)
                    .ifTrue {
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    }
                parameters.optJSONArray(KEY_EXTRAS)
                    ?.toListOfObjects()
                    ?.forEach { extra ->
                        val name = extra.optString(KEY_EXTRA_NAME)
                            .takeUnlessEmpty()
                            ?: return@forEach
                        when (extra.optString(KEY_EXTRA_TYPE)) {
                            EXTRA_TYPE_BOOLEAN -> {
                                putExtra(name, extra.optBoolean(KEY_EXTRA_VALUE))
                            }
                            EXTRA_TYPE_FLOAT -> {
                                putExtra(name, extra.optDouble(KEY_EXTRA_VALUE).toFloat())
                            }
                            EXTRA_TYPE_DOUBLE -> {
                            putExtra(name, extra.optDouble(KEY_EXTRA_VALUE))
                        }
                            EXTRA_TYPE_INT -> {
                                putExtra(name, extra.optInt(KEY_EXTRA_VALUE))
                            }
                            EXTRA_TYPE_LONG -> {
                                putExtra(name, extra.optLong(KEY_EXTRA_VALUE))
                            }
                            else -> {
                                putExtra(name, extra.optString(KEY_EXTRA_VALUE))
                            }
                        }
                    }
            }

    }

}