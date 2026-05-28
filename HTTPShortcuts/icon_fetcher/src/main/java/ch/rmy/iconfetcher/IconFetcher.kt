package ch.rmy.iconfetcher

import ch.rmy.iconfetcher.Config.CACHE_MAX_AGE
import ch.rmy.iconfetcher.Config.ICONS_FILE
import ch.rmy.iconfetcher.models.IconEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.io.Reader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody

class IconFetcher(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val cacheFile: File,
) {
    @Throws(IOException::class)
    suspend fun getIcons(): List<IconEntry> = withContext(Dispatchers.IO) {
        try {
            getEntries()
        } catch (e: IllegalStateException) {
            if (e.message?.contains("Expected BEGIN_ARRAY but was STRING") == true) {
                // The server sometimes returns fails and returns an error message.
                // We treat this as an IOException as it is temporary and expected.
                throw IOException(e)
            } else {
                throw e
            }
        }
    }

    private fun getEntries(): List<IconEntry> {
        if (!isCacheFileValid()) {
            fetchIconIndex().use { responseBody ->
                responseBody.byteStream().copyTo(cacheFile.outputStream())
            }
        }
        return getCachedIconIndex().use { reader ->
            val type = object : TypeToken<List<IconEntry>>() {
            }.type
            Gson().fromJson(reader, type)
        }
    }

    private fun fetchIconIndex(): ResponseBody =
        client.newCall(
            Request.Builder()
                .url(baseUrl + ICONS_FILE)
                .build(),
        )
            .execute()
            .takeIf { it.isSuccessful }
            ?.body
            ?: throw IOException()

    private fun getCachedIconIndex(): Reader =
        cacheFile.reader()

    private fun isCacheFileValid() =
        cacheFile.exists() && cacheFile.lastModified().let { it != 0L && it + CACHE_MAX_AGE.inWholeMilliseconds > System.currentTimeMillis() }
}
