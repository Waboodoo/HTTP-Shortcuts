package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import io.reactivex.Completable

class ExtractCookieAction(
    actionType: ExtractCookieActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var cookieName: String
        get() = internalData[KEY_COOKIE_NAME] ?: ""
        set(value) {
            internalData[KEY_COOKIE_NAME] = value
        }

    var variableId: String
        get() = internalData[KEY_VARIABLE_ID] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_ID] = value
        }

    override fun getDescription(context: Context): CharSequence =
        context.getString(R.string.action_type_extract_cookie_description, cookieName, Variables.toRawPlaceholder(variableId))

    override fun perform(context: Context, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val cookiesString = response?.headers?.get(COOKIE_HEADER)
            ?: volleyError?.networkResponse?.headers?.get(COOKIE_HEADER)
            ?: return Completable.complete()

        val cookie = cookiesString.split(';').first().split('=')
        val cookieName = cookie.first()
        if (cookieName != this.cookieName || cookie.size != 2) {
            return Completable.complete()
        }
        val cookieValue = cookie[1]

        variableValues[variableId] = cookieValue
        return Commons.setVariableValue(variableId, cookieValue)
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
        ExtractCookieActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_COOKIE_NAME = "cookieName"
        private const val KEY_VARIABLE_ID = "variableId"

        private const val COOKIE_HEADER = "Set-Cookie"

    }

}