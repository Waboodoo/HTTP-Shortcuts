package ch.rmy.android.http_shortcuts.usecases

import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R

class GetIconPickerDialogUseCase {

    operator fun invoke(includeFaviconOption: Boolean = false, callbacks: Callbacks): DialogState =
        DialogState.create(DIALOG_ID) {
            title(R.string.change_icon)
                .item(R.string.choose_icon, action = callbacks::openBuiltInIconSelectionDialog)
                .item(R.string.choose_image, action = callbacks::openCustomIconPicker)
                .mapIf(includeFaviconOption) {
                    item(R.string.choose_page_favicon, action = callbacks::fetchFavicon)
                }
                .item(R.string.choose_ipack_icon, action = callbacks::openIpackPicker)
                .build()
        }

    interface Callbacks {
        fun openBuiltInIconSelectionDialog()

        fun openCustomIconPicker()

        fun fetchFavicon() {}

        fun openIpackPicker()
    }

    companion object {
        private const val DIALOG_ID = "icon-type-picker"
    }
}
