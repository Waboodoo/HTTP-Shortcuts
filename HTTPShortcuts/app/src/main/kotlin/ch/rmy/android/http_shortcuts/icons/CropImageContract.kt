package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import com.yalantis.ucrop.UCrop
import java.io.File

class CropImageContract(
    private val title: String? = null,
    private val enforceSquare: Boolean = false,
    private val maxSize: Int? = null,
) : ActivityResultContract<CropImageContract.Input, CropImageContract.Result>() {
    override fun createIntent(context: Context, input: Input): Intent {
        val destinationFile = Uri.fromFile(File.createTempFile("crop_", null, context.cacheDir))
        return UCrop.of(input.imageUri, destinationFile)
            .withOptions(
                UCrop.Options().apply {
                    setToolbarTitle(title)
                    setCompressionQuality(100)
                    setCompressionFormat(input.compressFormat)
                    setFreeStyleCropEnabled(!enforceSquare)
                    if (input.circle) {
                        setCircleDimmedLayer(true)
                    }
                },
            )
            .runIf(enforceSquare) {
                withAspectRatio(1f, 1f)
            }
            .runIfNotNull(maxSize) {
                withMaxResultSize(it, it)
            }
            .getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Result =
        if (resultCode == AppCompatActivity.RESULT_OK && intent != null) {
            Result.Success(UCrop.getOutput(intent)!!.toFile())
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Result.Failure
        } else {
            Result.Canceled
        }

    data class Input(
        val imageUri: Uri,
        val compressFormat: CompressFormat = CompressFormat.PNG,
        val circle: Boolean = false,
    )

    sealed interface Result {
        data class Success(val imageFile: File) : Result
        data object Failure : Result
        data object Canceled : Result
    }
}
