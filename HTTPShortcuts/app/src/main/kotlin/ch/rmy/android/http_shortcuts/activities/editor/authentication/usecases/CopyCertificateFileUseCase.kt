package ch.rmy.android.http_shortcuts.activities.editor.authentication.usecases

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.framework.utils.UUIDUtils
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CopyCertificateFileUseCase
@Inject
constructor(
    private val context: Context,
) {

    suspend operator fun invoke(file: Uri): String =
        withContext(Dispatchers.IO) {
            val fileName = "${UUIDUtils.newUUID()}.p12"
            context.contentResolver.openInputStream(file)!!.use { inputStream ->
                context.openFileOutput(fileName, AppCompatActivity.MODE_PRIVATE).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            fileName
        }
}
