package ch.rmy.android.http_shortcuts.activities.settings.usecases

import android.content.Context
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTranslationProgressUseCase
@Inject
constructor(
    private val context: Context,
) {
    suspend operator fun invoke(): Map<String, Int> = withContext(Dispatchers.IO) {
        buildMap {
            context.assets.open("translation-progress.txt").use { inputStream ->
                inputStream.reader().use { reader ->
                    reader.forEachLine { line ->
                        val parts = line.split(":")
                        if (parts.size == 2) {
                            put(parts[0], parts[1].toInt())
                        }
                    }
                }
            }
        }
    }
}
