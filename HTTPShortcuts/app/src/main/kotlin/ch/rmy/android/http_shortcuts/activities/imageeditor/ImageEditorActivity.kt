package ch.rmy.android.http_shortcuts.activities.imageeditor

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import com.canhub.cropper.CropException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageEditorActivity : BaseComposeActivity() {
    @Composable
    override fun Content() {
        val imageUri = intent.getParcelable<Uri>(EXTRA_INPUT_URI)
        if (imageUri == null) {
            LaunchedEffect(Unit) {
                finish()
            }
            return
        }

        ImageEditorScreen(
            title = intent.getStringExtra(EXTRA_TITLE) ?: "",
            imageUri = imageUri,
            squareAspectRatio = intent.getBooleanExtra(EXTRA_SQUARE_ASPECT_RATIO, false),
            circular = intent.getBooleanExtra(EXTRA_CIRCULAR, false),
            compressFormat = intent.getStringExtra(EXTRA_COMPRESS_FORMAT)
                ?.let { Bitmap.CompressFormat.valueOf(it) }
                ?: Bitmap.CompressFormat.PNG,
            maxSize = intent.getIntExtra(EXTRA_MAX_SIZE, -1).takeUnless { it == -1 },
            onDone = { imageUri ->
                setResult(RESULT_OK, Intent().putExtra(EXTRA_OUTPUT_URI, imageUri))
                finish()
            },
            onError = { e ->
                if (e != null && e !is CropException) {
                    logException(e)
                }
                if (e !is CropException.Cancellation) {
                    setResult(2)
                }
                finish()
            },
        )
    }

    class IntentBuilder(
        inputUri: Uri,
    ) : BaseIntentBuilder(ImageEditorActivity::class) {
        init {
            intent.putExtra(EXTRA_INPUT_URI, inputUri)
        }

        fun title(title: String?) = also {
            intent.putExtra(EXTRA_TITLE, title)
        }

        fun squareAspectRatio() = also {
            intent.putExtra(EXTRA_SQUARE_ASPECT_RATIO, true)
        }

        fun circular() = also {
            intent.putExtra(EXTRA_CIRCULAR, true)
        }

        fun compressFormat(compressFormat: Bitmap.CompressFormat) = also {
            intent.putExtra(EXTRA_COMPRESS_FORMAT, compressFormat.name)
        }

        fun maxSize(maxSize: Int) = also {
            intent.putExtra(EXTRA_MAX_SIZE, maxSize)
        }
    }

    companion object {
        private const val EXTRA_INPUT_URI = "input_uri"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_SQUARE_ASPECT_RATIO = "square_aspect_ratio"
        private const val EXTRA_CIRCULAR = "circular"
        private const val EXTRA_COMPRESS_FORMAT = "compress_format"
        private const val EXTRA_MAX_SIZE = "max_size"

        private const val EXTRA_OUTPUT_URI = "output_uri"

        fun Intent.getOutputUri(): Uri? =
            getParcelable(EXTRA_OUTPUT_URI)
    }
}
