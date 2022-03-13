package ch.rmy.favicongrabber

import ch.rmy.favicongrabber.grabbers.Grabber
import ch.rmy.favicongrabber.grabbers.ICOGrabber
import ch.rmy.favicongrabber.grabbers.ManifestGrabber
import ch.rmy.favicongrabber.grabbers.PageMetaGrabber
import ch.rmy.favicongrabber.models.IconResult
import ch.rmy.favicongrabber.utils.HttpUtil
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import java.io.File

class FaviconGrabber(
    private val client: OkHttpClient,
    private val targetDirectory: File,
    private val userAgent: String,
) {

    fun grab(url: String, preferredSize: Int): List<IconResult> {
        val pageUrl = url.toHttpUrlOrNull()
            ?: return emptyList()

        val pageCache = mutableMapOf<HttpUrl, String?>()
        val httpUtil = HttpUtil(client, targetDirectory, pageCache, userAgent)
        val grabbers = getGrabbers(httpUtil)

        return grabbers.flatMap { grabber ->
            grabber.grabIconsFrom(pageUrl, preferredSize)
        }
    }

    private fun getGrabbers(
        httpUtil: HttpUtil,
    ): List<Grabber> =
        listOf(
            ManifestGrabber(httpUtil),
            PageMetaGrabber(httpUtil),
            ICOGrabber(httpUtil),
        )
}
