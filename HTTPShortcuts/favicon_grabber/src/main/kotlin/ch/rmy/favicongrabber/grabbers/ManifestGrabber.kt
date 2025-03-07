package ch.rmy.favicongrabber.grabbers

import ch.rmy.favicongrabber.models.IconResult
import ch.rmy.favicongrabber.models.ManifestRoot
import ch.rmy.favicongrabber.utils.HTMLUtil
import ch.rmy.favicongrabber.utils.HttpUtil
import ch.rmy.favicongrabber.utils.createComparator
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import okhttp3.HttpUrl

class ManifestGrabber(
    private val httpUtil: HttpUtil,
) : Grabber {
    override suspend fun grabIconsFrom(pageUrl: HttpUrl, preferredSize: Int): List<IconResult> {
        val pageContent = httpUtil.downloadIntoString(pageUrl)
            ?: return emptyList()

        val iconUrls = getManifestIcons(pageUrl, pageContent)
            ?.sortedWith(
                createComparator(preferredSize) { size },
            )
            ?.mapNotNull { icon ->
                pageUrl.resolve(icon.src)
            }
            ?: return emptyList()

        val results = mutableListOf<IconResult>()
        for (iconUrl in iconUrls) {
            val file = httpUtil.downloadIntoFile(iconUrl)
            if (file != null) {
                results.add(IconResult(file))
                if (results.size >= PREFERRED_NUMBER_OF_RESULTS) {
                    break
                }
            }
        }
        return results
    }

    private fun getManifestIcons(pageUrl: HttpUrl, pageContent: String) =
        HTMLUtil.findLinkTags(pageContent, MANIFEST_REL_VALUES)
            .firstOrNull()
            ?.href
            ?.let(pageUrl::resolve)
            ?.let(httpUtil::downloadIntoString)
            ?.let(::parseManifest)
            ?.icons
            ?.filter { icon ->
                icon.type !in UNSUPPORTED_ICON_TYPES && icon.purpose !in UNSUPPORTED_ICON_PURPOSES
            }

    companion object {
        private val MANIFEST_REL_VALUES = setOf("manifest")
        private val UNSUPPORTED_ICON_TYPES = setOf("image/svg+xml")
        private val UNSUPPORTED_ICON_PURPOSES = setOf("monochrome")
        private const val PREFERRED_NUMBER_OF_RESULTS = 2

        private fun parseManifest(manifestString: String): ManifestRoot? =
            try {
                Gson().fromJson(manifestString, ManifestRoot::class.java)
            } catch (e: JsonSyntaxException) {
                null
            } catch (e: JsonParseException) {
                null
            }
    }
}
