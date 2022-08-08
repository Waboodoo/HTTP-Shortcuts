package ch.rmy.android.http_shortcuts.activities.editor.usecases

import android.content.Context
import android.graphics.BitmapFactory
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.Optional
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.favicongrabber.FaviconGrabber
import ch.rmy.favicongrabber.models.IconResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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

    operator fun invoke(url: String, variables: List<VariableModel>): Single<Optional<ShortcutIcon>> =
        variableResolver.resolve(variables, Variables.extractVariableIds(url))
            .map { variableManager ->
                Variables.rawPlaceholdersToResolvedValues(url, variableManager.getVariableValuesByIds())
            }
            .observeOn(Schedulers.io())
            .map { finalUrl ->
                val iconSize = IconUtil.getIconSize(context)
                FaviconGrabber(client, context.cacheDir, userAgent = UserAgentUtil.userAgent)
                    .grab(finalUrl, preferredSize = iconSize)
                    .mapNotNull(::toCandidate)
                    .sortedByDescending { it.size }
            }
            .map { candidates ->
                try {
                    Optional(
                        candidates.firstNotNullOfOrNull { candidate ->
                            toShortcutIcon(context, candidate.file)
                        }
                    )
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
            logException(e)
        }
        return null
    }

    private fun toShortcutIcon(context: Context, file: File): ShortcutIcon? =
        try {
            file.inputStream().use { inStream ->
                IconUtil.createIconFromStream(context, inStream)
            }
        } catch (e: Exception) {
            logException(e)
            null
        }

    data class Candidate(
        val file: File,
        val size: Int,
    )
}
