package ch.rmy.favicongrabber.utils

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException

class HttpUtil(
    private val client: OkHttpClient,
    private val targetDirectory: File,
    private val pageCache: MutableMap<HttpUrl, String?>,
    private val userAgent: String,
) {

    fun downloadIntoFile(url: HttpUrl): File? {
        var targetFile: File? = null
        try {
            makeRequest(url).use { response ->
                response
                    ?.byteStream()
                    ?.use { inStream ->
                        targetFile = File.createTempFile(TEMP_FILE_PREFIX, null, targetDirectory)
                        targetFile
                            ?.outputStream()
                            ?.use { outStream ->
                                inStream.copyTo(outStream)
                                return targetFile
                            }
                    }
            }
        } catch (e: IOException) {
            targetFile?.delete()
        }
        return null
    }

    fun downloadIntoString(url: HttpUrl): String? {
        try {
            if (pageCache.containsKey(url)) {
                return pageCache[url]
            }
            makeRequest(url).use { responseBody ->
                val result = responseBody
                    ?.takeUnless { it.contentLength() > MAX_PAGE_LENGTH }
                    ?.string()
                pageCache[url] = result
                return result
            }
        } catch (e: IOException) {
            pageCache[url] = null
            return null
        }
    }

    private fun makeRequest(url: HttpUrl): ResponseBody? =
        client.newCall(
            Request.Builder()
                .url(url)
                .header(HEADER_USER_AGENT, userAgent)
                .build()
        )
            .execute()
            .takeIf { it.isSuccessful }
            ?.body

    companion object {
        private const val HEADER_USER_AGENT = "User-Agent"
        private const val TEMP_FILE_PREFIX = "favico_"

        private const val MAX_PAGE_LENGTH = 1024 * 1024
    }
}
