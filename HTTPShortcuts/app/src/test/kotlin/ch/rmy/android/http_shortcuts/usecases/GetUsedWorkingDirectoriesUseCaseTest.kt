package ch.rmy.android.http_shortcuts.usecases

import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.testutils.DefaultModels
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetUsedWorkingDirectoriesUseCaseTest {

    @RelaxedMockK
    private lateinit var workingDirectoryRepository: WorkingDirectoryRepository

    @InjectMockKs
    private lateinit var useCase: GetUsedWorkingDirectoryIdsUseCase

    @Test
    fun `get working directories`() = runTest {
        coEvery { workingDirectoryRepository.getWorkingDirectories() } returns listOf(
            workingDirectory(id = "a", name = "dir 1"),
            workingDirectory(id = "b", name = "dir2"),
            workingDirectory(id = "c", name = "dir 3"),
            workingDirectory(id = "d", name = "dir 4"),
            workingDirectory(id = "e", name = "dir 5"),
            workingDirectory(id = "f", name = "dir 6"),
        )

        val appConfig = AppConfig(
            title = "",
            globalCode = """
                const foo = getDirectory('dir 1');
                const bar = getDirectory("dir2");
            """,
        )
        val shortcuts = listOf(
            DefaultModels.shortcut.copy(
                responseStoreDirectoryId = "c",
            ),
            DefaultModels.shortcut.copy(
                codeOnPrepare = """
                    getDirectory("Dir 4");
                """,
            ),
            DefaultModels.shortcut.copy(
                codeOnSuccess = """
                    getDirectory("e");
                """,
            ),
            DefaultModels.shortcut.copy(
                codeOnFailure = """
                    getDirectory("x");
                """,
            ),
        )

        assertEquals(
            setOf(
                "a",
                "b",
                "c",
                "d",
                "e",
            ),
            useCase.invoke(shortcuts, appConfig),
        )
    }

    companion object {
        private fun workingDirectory(id: WorkingDirectoryId, name: String): WorkingDirectory =
            DefaultModels.workingDirectory.copy(
                id = id,
                name = name,
            )
    }
}
