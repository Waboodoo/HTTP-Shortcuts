package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import ch.rmy.android.http_shortcuts.data.domains.import_export.ImportRepository
import ch.rmy.android.http_shortcuts.import_export.models.ImportBase
import ch.rmy.android.http_shortcuts.import_export.models.ImportCategory
import ch.rmy.android.http_shortcuts.import_export.models.ImportCertificatePin
import ch.rmy.android.http_shortcuts.import_export.models.ImportHeader
import ch.rmy.android.http_shortcuts.import_export.models.ImportResponseHandling
import ch.rmy.android.http_shortcuts.import_export.models.ImportSection
import ch.rmy.android.http_shortcuts.import_export.models.ImportShortcut
import ch.rmy.android.http_shortcuts.import_export.models.ImportVariable
import ch.rmy.android.http_shortcuts.import_export.models.ImportWorkingDirectory
import ch.rmy.android.testutils.ResourceLoader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ImporterTest {

    @Suppress("unused")
    @RelaxedMockK
    private lateinit var context: Context

    @Suppress("unused")
    @RelaxedMockK
    private lateinit var importRepository: ImportRepository

    @Suppress("unused")
    private val importMigrator = ImportMigrator()

    @Suppress("unused")
    private val importExportDefaultsProvider = ImportExportDefaultsProvider()

    @InjectMockKs
    private lateinit var importer: Importer

    @Test
    fun `import from version 90`() = runTest {
        var importBase: ImportBase? = null
        coEvery { importRepository.import(any(), any()) } answers { importBase = firstArg() }
        val status = importer.importFromJSON(
            inputStream = ResourceLoader.getStream("shortcuts-v90.json"),
            importMode = ImportMode.MERGE,
        )

        assertEquals(
            Importer.ImportStatus(importedShortcuts = 3),
            status,
        )
        coVerify(exactly = 1) { importRepository.import(base = any(), mode = ImportMode.MERGE) }
        assertEquals(
            ImportBase(
                version = 90,
                compatibilityVersion = 90,
                categories = listOf(
                    ImportCategory(
                        id = "a2f1bb79-167c-49a6-9dc7-6c57d15a31a2",
                        name = "Shortcuts",
                        iconName = null,
                        layoutType = "medium_grid",
                        background = "color\u003d#FF0A11",
                        hidden = null,
                        scale = 1f,
                        shortcutClickBehavior = null,
                        sections = listOf(
                            ImportSection(
                                id = "ae5b6c9b-2cee-488f-9eb6-78c1446420c0",
                                name = "Section 1",
                            ),
                        ),
                        shortcuts = listOf(
                            ImportShortcut(
                                id = "beae9b20-af4a-4dea-991e-532683452923",
                                executionType = "app",
                                name = "HTTP",
                                iconName = "flat_color_rocket",
                                method = "POST",
                                url = "https://example.com/post?data\u003d{{a2252411-cf14-4420-b79b-efe71cc59280}}",
                                username = "username",
                                password = "password",
                                authToken = "",
                                requestBodyType = "custom_text",
                                bodyContent = "hello",
                                launcherShortcut = true,
                                timeout = 10_000,
                                authentication = "basic",
                                contentType = "text/plain",
                                responseHandling = ImportResponseHandling(
                                    actions = listOf("rerun", "share"),
                                    uiType = "window",
                                    successOutput = "response",
                                    successMessage = "",
                                    failureOutput = "simple",
                                    jsonArrayAsTable = true,
                                    storeDirectoryId = "ab85cc28-d159-4223-9c3f-634fc9d2f8c0",
                                    storeFileName = "foo",
                                ),
                                confirmation = "simple",
                                followRedirects = true,
                                acceptCookies = true,
                                codeOnPrepare = "",
                                codeOnSuccess = "alert(\"success\")",
                                codeOnFailure = "",
                                wolPort = 9,
                                wolBroadcastAddress = "255.255.255.255",
                                headers = listOf(
                                    ImportHeader(
                                        key = "User-Agent",
                                        value = "foo",
                                    ),
                                ),
                                parameters = emptyList(),
                            ),
                            ImportShortcut(
                                id = "38caa0c5-9230-403f-a7a2-23b490527562",
                                name = "Browser",
                                executionType = "browser",
                                url = "https://example.com",
                                browserPackageName = "custom-tabs(com.android.chrome)",
                                excludeFromHistory = true,
                                iconName = "flat_color_star",
                                quickSettingsTileShortcut = true,
                                launcherShortcut = true,
                                method = "GET",
                                timeout = 10_000,
                                requestBodyType = "custom_text",
                                followRedirects = true,
                                acceptCookies = true,
                                wolPort = 9,
                                wolBroadcastAddress = "255.255.255.255",
                                bodyContent = "",
                                codeOnPrepare = "",
                                codeOnSuccess = "",
                                codeOnFailure = "",
                                authToken = "",
                                username = "",
                                password = "",
                                headers = emptyList(),
                                parameters = emptyList(),
                            ),
                            ImportShortcut(
                                id = "d30a31ba-1e13-41ee-a8c5-41a99b12b749",
                                name = "Scripting",
                                description = "description",
                                iconName = "flat_color_lightbulb_3",
                                executionType = "scripting",
                                section = "ae5b6c9b-2cee-488f-9eb6-78c1446420c0",
                                launcherShortcut = false,
                                codeOnPrepare = ";",
                                method = "GET",
                                timeout = 10_000,
                                requestBodyType = "custom_text",
                                followRedirects = true,
                                acceptCookies = true,
                                wolPort = 9,
                                wolBroadcastAddress = "255.255.255.255",
                                headers = emptyList(),
                                parameters = emptyList(),
                                url = "",
                                bodyContent = "",
                                codeOnSuccess = "",
                                codeOnFailure = "",
                                authToken = "",
                                username = "",
                                password = "",
                            ),
                        ),
                    ),
                ),
                variables = listOf(
                    ImportVariable(
                        id = "a2252411-cf14-4420-b79b-efe71cc59280",
                        key = "Select",
                        type = "select",
                        value = null,
                        data = "{\"labels\":[\"A\",\"B\"],\"values\":[\"a\",\"b\"],\"multi_select\":\"false\",\"separator\":\",\"}",
                        rememberValue = null,
                        urlEncode = true,
                        jsonEncode = null,
                        title = null,
                        message = null,
                        isShareText = null,
                        isShareTitle = null,
                        isMultiline = null,
                        isExcludeValueFromExport = null,
                    ),
                ),
                certificatePins = listOf(
                    ImportCertificatePin(
                        id = "46bdc6dc-dcb8-479b-aa2e-379cf8c71a72",
                        pattern = "example.com",
                        hash = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                    ),
                ),
                workingDirectories = listOf(
                    ImportWorkingDirectory(
                        id = "ab85cc28-d159-4223-9c3f-634fc9d2f8c0",
                        name = "Sync",
                        directory = "content://com.android.externalstorage.documents/tree/primary%3ASync",
                    ),
                ),
                title = "title",
                globalCode = ";;;",
            ),
            importBase,
        )
    }
}
