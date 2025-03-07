package ch.rmy.android.http_shortcuts.plugin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import java.util.ArrayList
import kotlin.random.Random

class TaskerIntent(taskName: String) : Intent(ACTION_TASK) {

    init {
        setRandomData()
        putMetaExtras(taskName)
    }

    private fun setRandomData() {
        data = "$TASK_ID_SCHEME:${getRandomString()}".toUri()
    }

    private fun putMetaExtras(taskName: String) {
        putExtra(EXTRA_INTENT_VERSION_NUMBER, INTENT_VERSION_NUMBER)
        putExtra(EXTRA_TASK_NAME, taskName)
    }

    fun addLocalVariable(name: String, value: String) = also {
        val names: ArrayList<String>
        val values: ArrayList<String>
        if (hasExtra(EXTRA_VAR_NAMES_LIST)) {
            names = getStringArrayListExtra(EXTRA_VAR_NAMES_LIST)!!
            values = getStringArrayListExtra(EXTRA_VAR_VALUES_LIST)!!
        } else {
            names = ArrayList()
            values = ArrayList()
            putStringArrayListExtra(EXTRA_VAR_NAMES_LIST, names)
            putStringArrayListExtra(EXTRA_VAR_VALUES_LIST, values)
        }
        names.add(name)
        values.add(value)
    }

    companion object {
        private const val TASKER_PACKAGE = "net.dinglisch.android.tasker"
        private const val TASKER_PACKAGE_MARKET = TASKER_PACKAGE + "m"
        private const val ACTION_TASK = "$TASKER_PACKAGE.ACTION_TASK"
        private const val ACTION_TASK_SELECT = "$TASKER_PACKAGE.ACTION_TASK_SELECT"
        private const val EXTRA_TASK_NAME = "task_name"
        private const val EXTRA_VAR_NAMES_LIST = "varNames"
        private const val EXTRA_VAR_VALUES_LIST = "varValues"
        private const val TASK_ID_SCHEME = "id"
        private const val EXTRA_INTENT_VERSION_NUMBER = "version_number"
        private const val INTENT_VERSION_NUMBER = "1.1"

        fun isTaskerInstalled(context: Context): Boolean {
            try {
                context.packageManager.getPackageInfo(TASKER_PACKAGE, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
            }
            try {
                context.packageManager.getPackageInfo(TASKER_PACKAGE_MARKET, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
            }
            return false
        }

        fun getTaskSelectIntent() =
            Intent(ACTION_TASK_SELECT)
                .setFlags(
                    FLAG_ACTIVITY_NO_USER_ACTION or
                        FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        FLAG_ACTIVITY_NO_HISTORY,
                )

        internal fun getRandomString() =
            Random.nextLong().toString()
    }
}
