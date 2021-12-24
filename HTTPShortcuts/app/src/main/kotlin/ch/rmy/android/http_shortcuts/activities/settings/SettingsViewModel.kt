package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import org.mindrot.jbcrypt.BCrypt

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository()

    fun setAppLock(password: String) =
        appRepository.setLock(BCrypt.hashpw(password, BCrypt.gensalt()))
}
