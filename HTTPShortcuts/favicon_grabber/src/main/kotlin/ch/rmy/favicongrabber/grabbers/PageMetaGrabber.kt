package ch.rmy.favicongrabber.grabbers

import ch.rmy.favicongrabber.models.IconResult
import ch.rmy.favicongrabber.utils.HTMLUtil.findLinkTags
import ch.rmy.favicongrabber.utils.HttpUtil
import ch.rmy.favicongrabber.utils.createComparator
import okhttp3.HttpUrl

class PageMetaGrabber(
    private val httpUtil: HttpUtil,
) : Grabber {
    override suspend fun grabIconsFrom(pageUrl: HttpUrl, preferredSize: Int): List<IconResult> {
        val pageContent = httpUtil.downloadIntoString(pageUrl)
            ?: return emptyList()

        val candidates = findLinkTags(pageContent, ICON_REL_VALUES)
            .sortedWith(
                createComparator(preferredSize) { size },
            )

        val results = mutableListOf<IconResult>()
        for (candidate in candidates) {
            val file = pageUrl.resolve(candidate.href)
                ?.takeUnless { it.encodedPath == DEFAULT_FAVICON_PATH }
                ?.let { httpUtil.downloadIntoFile(it) }
            if (file != null) {
                results.add(IconResult(file))
                if (results.size >= PREFERRED_NUMBER_OF_RESULTS) {
                    break
                }
            }
        }
        return results
    }

    companion object {
        private val ICON_REL_VALUES = setOf(
            "apple-touch-icon",
            "apple-touch-icon-precomposed",
            "shortcut icon",
            "icon",
        )
        private const val PREFERRED_NUMBER_OF_RESULTS = 2
        private const val DEFAULT_FAVICON_PATH = "/favicon.ico"
    }
}
