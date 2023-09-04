package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class HmacAction
@Inject
constructor() : Action<HmacAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): ByteArray {
        val algorithmName = SUPPORTED_ALGORITHMS[normalizeAlgorithm(algorithm)]
            ?: throwUnsupportedError()
        return try {
            hmac(algorithmName, key, message)
        } catch (e: NoSuchAlgorithmException) {
            throwUnsupportedError()
        } catch (e: IllegalArgumentException) {
            throwUnsupportedError()
        }
    }

    private fun Params.throwUnsupportedError(): Nothing {
        throw ActionException {
            getString(
                R.string.error_unsupported_hmac_algorithm,
                algorithm,
                SUPPORTED_ALGORITHMS.keys.joinToString(),
            )
        }
    }

    data class Params(
        val algorithm: String,
        val key: ByteArray,
        val message: ByteArray,
    )

    companion object {

        internal fun normalizeAlgorithm(algorithm: String) =
            algorithm.lowercase()
                .replace("-", "")
                .replace("_", "")

        internal fun hmac(algorithm: String, key: ByteArray, message: ByteArray): ByteArray {
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
