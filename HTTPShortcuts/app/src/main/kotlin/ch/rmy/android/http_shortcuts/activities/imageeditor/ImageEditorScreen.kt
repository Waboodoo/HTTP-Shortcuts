package ch.rmy.android.http_shortcuts.activities.imageeditor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.LoadingIndicator
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.io.File

@Composable
fun ImageEditorScreen(
    title: String,
    imageUri: Uri,
    squareAspectRatio: Boolean,
    circular: Boolean,
    compressFormat: Bitmap.CompressFormat,
    maxSize: Int?,
    onDone: (Uri) -> Unit,
    onError: (Exception?) -> Unit,
) {
    var isLoading by remember {
        mutableStateOf(true)
    }
    var isExporting by remember {
        mutableStateOf(false)
    }
    val rootView = rememberCropImageView { context, isRestore ->
        LayoutInflater.from(context).inflate(R.layout.image_editor, null)
            .apply {
                layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                val cropImageView = findViewById<CropImageView>(R.id.cropImageView)

                cropImageView.setOnSetImageUriCompleteListener { _, _, e ->
                    isLoading = false
                    if (e != null) {
                        onError(e)
                    }
                }

                if (!isRestore) {
                    cropImageView.setImageCropOptions(
                        CropImageOptions(
                            fixAspectRatio = squareAspectRatio,
                            cropShape = if (circular) CropImageView.CropShape.OVAL else CropImageView.CropShape.RECTANGLE,
                            outputRequestWidth = maxSize ?: 0,
                            outputRequestHeight = maxSize ?: 0,
                            outputCompressFormat = compressFormat,
                            showProgressBar = false,
                            outputCompressQuality = 95,
                        ),
                    )

                    cropImageView.setImageUriAsync(imageUri)
                }
            }
    }

    val context = LocalContext.current
    SimpleScaffold(
        viewState = Unit,
        title = title,
        backButton = BackButton.CROSS,
        actions = {
            ToolbarIcon(
                painterResource(R.drawable.outline_rotate_90_degrees_cw_24),
                enabled = !isLoading && !isExporting,
                contentDescription = stringResource(R.string.accessibility_rotate_cw),
                onClick = {
                    val cropImageView = rootView.findViewById<CropImageView>(R.id.cropImageView)
                    cropImageView.rotateImage(90)
                },
            )
            ToolbarIcon(
                painterResource(R.drawable.outline_check_24),
                enabled = !isLoading && !isExporting,
                contentDescription = stringResource(R.string.dialog_ok),
                onClick = {
                    isExporting = true
                    val cropImageView = rootView.findViewById<CropImageView>(R.id.cropImageView)
                    cropImageView.setOnCropImageCompleteListener { _, result ->
                        isExporting = false
                        if (result.error != null || result.uriContent == null) {
                            onError(result.error)
                        } else {
                            onDone(result.uriContent!!)
                        }
                    }
                    cropImageView.croppedImageAsync(
                        saveCompressFormat = compressFormat,
                        saveCompressQuality = 95,
                        reqWidth = maxSize ?: 0,
                        reqHeight = maxSize ?: 0,
                        customOutputUri = getTempFileUri(context, compressFormat),
                    )
                },
            )
        },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                rootView
            },
            onReset = {
                // do nothing
            },
            onRelease = {
                // do nothing
            },
            update = {
                // do nothing
            },
        )
        if (isLoading || isExporting) {
            LoadingIndicator()
        }
    }
}

private fun getTempFileUri(context: Context, format: Bitmap.CompressFormat): Uri {
    val extension = when (format) {
        Bitmap.CompressFormat.JPEG -> "jpg"
        Bitmap.CompressFormat.PNG -> "png"
        else -> "webp"
    }
    return FileUtil.getUriFromFile(
        context,
        File(context.cacheDir, "image-editor-${newUUID()}.$extension"),
    )
}

@Composable
private fun <T : View> rememberCropImageView(init: (Context, isRestore: Boolean) -> T): T {
    val context = LocalContext.current
    val webView = rememberSaveable(
        saver = object : Saver<T, Parcelable> {
            override fun restore(value: Parcelable): T =
                init(context, true)
                    .apply {
                        val cropImageView = findViewById<CropImageView>(R.id.cropImageView)
                        cropImageView.onRestoreInstanceState(value)
                    }

            override fun SaverScope.save(value: T): Parcelable? =
                value.findViewById<CropImageView>(R.id.cropImageView).onSaveInstanceState()
        },
    ) {
        init(context, false)
    }
    return webView
}
