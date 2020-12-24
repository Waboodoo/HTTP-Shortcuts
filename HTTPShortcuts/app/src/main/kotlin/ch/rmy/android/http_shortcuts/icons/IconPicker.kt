package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.utils.IpackUtil
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import com.theartofdev.edmodo.cropper.CropImage
import io.reactivex.Completable
import java.io.File
import java.io.FileOutputStream

class IconPicker(
    private val activity: BaseActivity,
    private val iconSelected: (String) -> Completable,
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
            .item(R.string.choose_ipack_icon, action = ::openIpackPicker)
            .showIfPossible()
    }

    private fun openBuiltInIconSelectionDialog() {
        IconSelector(context)
            .show()
            .flatMapCompletable { iconName ->
                iconSelected(iconName)
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun openImagePicker() {
        val iconSize = IconUtil.getIconSize(context)
        CropImage.activity()
            .setCropMenuCropButtonIcon(R.drawable.ic_save)
            .setCropMenuCropButtonTitle(context.getString(R.string.button_apply_icon))
            .setAspectRatio(1, 1)
            .setRequestedSize(iconSize, iconSize)
            .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
            .setMultiTouchEnabled(true)
            .start(activity)
    }

    private fun openIpackPicker() {
        IpackUtil.getIpackIntent(context)
            .startActivity(activity, REQUEST_SELECT_IPACK_ICON)
    }

    fun handleResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                try {
                    val result = CropImage.getActivityResult(intent)
                    if (resultCode == AppCompatActivity.RESULT_OK) {
                        val iconName = storeImage(result.uri)
                        updateIconName(iconName)
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        activity.showSnackbar(R.string.error_set_image, long = true)
                    }
                } catch (e: Exception) {
                    activity.showSnackbar(R.string.error_set_image, long = true)
                }
            }
            REQUEST_SELECT_IPACK_ICON -> {
                if (resultCode == AppCompatActivity.RESULT_OK && intent != null) {
                    updateIconName(IpackUtil.getIpackUri(intent).toString())
                }
            }
        }
    }

    private fun storeImage(uri: Uri): String {
        val bitmap = context.contentResolver.openInputStream(uri)!!.use { stream ->
            BitmapFactory.decodeStream(stream)
        }

        val fileName = "custom-icon_${UUIDUtils.newUUID()}.png"
        FileOutputStream(File(context.filesDir, fileName)).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
        }
        bitmap.recycle()

        return fileName
    }

    private fun updateIconName(iconName: String) {
        iconSelected(iconName)
            .subscribe()
            .attachTo(destroyer)
    }

    companion object {
        private const val REQUEST_SELECT_IPACK_ICON = 3
    }

}