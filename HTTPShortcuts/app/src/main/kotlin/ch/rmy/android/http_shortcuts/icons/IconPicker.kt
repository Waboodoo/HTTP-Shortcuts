package ch.rmy.android.http_shortcuts.icons

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.utils.IpackUtil
import com.yalantis.ucrop.UCrop
import io.reactivex.Completable
import java.io.File

class IconPicker(
    private val activity: BaseActivity,
    private val iconSelected: (ShortcutIcon) -> Completable,
) {

    private val context: Context
        get() = activity

    private val destroyer: Destroyer
        get() = activity.destroyer

    fun openIconSelectionDialog() {
        DialogBuilder(context)
            .title(R.string.change_icon)
            .item(R.string.choose_icon, action = ::openBuiltInIconSelectionDialog)
            .item(R.string.choose_image, action = ::openImagePicker)
            .item(R.string.choose_previously_used_image, action = ::openPreviouslyUsedCustomInIconSelectionDialog)
            .item(R.string.choose_ipack_icon, action = ::openIpackPicker)
            .showIfPossible()
    }

    private fun openBuiltInIconSelectionDialog() {
        BuiltInIconSelector(context)
            .show()
            .flatMapCompletable { icon ->
                iconSelected(icon)
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun openImagePicker() {
        try {
            FilePickerUtil.createIntent(type = "image/*")
                .startActivity(activity, REQUEST_SELECT_IMAGE)
        } catch (e: ActivityNotFoundException) {
            activity.showSnackbar(R.string.error_not_supported)
        }
    }

    private fun openPreviouslyUsedCustomInIconSelectionDialog() {
        CustomIconSelector(context)
            .show()
            .flatMapCompletable { icon ->
                iconSelected(icon)
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun openIpackPicker() {
        IpackUtil.getIpackIntent(context)
            .startActivity(activity, REQUEST_SELECT_IPACK_ICON)
    }

    fun handleResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_IMAGE -> {
                if (resultCode == RESULT_OK && intent?.data != null) {
                    val iconSize = IconUtil.getIconSize(context)
                    UCrop.of(intent.data!!, createNewIconFile())
                        .withOptions(UCrop.Options().apply {
                            setToolbarTitle(activity.getString(R.string.title_edit_custom_icon))
                            setCompressionQuality(100)
                            setCompressionFormat(Bitmap.CompressFormat.PNG)
                        })
                        .withAspectRatio(1f, 1f)
                        .withMaxResultSize(iconSize, iconSize)
                        .start(activity, REQUEST_CROP_IMAGE)
                }
            }
            REQUEST_CROP_IMAGE -> {
                try {
                    if (resultCode == RESULT_OK && intent != null) {
                        updateIcon(ShortcutIcon.CustomIcon(UCrop.getOutput(intent)!!.lastPathSegment!!))
                    } else if (resultCode == UCrop.RESULT_ERROR) {
                        activity.showSnackbar(R.string.error_set_image, long = true)
                    }
                } catch (e: Exception) {
                    logException(e)
                    activity.showSnackbar(R.string.error_set_image, long = true)
                }
            }
            REQUEST_SELECT_IPACK_ICON -> {
                if (resultCode == RESULT_OK && intent != null) {
                    updateIcon(ShortcutIcon.ExternalResourceIcon(IpackUtil.getIpackUri(intent)))
                }
            }
        }
    }

    private fun createNewIconFile(): Uri =
        Uri.fromFile(File(context.filesDir, IconUtil.generateCustomIconName()))

    private fun updateIcon(icon: ShortcutIcon) {
        iconSelected(icon)
            .subscribe()
            .attachTo(destroyer)
    }

    companion object {
        private const val REQUEST_SELECT_IMAGE = 1
        private const val REQUEST_CROP_IMAGE = 2
        private const val REQUEST_SELECT_IPACK_ICON = 3
    }

}