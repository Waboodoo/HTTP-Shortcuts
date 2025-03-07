package ch.rmy.android.framework.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.framework.utils.FileUtil.createCacheFile
import ch.rmy.android.framework.utils.FileUtil.getCacheFileIfValid

object FilePickerUtil {

    object PickFile : ActivityResultContract<String?, Uri?>() {
        override fun createIntent(context: Context, input: String?): Intent =
            createIntent(type = input)

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
            if (resultCode == AppCompatActivity.RESULT_OK) {
                intent?.data
            } else {
                null
            }
    }

    object PickFiles : ActivityResultContract<Boolean, List<Uri>?>() {
        override fun createIntent(context: Context, input: Boolean): Intent =
            createIntent(multiple = input)

        override fun parseResult(resultCode: Int, intent: Intent?): List<Uri>? =
            if (resultCode == AppCompatActivity.RESULT_OK) {
                intent?.let(::extractUris)
            } else {
                null
            }

        private fun extractUris(intent: Intent): List<Uri>? =
            intent.clipData
                ?.let { data ->
                    buildList {
                        for (i in 0 until data.itemCount) {
                            val uri = data.getItemAt(i).uri
                            add(uri)
                        }
                    }
                }
                ?: intent.data?.let { listOf(it) }
    }

    internal fun createIntent(multiple: Boolean = false, type: String? = null): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .apply {
                this.type = type ?: "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
                addCategory(Intent.CATEGORY_OPENABLE)
            }

    object OpenCamera : ActivityResultContract<Unit, ((Context) -> Uri?)>() {

        private const val IMAGE_FILE_NAME = "camera_image.jpg"

        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, createCacheFile(context, IMAGE_FILE_NAME, deleteIfExists = true))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

        override fun parseResult(resultCode: Int, intent: Intent?): ((Context) -> Uri?) =
            { context: Context ->
                getCacheFileIfValid(context, IMAGE_FILE_NAME)
            }
    }
}
