package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import android.util.Base64
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single

class Base64DecodeAction(private val encoded: String) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<String> =
        Single.fromCallable {
            try {
                String(Base64.decode(encoded, Base64.DEFAULT))
            } catch (e: IllegalArgumentException) {
                throw object : UserException() {
                    override fun getLocalizedMessage(context: Context) =
                        "Invalid Base64: $encoded"
                }
            }
        }
}
