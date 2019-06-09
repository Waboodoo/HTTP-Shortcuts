package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Application
import ch.rmy.android.http_shortcuts.data.RealmViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.models.AppLock
import org.mindrot.jbcrypt.BCrypt

class SettingsViewModel(application: Application) : RealmViewModel(application) {

    fun setAppLock(password: String) =
        Transactions.commit { realm ->
            realm.copyToRealmOrUpdate(
                AppLock()
                    .apply {
                        passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
                    }
            )
        }

}