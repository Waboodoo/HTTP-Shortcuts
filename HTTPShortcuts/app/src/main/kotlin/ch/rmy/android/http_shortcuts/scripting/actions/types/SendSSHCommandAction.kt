package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Base64
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LocalNetworkPermissionManager
import ch.rmy.android.scripting.JsObject
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext
import org.connectbot.sshlib.AuthResult
import org.connectbot.sshlib.ConnectResult
import org.connectbot.sshlib.HostKeyVerifier
import org.connectbot.sshlib.PublicKey
import org.connectbot.sshlib.SessionExit
import org.connectbot.sshlib.SshClient
import org.connectbot.sshlib.SshSession

class SendSSHCommandAction
@Inject
constructor(
    private val localNetworkPermissionManager: LocalNetworkPermissionManager,
) : Action<SendSSHCommandAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): JsObject {
        localNetworkPermissionManager.requestLocalNetworkPermissionIfNeeded(host)

        val result = withContext(Dispatchers.IO) {
            try {
                val hostKeyVerifier = object : HostKeyVerifier {
                    override suspend fun verify(key: PublicKey): Boolean {
                        if (verifyHost?.isEmpty() == true) {
                            return true
                        }
                        return verifyHost == key.toConfigString()
                    }
                }

                val client = SshClient(host, port = port, hostKeyVerifier = hostKeyVerifier)
                client.connectOrThrow(verifyHost)

                try {
                    client.authenticate(username, password, privateKey, passphrase)

                    val session = client.openSession()
                        ?: throw ActionException {
                            "Failed to open SSH session"
                        }
                    session.use {
                        val result = session.requestExec(command)
                        if (!result) {
                            throw ActionException {
                                "SSH command rejected by host"
                            }
                        }

                        return@withContext session.readResult()
                    }
                } finally {
                    client.disconnect()
                }
            } catch (e: ActionException) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logException(e)
                throw ActionException {
                    "SSH failed unexpectedly: $e"
                }
            }
        }

        return executionContext.scriptingEngine.buildJsObject {
            property("stdout", result.stdout)
            property("stderr", result.stderr)
            property("code", result.code)
        }
    }

    private suspend fun SshClient.connectOrThrow(verifyHost: String?) {
        when (val result = connect()) {
            is ConnectResult.AlgorithmMismatch -> throw ActionException {
                "SSH connection failed due to algorithm mismatch: ${result.message}"
            }
            is ConnectResult.HostKeyRejected -> throw ActionException {
                if (verifyHost == null) {
                    "SSH connection failed due to unconfigured host key verification.\ngot ${result.key.toConfigString()}"
                } else {
                    "SSH connection failed due to unexpected host key:\nexpected $verifyHost\ngot ${result.key.toConfigString()}"
                }
            }
            is ConnectResult.ProtocolError -> throw ActionException {
                "SSH connection failed due to a protocol error: ${result.message}"
            }
            is ConnectResult.TransportError -> throw ActionException {
                "SSH connection failed due to a transport error: ${result.cause}"
            }
            ConnectResult.Success -> Unit
        }
    }

    private suspend fun SshClient.authenticate(username: String, password: String?, privateKey: String?, passphrase: String?) {
        val authResult = if (privateKey != null) {
            authenticatePublicKey(username, privateKey.toByteArray(), passphrase)
        } else if (password != null) {
            authenticatePassword(username, password)
        } else {
            AuthResult.Success
        }
        if (authResult !is AuthResult.Success || !isAuthenticated) {
            throw ActionException {
                "SSH authentication failed"
                    .runIfNotNull((authResult as? AuthResult.Error)?.message) {
                        plus(": $it")
                    }
            }
        }
    }

    private suspend fun SshSession.readResult(): Result {
        val code = when (val exitInfo = exitInfo.await()) {
            is SessionExit.Signal -> throw ActionException {
                "SSH command failed with signal ${exitInfo.signalName}"
                    .runIf(exitInfo.errorMessage.isNotEmpty()) {
                        plus(": ${exitInfo.errorMessage}")
                    }
            }
            is SessionExit.Status -> exitInfo.code
            null -> null
        }
        return Result(
            code = code,
            stdout = stdout.readAllIntoString(),
            stderr = stderr.readAllIntoString(),
        )
    }

    private suspend fun ReceiveChannel<ByteArray>.readAllIntoString(): String? =
        buildList {
            while (true) {
                val value = receiveCatching().getOrNull()?.decodeToString()
                if (value != null) {
                    add(value)
                } else {
                    break
                }
            }
        }
            .takeUnlessEmpty()
            ?.joinToString(separator = "")

    data class Params(
        val host: String,
        val port: Int,
        val username: String,
        val password: String?,
        val privateKey: String?,
        val passphrase: String?,
        val verifyHost: String?,
        val command: String,
    )

    private fun PublicKey.toConfigString(): String =
        "$type ${Base64.encodeToString(encoded, Base64.DEFAULT).trim()}"

    private data class Result(
        val code: Long?,
        val stdout: String?,
        val stderr: String?,
    )
}
