package ch.rmy.android.http_shortcuts.data.domains.app_config

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import javax.inject.Inject

class AppConfigRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getAppConfig(): AppConfig = query {
        appConfigDao()
            .getAppConfig()
            ?: AppConfig(
                title = "",
                globalCode = "",
            )
    }
}
