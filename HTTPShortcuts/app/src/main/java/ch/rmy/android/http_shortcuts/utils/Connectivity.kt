package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.net.ConnectivityManager

object Connectivity {

    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null
    }

}
