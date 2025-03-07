package ch.rmy.android.http_shortcuts.activities.editor.usecases

import android.content.Context
import android.graphics.BitmapFactory
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.favicongrabber.FaviconGrabber
import ch.rmy.favicongrabber.models.IconResult
import java.io.File
import java.lang.Exception
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchFaviconUseCase
@Inject
constructor(
    private val context: Context,
    private val variableResolver: VariableResolver,
    httpClientFactory: HttpClientFactory,
) {

    private val client = httpClientFactory.getClient(context)

    suspend operator fun invoke(url: String, variables: List<Variable>, dialogHandle: DialogHandle): ShortcutIcon? {
        val variableManager = VariableManager(variables)
        variableResolver.resolve(variableManager, Variables.extractVariableIds(url), dialogHandle)
        val finalUrl = Variables.rawPlaceholdersToResolvedValues(url, variableManager.getVariableValuesByIds())

        val iconSize = IconUtil.getIconSize(context)
        return withContext(Dispatchers.IO) {
            val candidates = FaviconGrabber(
                client = client,
                targetDirectory = context.cacheDir,
                userAgent = UserAgentProvider.getUserAgent(context),
            )
                .grab(finalUrl, preferredSize = iconSize)
                .mapNotNull(::toCandidate)
                .sortedByDescending { it.size }

            try {
                candidates.firstNotNullOfOrNull { candidate ->
                    toShortcutIcon(context, candidate.file)
                }
            } finally {
                candidates.forEach { candidate ->
                    candidate.file.delete()
                }
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
