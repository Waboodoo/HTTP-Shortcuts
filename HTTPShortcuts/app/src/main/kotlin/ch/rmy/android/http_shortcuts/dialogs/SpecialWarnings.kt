package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import java.util.Locale

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
            show(context)
            return true
        }
        return false
    }

    fun show(context: Context) {
        DialogBuilder(context)
            .title(TITLE)
            .message(MESSAGE)
            .positive(R.string.dialog_ok)
            .showIfPossible()
    }

    private fun shouldShow(): Boolean =
        Locale.getDefault().language == "ru"

    private const val PREF_NAME = "special_warnings"
    private const val PREF_WARNED_CONFUSED_RUSSIAN_USERS = "confused_russian_users"

    private const val TITLE = "Предупреждение"
    private const val MESSAGE = "Я получаю множество писем и отзывов от людей, которые жалуются мне на Beeline, скрипты, тарифы и скидки, или на ошибку 403. Пожалуйста, поймите, что я не могу вам помочь с этим. Я не несу ответственности за то, что эти скрипты больше не работают. Пожалуйста, не присылайте мне жалобы на это. Спасибо.\n\nI get a lot of emails and reviews from people who complain to me about Beeline, scripts, tariffs and discounts, or about error 403. Please understand that I cannot help you with that. I am not responsible for the fact that those scripts do not work anymore. Please don't send me complaints about that. Thank you."

}