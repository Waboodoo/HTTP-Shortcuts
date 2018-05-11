package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import org.jdeferred2.Promise

class ExtractCookieAction(
        id: String,
        actionType: ExtractCookieActionType,
        data: Map<String, String>
) : BaseAction(id, actionType, data) {

    var cookieName: String
        get() = internalData[KEY_COOKIE_NAME] ?: ""
        set(value) {
            internalData[KEY_COOKIE_NAME] = value
        }

    var variableKey: String
        get() = internalData[KEY_VARIABLE_KEY] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_KEY] = value
        }

    override fun getDescription(context: Context): CharSequence =
            Variables.rawPlaceholdersToVariableSpans(
                    context,
                    context.getString(R.string.action_type_extract_cookie_description, cookieName, Variables.toRawPlaceholder(variableKey))
            )

    override fun perform(context: Context, shortcutId: Long, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Promise<Unit, Throwable, Unit> {
        val cookiesString = response?.headers?.get(COOKIE_HEADER)
                ?: volleyError?.networkResponse?.headers?.get(COOKIE_HEADER)
                ?: return PromiseUtils.resolve(Unit)

        val cookie = cookiesString.split(';').first().split('=')
        val cookieName = cookie.first()
        if (cookieName != this.cookieName || cookie.size != 2) {
            return PromiseUtils.resolve(Unit)
        }
        val cookieValue = cookie[1]

        variableValues[variableKey] = cookieValue
        Controller().use { controller ->
            return controller.setVariableValue(variableKey, cookieValue)
        }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
            ExtractCookieActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_COOKIE_NAME = "cookieName"
        private const val KEY_VARIABLE_KEY = "variableKey"

        private const val COOKIE_HEADER = "Set-Cookie"

    }

}