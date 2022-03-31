package ch.rmy.android.http_shortcuts.activities.editor.authentication.usecases

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.framework.utils.RxUtils
import ch.rmy.android.framework.utils.UUIDUtils
import io.reactivex.Single

class CopyCertificateFileUseCase(
    private val context: Context,
) {

    operator fun invoke(file: Uri): Single<String> =
        RxUtils.single {
            val fileName = "${UUIDUtils.newUUID()}.p12"
            context.contentResolver.openInputStream(file)!!.use { inputStream ->
                context.openFileOutput(fileName, AppCompatActivity.MODE_PRIVATE).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            fileName
        }
}
