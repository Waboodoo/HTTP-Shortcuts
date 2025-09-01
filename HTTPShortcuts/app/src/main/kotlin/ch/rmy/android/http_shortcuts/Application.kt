package ch.rmy.android.http_shortcuts

import android.content.Context
import ch.rmy.android.framework.extensions.GlobalLogger
import ch.rmy.android.http_shortcuts.data.realm.RealmToRoomMigration
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : android.app.Application() {
    private val context: Context
        get() = this

    @Inject
    lateinit var localeHelper: LocaleHelper

    @Inject
    lateinit var realmToRoomMigration: RealmToRoomMigration

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate() {
        super.onCreate()
        localeHelper.applyLocaleFromSettings()

        Logging.initCrashReporting(context)
        GlobalLogger.registerLogging(Logging)

        DarkThemeHelper.applyDarkThemeSettings(userPreferences.darkThemeSetting)
    }
}
