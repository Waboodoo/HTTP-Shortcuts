package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import ch.rmy.android.http_shortcuts.activities.imageeditor.ImageEditorActivity
import ch.rmy.android.http_shortcuts.activities.imageeditor.ImageEditorActivity.Companion.getOutputUri
import java.io.File

class EditImageContract(
    private val title: String? = null,
    private val enforceSquare: Boolean = false,
    private val maxSize: Int? = null,
) : ActivityResultContract<EditImageContract.Input, EditImageContract.Result>() {
    override fun createIntent(context: Context, input: Input): Intent =
        ImageEditorActivity.IntentBuilder(input.imageUri).apply {
            if (title != null) {
                title(title)
            }
            if (enforceSquare) {
                squareAspectRatio()
            }
            if (input.circle) {
                circular()
            }
            if (maxSize != null) {
                maxSize(maxSize)
            }
            compressFormat(input.compressFormat)
        }
            .build(context)

    override fun parseResult(resultCode: Int, intent: Intent?): Result {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            return Result.Canceled
        }
        val file = intent?.takeIf { resultCode == AppCompatActivity.RESULT_OK }?.getOutputUri()?.toFile()
        return if (file != null) {
            Result.Success(file)
        } else {
            Result.Failure
        }
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
