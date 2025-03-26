package ch.rmy.android.http_shortcuts.data.domains.app_config

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    suspend fun getGlobalCode(): String = query {
        appConfigDao()
            .getAppConfig()
            ?.globalCode
            .orEmpty()
    }

    suspend fun getToolbarTitle(): String = query {
        appConfigDao()
            .getAppConfig()
            ?.title
            ?.trim()
            .orEmpty()
    }

    fun observeToolbarTitle(): Flow<String> = queryFlow {
        appConfigDao()
            .observeAppConfig()
            .map { it?.title?.trim().orEmpty() }
    }

    suspend fun setToolbarTitle(title: String) = query {
        appConfigDao()
            .update {
                it.copy(title = title)
            }
    }

    suspend fun setGlobalCode(globalCode: String) = query {
        appConfigDao()
            .update {
                it.copy(globalCode = globalCode)
            }
    }
}
