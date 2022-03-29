package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.IconUtil
import com.yalantis.ucrop.UCrop
import java.io.File

object CropIconContract : ActivityResultContract<Uri, CropIconContract.Result>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        val iconSize = IconUtil.getIconSize(context)
        val destinationFile = Uri.fromFile(File.createTempFile("crop_", null, context.cacheDir))
        return UCrop.of(input, destinationFile)
            .withOptions(
                UCrop.Options().apply {
                    setToolbarTitle(context.getString(R.string.title_edit_custom_icon))
                    setCompressionQuality(100)
                    setCompressionFormat(Bitmap.CompressFormat.PNG)
                }
            )
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(iconSize, iconSize)
            .getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Result =
        if (resultCode == AppCompatActivity.RESULT_OK && intent != null) {
            Result.Success(UCrop.getOutput(intent)!!)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Result.Failure
        } else {
            Result.Canceled
        }

    sealed interface Result {
        data class Success(val iconUri: Uri) : Result
        object Failure : Result
        object Canceled : Result
    }
}
