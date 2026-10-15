package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Base64
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LocalNetworkPermissionManager
import ch.rmy.android.scripting.JsObject
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.text.decodeToString
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

        val result: Result = withContext(Dispatchers.IO) {
            try {
                val hostKeyVerifier = object : HostKeyVerifier {
                    override suspend fun verify(key: PublicKey) =
                        verifyHost?.isEmpty() == true || verifyHost == key.toConfigString()
                }

                val client = SshClient(host, port = port, hostKeyVerifier = hostKeyVerifier)
                client.connectOrThrow(verifyHost)

                try {
                    client.authenticate(username, password, privateKey, passphrase)

                    val session = client.openSession()
                        ?: throw ActionException {
                            getString(R.string.error_ssh_failed_to_open_session)
                        }
                    session.use {
                        val result = session.requestExec(command)
                        if (!result) {
                            throw ActionException {
                                getString(R.string.error_ssh_command_rejected_by_host)
                            }
                        }

                        return@withContext if (rawBytes) {
                            session.readRawResult()
                        } else {
                            session.readStringResult()
                        }
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
                    getString(R.string.error_ssh_failed, e.message ?: e.toString())
                }
            }
        }

        return executionContext.scriptingEngine.buildJsObject {
            when (result) {
                is RawResult -> {
                    property("stdout", result.stdout)
                    property("stderr", result.stderr)
                }
                is StringResult -> {
                    property("stdout", result.stdout)
                    property("stderr", result.stderr)
                }
            }
            property("code", result.code)
        }
    }

    private suspend fun SshClient.connectOrThrow(verifyHost: String?) {
        when (val result = connect()) {
            is ConnectResult.AlgorithmMismatch -> throw ActionException {
                getString(R.string.error_ssh_failed_with_algorithm_mismatch, result.message)
            }
            is ConnectResult.HostKeyRejected -> throw ActionException {
                if (verifyHost == null || verifyHost == "<algorithm> <public-key>") {
                    getString(R.string.error_ssh_host_verification_failed, result.key.toConfigString())
                } else {
                    getString(R.string.error_ssh_host_verification_failed_with_expected, verifyHost, result.key.toConfigString())
                }
            }
            is ConnectResult.ProtocolError -> throw ActionException {
                getString(R.string.error_ssh_failed, result.message)
            }
            is ConnectResult.TransportError -> throw ActionException {
                getString(R.string.error_ssh_failed, result.cause.message)
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
                val details = when (val errorMessage = (authResult as? AuthResult.Error)?.message) {
                    "Decryption failed (check integers don't match)" -> getString(R.string.error_ssh_incorrect_passphrase)
                    else -> errorMessage?.takeUnlessEmpty()
                }
                getString(R.string.error_ssh_authentication_failed)
                    .runIfNotNull(details) {
                        plus("\n\n$it")
                    }
            }
        }
    }

    private suspend fun SshSession.readStringResult() = StringResult(
        code = readCode(),
        stdout = stdout.readAll()?.decodeToString(),
        stderr = stderr.readAll()?.decodeToString(),
    )

    private suspend fun SshSession.readRawResult() = RawResult(
        code = readCode(),
        stdout = stdout.readAll(),
        stderr = stderr.readAll(),
    )

    private suspend fun SshSession.readCode(): Long? =
        when (val exitInfo = exitInfo.await()) {
            is SessionExit.Signal -> throw ActionException {
                getString(R.string.error_ssh_command_failed_with_signal, exitInfo.signalName)
                    .runIf(exitInfo.errorMessage.isNotEmpty()) {
                        plus("\n\n${exitInfo.errorMessage}")
                    }
            }
            is SessionExit.Status -> exitInfo.code
            null -> null
        }

    private suspend fun ReceiveChannel<ByteArray>.readAll(): ByteArray? =
        buildList {
            while (true) {
                add(receiveCatching().getOrNull() ?: break)
            }
        }
            .takeUnlessEmpty()
            ?.let { byteArrays ->
                val buffer = ByteBuffer.allocate(byteArrays.sumOf { it.size })
                byteArrays.forEach { byteArray ->
                    buffer.put(byteArray)
                }
                buffer.array()
            }

    data class Params(
        val host: String,
        val port: Int,
        val username: String,
        val password: String?,
        val privateKey: String?,
        val passphrase: String?,
        val verifyHost: String?,
        val command: String,
        val rawBytes: Boolean,
    )

    private fun PublicKey.toConfigString(): String =
        "$type ${Base64.encodeToString(encoded, Base64.DEFAULT).trim()}"

    sealed interface Result {
        val code: Long?
    }

    private data class StringResult(
        override val code: Long?,
        val stdout: String?,
        val stderr: String?,
    ) : Result

    private data class RawResult(
        override val code: Long?,
        val stdout: ByteArray?,
        val stderr: ByteArray?,
    ) : Result
}
