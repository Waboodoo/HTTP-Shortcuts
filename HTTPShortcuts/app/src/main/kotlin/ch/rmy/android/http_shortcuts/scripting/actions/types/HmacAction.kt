package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.extensions.toHexString
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HmacAction(
    private val algorithm: String,
    private val key: ByteArray,
    private val message: String,
    ) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        Single.fromCallable {
            val algorithmName = SUPPORTED_ALGORITHMS[normalizeAlgorithm(algorithm)]
                ?: throwUnsupportedError()
            try {
                hmac(algorithmName, key, message.toByteArray())
                    .toHexString()
            } catch (e: NoSuchAlgorithmException) {
                throwUnsupportedError()
            } catch (e: IllegalArgumentException) {
                throwUnsupportedError()
            }
        }

    private fun throwUnsupportedError(): Nothing {
        throw ActionException {
            it.getString(
                R.string.error_unsupported_hmac_algorithm,
                algorithm,
                SUPPORTED_ALGORITHMS.keys.joinToString(),
            )
        }
    }

    companion object {

        private fun normalizeAlgorithm(algorithm: String) =
            algorithm.lowercase()
                .replace("-", "")
                .replace("_", "")

        private fun hmac(algorithm: String, key: ByteArray, message: ByteArray): ByteArray {
            val mac = Mac.getInstance(algorithm)
            mac.init(SecretKeySpec(key, algorithm))
            return mac.doFinal(message)
        }

        private val SUPPORTED_ALGORITHMS = mapOf(
            "md5" to "HmacMD5",
            "sha1" to "HmacSHA1",
            "sha256" to "HmacSHA256",
            "sha512" to "HmacSHA512",
        )

    }

}
