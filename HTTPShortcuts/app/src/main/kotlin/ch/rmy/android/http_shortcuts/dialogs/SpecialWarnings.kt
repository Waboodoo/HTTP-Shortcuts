package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import java.util.*

/**
 * This class deals with showing a warning message to Russian users of the app. I know
 * this is not pretty at all, but I'm getting really tired of people complaining to me
 * in Russian without providing ANY kind of context as to what they are even trying to
 * do. If you have any information regarding this, please let me know. Thank you.
 */
object SpecialWarnings {

    fun showIfNeeded(context: Context): Boolean {
        if (!shouldShow()) {
            return false
        }
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!preferences.getBoolean(PREF_WARNED_CONFUSED_RUSSIAN_USERS, false)) {
            preferences.edit().putBoolean(PREF_WARNED_CONFUSED_RUSSIAN_USERS, true).apply()
            DialogBuilder(context)
                .title(TITLE)
                .message(MESSAGE)
                .positive(R.string.dialog_ok)
                .showIfPossible()
            return true
        }
        return false
    }

    private fun shouldShow(): Boolean =
        Locale.getDefault().getLanguage() == "ru"

    private const val PREF_NAME = "special_warnings"
    private const val PREF_WARNED_CONFUSED_RUSSIAN_USERS = "confused_russian_users"

    private const val TITLE = "Предупреждение"
    private const val MESSAGE = "Я продолжаю получать электронные письма и отзывы от людей, которые жалуются мне на «Билайн», «тарифы» и «скидки». Я не знаю, о чем это, но я не могу помочь ни с чем из этого. Это не имеет ничего общего с этим приложением. Пожалуйста, прекратите посылать мне эти сообщения, если вы не предоставите достаточно контекста, чтобы я знал, что вы хотите. Спасибо.\n\nI keep getting emails and reviews from people who complain to me about \"Beeline\", \"tariffs\" and \"discounts\". I don't know what this is about, but I cannot help with any of that. It has NOTHING to do with this app. Please stop sending me these messages, unless you actually provide enough context so that I know what you want. Thank you."

}