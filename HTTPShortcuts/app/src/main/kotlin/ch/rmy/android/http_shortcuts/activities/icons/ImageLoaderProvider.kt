package ch.rmy.android.http_shortcuts.activities.icons

import android.content.Context
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.OkHttpClient

fun createImageLoader(context: Context): ImageLoader {
    val userAgent = UserAgentProvider.getUserAgent(context)
    return ImageLoader.Builder(context)
        .components {
            add(
                OkHttpNetworkFetcherFactory(
                    callFactory = {
                        OkHttpClient.Builder()
                            .addInterceptor { chain ->
                                chain.proceed(
                                    chain.request()
                                        .newBuilder()
                                        .header(HttpHeaders.USER_AGENT, userAgent)
                                        .build(),
                                )
                            }
                            .build()
                    },
                ),
            )
        }
        .build()
}
