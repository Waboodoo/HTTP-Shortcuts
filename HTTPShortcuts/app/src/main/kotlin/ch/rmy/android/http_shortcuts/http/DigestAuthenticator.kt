package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.exceptions.DigestAuthException
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class DigestAuthenticator(credentials: Credentials) : DigestAuthenticator(credentials) {

    override fun authenticate(route: Route?, response: Response): Request? {
        try {
            return super.authenticate(route, response)
        } catch (e: IllegalArgumentException) {
            throw DigestAuthException(e.message!!)
        }
    }
}
