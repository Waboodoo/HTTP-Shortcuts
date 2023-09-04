package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.FileUriExposedException
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.extensions.toListOfObjects
import ch.rmy.android.http_shortcuts.extensions.toListOfStrings
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class SendIntentAction
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) : Action<SendIntentAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val parameters = JSONObject(jsonData)
        val intent = constructIntent(parameters)
        withContext(Dispatchers.Main) {
            val activity = activityProvider.getActivity()
            try {
                when (parameters.optString(KEY_TYPE).lowercase()) {
                    TYPE_ACTIVITY -> {
                        activity.startActivity(intent)
                    }
                    TYPE_SERVICE -> {
                        activity.startService(intent)
                    }
                    else -> {
                        activity.sendBroadcast(intent)
                    }
                }
            } catch (e: Exception) {
                if (shouldLogException(e)) {
                    logException(e)
                }
                throw ActionException {
                    getString(R.string.error_action_type_send_intent_failed, e.message)
                }
            }
        }
    }

    data class Params(
        val jsonData: String,
    )

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

        private const val EXTRA_TYPE_BOOLEAN = "boolean"
        private const val EXTRA_TYPE_INT = "int"
        private const val EXTRA_TYPE_LONG = "long"
        private const val EXTRA_TYPE_DOUBLE = "double"
        private const val EXTRA_TYPE_FLOAT = "float"

        private const val TYPE_ACTIVITY = "activity"
        private const val TYPE_SERVICE = "service"

        fun constructIntent(parameters: JSONObject): Intent =
            Intent(parameters.optString(KEY_ACTION)).apply {
                val data = parameters.optString(KEY_DATA_URI)
                    .takeUnlessEmpty()
                    ?.toUri()
                val type = parameters.optString(KEY_DATA_TYPE)
                    .takeUnlessEmpty()
                when {
                    data != null && type != null -> {
                        setDataAndType(data, type)
                    }
                    data != null -> {
                        setData(data)
                    }
                    type != null -> {
                        setType(type)
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
                if (parameters.optBoolean(KEY_FLAG_CLEAR_TASK)) {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                if (parameters.optBoolean(KEY_FLAG_EXCLUDE_FROM_RECENTS)) {
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }
                if (parameters.optBoolean(KEY_FLAG_NEW_TASK)) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (parameters.optBoolean(KEY_FLAG_NO_HISTORY)) {
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

        internal fun shouldLogException(e: Exception): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && e is FileUriExposedException) {
                false
            } else {
                when (e) {
                    is ActivityNotFoundException,
                    is SecurityException,
                    -> false
                    else -> true
                }
            }
    }
}
