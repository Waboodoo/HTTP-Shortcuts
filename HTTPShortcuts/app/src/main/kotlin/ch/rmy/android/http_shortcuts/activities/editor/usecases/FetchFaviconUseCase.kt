package ch.rmy.android.http_shortcuts.activities.editor.usecases

import android.content.Context
import android.graphics.BitmapFactory
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.favicongrabber.FaviconGrabber
import ch.rmy.favicongrabber.models.IconResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception
import javax.inject.Inject

class FetchFaviconUseCase
@Inject
constructor(
    private val context: Context,
    private val variableResolver: VariableResolver,
    httpClientFactory: HttpClientFactory,
) {

    private val client = httpClientFactory.getClient(context)

    suspend operator fun invoke(url: String, variables: List<VariableModel>): ShortcutIcon? {
        val variableManager = VariableManager(variables)
        variableResolver.resolve(variableManager, Variables.extractVariableIds(url))
        val finalUrl = Variables.rawPlaceholdersToResolvedValues(url, variableManager.getVariableValuesByIds())

        val iconSize = IconUtil.getIconSize(context)
        val candidates = withContext(Dispatchers.IO) {
            FaviconGrabber(client, context.cacheDir, userAgent = UserAgentUtil.userAgent)
                .grab(finalUrl, preferredSize = iconSize)
                .mapNotNull(::toCandidate)
                .sortedByDescending { it.size }
        }

        return try {
            candidates.firstNotNullOfOrNull { candidate ->
                toShortcutIcon(context, candidate.file)
            }
        } finally {
            candidates.forEach { candidate ->
                candidate.file.delete()
            }
        }
    }

    private fun toCandidate(result: IconResult): Candidate? {
        try {
            val options = BitmapFactory.Options()
                .apply {
                    inJustDecodeBounds = true
                }
            result.file.inputStream().use { inStream ->
                BitmapFactory.decodeStream(inStream, null, options)
            }
            return options.outWidth
                .takeUnless { it == -1 }
                ?.let { size ->
                    Candidate(result.file, size)
                }
        } catch (e: Exception) {
            result.file.delete()
            if (e is CancellationException) {
                throw e
            } else {
                logException(e)
            }
        }
        return null
    }

    private fun toShortcutIcon(context: Context, file: File): ShortcutIcon? =
        try {
            file.inputStream().use { inStream ->
                IconUtil.createIconFromStream(context, inStream)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logException(e)
            null
        }

    data class Candidate(
        val file: File,
        val size: Int,
    )
}
