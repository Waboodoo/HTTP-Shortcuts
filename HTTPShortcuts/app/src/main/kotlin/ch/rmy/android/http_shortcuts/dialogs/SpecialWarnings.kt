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
        Locale.getDefault().language == "ru"

    private const val PREF_NAME = "special_warnings"
    private const val PREF_WARNED_CONFUSED_RUSSIAN_USERS = "confused_russian_users"

    private const val TITLE = "Предупреждение"
    private const val MESSAGE = "Я получаю много писем и отзывов от людей, которые жалуются мне на скрипты, которые применяют тарифы и скидки.\n" +
        "Обратите внимание, что это приложение не имеет к этому никакого отношения и что эти скрипты больше не работают. Я не несу ответственности и никоим образом не связан с поставщиком этих сценариев или каким-либо оператором связи. Пожалуйста, прекратите отправлять мне эти сообщения. Спасибо.\n\nI have been getting a lot of emails and reviews from people who complain to me about scripts that apply tariffs and discounts. \n" +
        "Please note that this app has nothing to do with that and that those scripts no longer work. I am not responsible or in any way affiliated with the provider of those scripts or with any telecom operator. Please stop sending me these messages. Thank you."

}