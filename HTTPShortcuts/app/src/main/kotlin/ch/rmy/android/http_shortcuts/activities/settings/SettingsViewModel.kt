package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Application
import ch.rmy.android.http_shortcuts.realm.RealmViewModel
import ch.rmy.android.http_shortcuts.realm.commitAsync
import ch.rmy.android.http_shortcuts.realm.models.AppLock
import org.mindrot.jbcrypt.BCrypt

class SettingsViewModel(application: Application) : RealmViewModel(application) {

    fun setAppLock(password: String) =
        persistedRealm.commitAsync { realm ->
            realm.copyToRealmOrUpdate(
                AppLock()
                    .apply {
                        passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
                    }
            )
        }

}