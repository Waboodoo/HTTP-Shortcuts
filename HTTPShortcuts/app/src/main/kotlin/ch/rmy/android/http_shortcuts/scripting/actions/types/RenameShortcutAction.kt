package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import io.reactivex.Completable
import javax.inject.Inject

class RenameShortcutAction(private val name: String, private val shortcutNameOrId: ShortcutNameOrId?) : BaseAction() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var widgetManager: WidgetManager

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun execute(executionContext: ExecutionContext): Completable =
        renameShortcut(
            executionContext.context,
            this.shortcutNameOrId ?: executionContext.shortcutId,
            executionContext.variableManager,
        )

    private fun renameShortcut(context: Context, shortcutNameOrId: ShortcutNameOrId, variableManager: VariableManager): Completable {
        val newName = Variables.rawPlaceholdersToResolvedValues(name, variableManager.getVariableValuesByIds())
            .trim()
            .truncate(ShortcutModel.NAME_MAX_LENGTH)
        if (newName.isEmpty()) {
            return Completable.complete()
        }

        return shortcutRepository.getShortcutByNameOrId(shortcutNameOrId)
            .flatMapCompletable { shortcut ->
                shortcutRepository.setName(shortcut.id, newName)
                    .andThen(
                        Completable.fromAction {
                            val launcherShortcutManager = LauncherShortcutManager(context)
                            if (launcherShortcutManager.supportsPinning()) {
                                launcherShortcutManager.updatePinnedShortcut(
                                    shortcutId = shortcut.id,
                                    shortcutName = newName,
                                    shortcutIcon = shortcut.icon,
                                )
                            }
                        }
                    )
                    .andThen(widgetManager.updateWidgets(context, shortcut.id))
            }
            .onErrorResumeNext { error ->
                if (error is NoSuchElementException) {
                    Completable
                        .error(
                            ActionException {
                                it.getString(R.string.error_shortcut_not_found_for_renaming, shortcutNameOrId)
                            }
                        )
                } else {
                    Completable.error(error)
                }
            }
    }
}
