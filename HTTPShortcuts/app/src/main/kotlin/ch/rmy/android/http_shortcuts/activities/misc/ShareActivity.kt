package ch.rmy.android.http_shortcuts.activities.misc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.finishWithoutAnimation
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ShareActivity : BaseActivity() {

    private val realm by lazy {
        RealmFactory.getInstance().createRealm()
            .also {
                destroyer.own { it.close() }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isRealmAvailable) {
            return
        }

        val type = intent.type
        val action = intent.action
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (type == TYPE_TEXT && action == Intent.ACTION_SEND && text != null) {
            handleTextSharing(text, getFileUris())
        } else {
            handleFileSharing(getFileUris())
        }
    }

    private fun getFileUris(): List<Uri> =
        if (intent.action == Intent.ACTION_SEND) {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { listOf(it) }
        } else {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        } ?: emptyList()

    private fun handleTextSharing(text: String, fileUris: List<Uri>) {
        val variableLookup = VariableManager(Repository.getBase(realm)!!.variables)
        val variables = getTargetableVariablesForTextSharing()
        val variableIds = variables.map { it.id }.toSet()
        val shortcuts = getTargetableShortcutsForTextSharing(variableIds, variableLookup)

        val variableValues = variables.associate { variable -> variable.key to text }
        when (shortcuts.size) {
            0 -> showInstructions(R.string.error_not_suitable_shortcuts)
            1 -> {
                cacheFiles(fileUris) {
                    executeShortcut(shortcuts[0], variableValues = variableValues, files = it)
                }
                finishWithoutAnimation()
            }
            else -> cacheFiles(fileUris) {
                showShortcutSelection(shortcuts, variableValues = variableValues, files = it)
            }
        }
    }

    private fun getTargetableVariablesForTextSharing() =
        Repository.getBase(realm)!!.variables
            .filter { it.isShareText }
            .toSet()

    private fun getTargetableShortcutsForTextSharing(variableIds: Set<String>, variableLookup: VariableLookup): List<Shortcut> =
        Repository.getShortcuts(realm)
            .filter { hasShareVariable(it, variableIds, variableLookup) }

    private fun getTargetableShortcutsForFileSharing(): List<Shortcut> =
        Repository.getShortcuts(realm)
            .filter { hasFileParameter(it) || it.usesFileBody() }

    private fun handleFileSharing(fileUris: List<Uri>) {
        val shortcuts = getTargetableShortcutsForFileSharing()
        if (shortcuts.isEmpty()) {
            showInstructions(R.string.error_not_suitable_shortcuts)
            return
        }

        cacheFiles(fileUris) {
            proceedWithCachedShareFiles(shortcuts, it)
        }
    }

    private fun cacheFiles(fileUris: List<Uri>, action: (List<Uri>) -> Unit) {
        if (fileUris.isEmpty()) {
            action(emptyList())
            return
        }
        val context = applicationContext
        Single.fromCallable {
            cacheSharedFiles(context, fileUris)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                action,
                { e ->
                    showToast(R.string.error_generic)
                    logException(e)
                    finishWithoutAnimation()
                }
            )
            .attachTo(destroyer)
    }

    private fun proceedWithCachedShareFiles(shortcuts: List<Shortcut>, fileUris: List<Uri>) {
        when (shortcuts.size) {
            1 -> {
                executeShortcut(shortcuts[0], files = fileUris)
                finishWithoutAnimation()
            }
            else -> showShortcutSelection(shortcuts, files = fileUris)
        }
    }

    private fun executeShortcut(shortcut: Shortcut, variableValues: Map<String, String> = emptyMap(), files: List<Uri> = emptyList()) {
        ExecuteActivity.IntentBuilder(context, shortcut.id)
            .variableValues(variableValues)
            .files(files)
            .build()
            .startActivity(this)
    }

    private fun showInstructions(@StringRes text: Int) {
        DialogBuilder(context)
            .message(text)
            .dismissListener { finishWithoutAnimation() }
            .positive(R.string.dialog_ok)
            .showIfPossible()
    }

    private fun showShortcutSelection(shortcuts: List<Shortcut>, variableValues: Map<String, String> = emptyMap(), files: List<Uri> = emptyList()) {
        DialogBuilder(context)
            .mapFor(shortcuts) { builder, shortcut ->
                builder.item(name = shortcut.name, iconName = shortcut.iconName) {
                    executeShortcut(shortcut, variableValues, files)
                }
            }
            .dismissListener { finishWithoutAnimation() }
            .showIfPossible()
    }

    companion object {

        private const val TYPE_TEXT = "text/plain"

        private fun hasShareVariable(shortcut: Shortcut, variableIds: Set<String>, variableLookup: VariableLookup): Boolean {
            val variableIdsInShortcut = VariableResolver.extractVariableIds(shortcut, variableLookup)
            return variableIds.any { variableIdsInShortcut.contains(it) }
        }

        private fun hasFileParameter(shortcut: Shortcut): Boolean =
            shortcut.parameters.any { it.isFileParameter || it.isFilesParameter }

        private fun cacheSharedFiles(context: Context, fileUris: List<Uri>) =
            fileUris
                .map {
                    context.contentResolver.openInputStream(it)!!
                        .use {
                            val file = FileUtil.createCacheFile(context, createCacheFileName())
                            it.copyTo(context.contentResolver.openOutputStream(file)!!)
                            file
                        }
                }

        private fun createCacheFileName() = "shared_${UUIDUtils.newUUID()}"

    }

}
