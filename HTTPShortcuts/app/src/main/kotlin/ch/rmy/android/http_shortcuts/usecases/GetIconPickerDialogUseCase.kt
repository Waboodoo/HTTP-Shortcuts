package ch.rmy.android.http_shortcuts.usecases

import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

class GetIconPickerDialogUseCase
@Inject
constructor() {

    operator fun invoke(
        title: Localizable = StringResLocalizable(R.string.change_icon),
        includeFaviconOption: Boolean = false,
        callbacks: Callbacks,
    ): DialogState =
        createDialogState(DIALOG_ID) {
            title(title)
                .item(R.string.choose_icon, action = callbacks::openBuiltInIconSelectionDialog)
                .item(R.string.choose_image, action = callbacks::openCustomIconPicker)
                .runIf(includeFaviconOption) {
                    item(R.string.choose_page_favicon, action = callbacks::fetchFavicon)
                }
                .build()
        }

    interface Callbacks {
        fun openBuiltInIconSelectionDialog()

        fun openCustomIconPicker()

        fun fetchFavicon() {}
    }

    companion object {
        private const val DIALOG_ID = "icon-type-picker"
    }
}
