package ch.rmy.android.http_shortcuts.activities.settings.importexport.usecases

import android.app.Dialog
import android.content.Context
import ch.rmy.android.framework.extensions.addOrRemove
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.callbacks.onShow
import io.reactivex.Single
import javax.inject.Inject

class GetShortcutSelectionDialogUseCase
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
) {

    operator fun invoke(onConfirm: (Collection<ShortcutId>?) -> Unit): Single<DialogState> =
        shortcutRepository.getShortcuts()
            .map { shortcuts ->
                object : DialogState {
                    override val id = "select-shortcuts-for-export"

                    private val selectedShortcutIds = shortcuts.map { it.id }.toMutableSet()

                    private var onSelectionChanged: (() -> Unit)? = null

                    override fun createDialog(context: Context, viewModel: WithDialog?): Dialog =
                        DialogBuilder(context)
                            .title(R.string.dialog_title_select_shortcuts_for_export)
                            .runFor(shortcuts) { shortcut ->
                                checkBoxItem(
                                    name = shortcut.name,
                                    shortcutIcon = shortcut.icon,
                                    checked = { shortcut.id in selectedShortcutIds },
                                ) { isChecked ->
                                    selectedShortcutIds.addOrRemove(shortcut.id, isChecked)
                                    onSelectionChanged?.invoke()
                                }
                            }
                            .positive(R.string.dialog_button_export) {
                                onConfirm(selectedShortcutIds.takeUnless { it.size == shortcuts.size })
                            }
                            .negative(R.string.dialog_cancel)
                            .dismissListener {
                                onSelectionChanged = null
                                viewModel?.onDialogDismissed(this)
                            }
                            .build()
                            .onShow { dialog ->
                                val okButton = dialog.getActionButton(WhichButton.POSITIVE)
                                onSelectionChanged = {
                                    okButton.isEnabled = selectedShortcutIds.isNotEmpty()
                                }
                                onSelectionChanged?.invoke()
                            }
                }
            }
}
