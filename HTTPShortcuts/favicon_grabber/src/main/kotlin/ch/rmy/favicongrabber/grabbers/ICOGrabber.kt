package ch.rmy.favicongrabber.grabbers

import ch.rmy.favicongrabber.models.IconResult
import ch.rmy.favicongrabber.utils.HttpUtil
import okhttp3.HttpUrl

class ICOGrabber(
    private val httpUtil: HttpUtil,
) : Grabber {

    override suspend fun grabIconsFrom(pageUrl: HttpUrl, preferredSize: Int): List<IconResult> {
        val faviconFile = getFaviconUrl(pageUrl)
            ?.let { httpUtil.downloadIntoFile(it) }
        return if (faviconFile != null) {
            listOf(IconResult(faviconFile))
        } else {
            emptyList()
        }
    }

    companion object {
        private const val DEFAULT_FAVICON_PATH = "/favicon.ico"

        internal fun getFaviconUrl(pageUrl: HttpUrl) =
            pageUrl.resolve(DEFAULT_FAVICON_PATH)
    }
}
