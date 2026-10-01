package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class SendSSHCommandActionType
@Inject
constructor(
    private val sendTCPPacketAction: SendSSHCommandAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs): ActionRunnable<*> {
        val host = args.getString(0) ?: ""
        val options = args.getObject(1) ?: emptyMap()
        return ActionRunnable(
            action = sendTCPPacketAction,
            params = SendSSHCommandAction.Params(
                host = host,
                port = options["port"] as? Int ?: 22,
                username = options["username"] as? String ?: "",
                password = (options["password"] as? String),
                privateKey = (options["privateKey"] as? String)?.takeUnlessEmpty(),
                passphrase = (options["passphrase"] as? String),
                verifyHost = (options["verifyHost"] as? String),
                command = args.getString(2) ?: "",
            ),
        )
    }

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = 3,
    )

    companion object {
        private const val TYPE = "send_ssh_command"
        private const val FUNCTION_NAME = "sendSSHCommand"
        private const val FUNCTION_NAME_ALIAS = "sendSshCommand"
    }
}
