package ch.rmy.android.http_shortcuts.activities.settings.importexport.usecases

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

/**
 * This class deals with showing a warning message to Russian users of the app. I know
 * this is not pretty at all, but I'm getting really tired of people complaining to me
 * in Russian without providing ANY kind of context as to what they are even trying to
 * do. If you have any information regarding this, please let me know. Thank you.
 */
class GetRussianWarningDialogUseCase
@Inject
constructor() {

    operator fun invoke(): DialogState =
        createDialogState {
            title(TITLE)
                .message(MESSAGE)
                .positive(R.string.dialog_ok)
                .build()
        }

    companion object {
        private const val TITLE = "Предупреждение"
        private const val MESSAGE = "Я получаю множество писем и отзывов от людей, которые жалуются мне на Beeline, скрипты, тарифы и скидки, " +
            "или на ошибку 403. Пожалуйста, поймите, что я не могу вам помочь с этим. Я не несу ответственности за то, что эти скрипты больше " +
            "не работают. Пожалуйста, не присылайте мне жалобы на это. Спасибо.\n\nI get a lot of emails and reviews from people who complain " +
            "to me about Beeline, scripts, tariffs and discounts, or about error 403. Please understand that I cannot help you with that. I am " +
            "not responsible for the fact that those scripts do not work anymore. Please don't send me complaints about that. Thank you."
    }
}
