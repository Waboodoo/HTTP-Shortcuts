package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toHexString
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class HashAction(private val algorithm: String, private val text: String) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        Single.fromCallable {
            val algorithmName = SUPPORTED_ALGORITHMS[normalizeAlgorithm(algorithm)]
                ?: throwUnsupportedError()
            try {
                MessageDigest.getInstance(algorithmName)
                    .digest(text.toByteArray())
                    .toHexString()
            } catch (e: NoSuchAlgorithmException) {
                throwUnsupportedError()
            }
        }

    private fun throwUnsupportedError(): Nothing {
        throw ActionException {
            it.getString(
                R.string.error_unsupported_hash_algorithm,
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

        private val SUPPORTED_ALGORITHMS = mapOf(
            "md5" to "md5",
            "sha1" to "sha-1",
            "sha256" to "sha-256",
            "sha512" to "sha-512",
        )
    }
}
