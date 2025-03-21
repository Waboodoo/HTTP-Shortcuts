package ch.rmy.android.http_shortcuts.data.domains.app_config

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import ch.rmy.android.http_shortcuts.import_export.ImportExportBase
import ch.rmy.android.http_shortcuts.import_export.Importer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppConfigRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getAppConfig(): AppConfig =
        get(Database::appConfigDao)
            .getAppConfig()
            ?: AppConfig()

    suspend fun getGlobalCode(): String =
        get(Database::appConfigDao)
            .getAppConfig()
            ?.globalCode
            .orEmpty()

    suspend fun getToolbarTitle(): String =
        get(Database::appConfigDao)
            .getAppConfig()
            ?.title
            ?.trim()
            .orEmpty()

    fun observeToolbarTitle(): Flow<String> =
        flow(Database::appConfigDao) {
            observeAppConfig()
                .map { it?.title?.trim().orEmpty() }
        }

    suspend fun setToolbarTitle(title: String) {
        get(Database::appConfigDao)
            .update {
                it.copy(title = title)
            }
    }

    suspend fun setGlobalCode(globalCode: String) {
        get(Database::appConfigDao)
            .update {
                it.copy(globalCode = globalCode)
            }
    }

    suspend fun import(base: ImportExportBase, mode: Importer.ImportMode) {
        get(Database::appConfigDao)
            .update { oldAppConfig ->
                var newTitle = oldAppConfig.title
                var newGlobalCode = oldAppConfig.globalCode
                when (mode) {
                    Importer.ImportMode.MERGE -> {
                        if (base.title != null && oldAppConfig.title.isEmpty()) {
                            newTitle = base.title
                        }
                        if (base.globalCode != null && oldAppConfig.globalCode.isEmpty()) {
                            newGlobalCode = base.globalCode
                        }
                    }
                    Importer.ImportMode.REPLACE -> {
                        if (base.title != null) {
                            newTitle = base.title
                        }
                        if (base.globalCode != null) {
                            newGlobalCode = base.globalCode
                        }
                    }
                }
                oldAppConfig.copy(
                    title = newTitle,
                    globalCode = newGlobalCode,
                )
            }
    }
}
