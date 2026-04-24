package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.app.BackgroundServiceStartNotAllowedException
import android.content.Intent
import android.os.Build
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.extensions.userError
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import javax.inject.Inject

class TermuxAction
@Inject
constructor(
    private val permissionManager: PermissionManager,
    private val activityProvider: ActivityProvider,
) : Action<TermuxAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val hadPermission = permissionManager.hasTermuxPermission()
        requestTermuxPermissionIfNeeded()
        if (!hadPermission) {
            try {
                executionContext.dialogHandle.showDialog(
                    ExecuteDialogState.GenericMessage(
                        message = StringResLocalizable(R.string.termux_setup_instructions),
                    ),
                )
            } catch (_: DialogCancellationException) {
                // Ignore cancellation and continue
            }
        }

        val intent = Intent().apply {
            setClassName("com.termux", "com.termux.app.RunCommandService")
            setAction("com.termux.RUN_COMMAND")
            putExtra("com.termux.RUN_COMMAND_PATH", commandPath)
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arguments.toTypedArray())
            workingDir?.let {
                putExtra("com.termux.RUN_COMMAND_WORKDIR", it)
            }
            resultDir?.let {
                putExtra("om.termux.RUN_COMMAND_RESULT_DIRECTORY", it)
            }
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", background)
            putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", sessionAction)
        }
        activityProvider.withActivity { activity ->
            try {
                activity.startService(intent)
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is BackgroundServiceStartNotAllowedException) {
                    userError {
                        getString(R.string.error_termux_not_running)
                    }
                } else if (e is IllegalStateException && e.message?.contains("app is in background") == true) {
                    userError {
                        getString(R.string.error_termux_cannot_start_from_background)
                    }
                } else {
                    throw e
                }
            }
        }
    }

    private suspend fun requestTermuxPermissionIfNeeded() {
        val granted = permissionManager.requestTermuxPermissionIfNeeded()
        if (!granted) {
            throw ActionException {
                getString(R.string.termux_permission_not_granted)
            }
        }
    }

    data class Params(
        val commandPath: String,
        val arguments: List<String>,
        val workingDir: String? = null,
        val resultDir: String? = null,
        val background: Boolean = false,
        val sessionAction: String = "0",
    )
}
