package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class HashAction(private val algorithm: String, private val text: String) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<String> =
        Single.fromCallable {
            if (algorithm !in SUPPORTED_ALGORITHMS) {
                throwUnsupportedError()
            }
            try {
                val digest = MessageDigest.getInstance(algorithm)
                val result = digest.digest(text.toByteArray())
                result.joinToString(separator = "") {
                    String.format("%02x", it)
                }
            } catch (e: NoSuchAlgorithmException) {
                throwUnsupportedError()
            }
        }

    private fun throwUnsupportedError(): Nothing {
        throw ActionException {
            it.getString(
                R.string.error_unsupported_hash_algorithm,
                algorithm,
                SUPPORTED_ALGORITHMS.joinToString(),
            )
        }
    }

    companion object {

        private val SUPPORTED_ALGORITHMS = setOf(
            "md5",
            "sha-1",
            "sha-256",
            "sha-512"
        )

    }

}