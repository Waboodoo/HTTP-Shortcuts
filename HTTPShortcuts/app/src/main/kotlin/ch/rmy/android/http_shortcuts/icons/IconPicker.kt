package ch.rmy.android.http_shortcuts.icons

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.utils.Destroyer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.IpackUtil

class IconPicker(
    private val activity: BaseActivity,
    private val iconSelected: (ShortcutIcon) -> Unit,
) {

    private val context: Context
        get() = activity

    private val destroyer: Destroyer
        get() = activity.destroyer

    fun openIconSelectionDialog() {
        DialogBuilder(context)
            .title(R.string.change_icon)
            .item(R.string.choose_icon, action = ::openBuiltInIconSelectionDialog)
            .item(R.string.choose_image, action = ::openCustomIconPicker)
            .item(R.string.choose_ipack_icon, action = ::openIpackPicker)
            .showIfPossible()
    }

    private fun openBuiltInIconSelectionDialog() {
        BuiltInIconSelector(context)
            .show()
            .subscribe { icon ->
                iconSelected(icon)
            }
            .attachTo(destroyer)
    }

    private fun openCustomIconPicker() {
        IconPickerActivity.IntentBuilder()
            .startActivity(activity, REQUEST_CUSTOM_ICON)
    }

    private fun openIpackPicker() {
        IpackUtil.getIpackIntent(context)
            .startActivity(activity, REQUEST_SELECT_IPACK_ICON)
    }

    fun handleResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            REQUEST_CUSTOM_ICON -> {
                if (intent != null) {
                    IconPickerActivity.Result.getIcon(intent)
                        ?.let { icon ->
                            iconSelected(icon)
                        }
                }
            }
            REQUEST_SELECT_IPACK_ICON -> {
                if (resultCode == RESULT_OK && intent != null) {
                    iconSelected(ShortcutIcon.ExternalResourceIcon(IpackUtil.getIpackUri(intent)))
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CUSTOM_ICON = 1
        private const val REQUEST_SELECT_IPACK_ICON = 2
    }
}
