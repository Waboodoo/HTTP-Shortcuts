package ch.rmy.android.http_shortcuts.activities.editor.typepicker

import android.app.Application
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TypePickerViewModel
@Inject
constructor(
    application: Application,
) : BaseViewModel<TypePickerViewModel.InitData, Unit>(application) {

    override suspend fun initialize(data: InitData) = Unit

    fun onHelpButtonClicked() = runAction {
        logInfo("Shortcut creation help button clicked")
        openURL(ExternalURLs.SHORTCUTS_DOCUMENTATION)
    }

    fun onCreationDialogOptionSelected(executionType: ShortcutExecutionType) = runAction {
        logInfo("Preparing to open editor for creating shortcut of type $executionType")
        closeScreen()
        navigate(
            NavigationDestination.ShortcutEditor.buildRequest(
                categoryId = initData.categoryId,
                executionType = executionType,
            ),
        )
    }

    fun onCurlImportOptionSelected() = runAction {
        logInfo("curl import button clicked")
        closeScreen()
        navigate(NavigationDestination.CurlImport)
    }

    data class InitData(
        val categoryId: CategoryId,
    )
}
