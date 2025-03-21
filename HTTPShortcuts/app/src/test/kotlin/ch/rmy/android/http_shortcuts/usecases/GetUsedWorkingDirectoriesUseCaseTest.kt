package ch.rmy.android.http_shortcuts.usecases

import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
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
            WorkingDirectory(id = "a", name = "dir 1"),
            WorkingDirectory(id = "b", name = "dir2"),
            WorkingDirectory(id = "c", name = "dir 3"),
            WorkingDirectory(id = "d", name = "dir 4"),
            WorkingDirectory(id = "e", name = "dir 5"),
            WorkingDirectory(id = "f", name = "dir 6"),
        )

        val base = Base()
        val appConfig = AppConfig(
            globalCode = """
                const foo = getDirectory('dir 1');
                const bar = getDirectory("dir2");
            """,
        )
        val category = Category()
        category.shortcuts.addAll(
            listOf(
                Shortcut()
                    .apply {
                        responseHandling = ResponseHandling()
                            .apply {
                                storeDirectoryId = "c"
                            }
                    },
                Shortcut().apply {
                    codeOnPrepare = """
                        getDirectory("Dir 4");
                    """
                },
                Shortcut().apply {
                    codeOnSuccess = """
                        getDirectory("e");
                    """
                },
                Shortcut().apply {
                    codeOnSuccess = """
                        getDirectory("x");
                    """
                },
            ),
        )
        base.categories.add(category)

        assertEquals(
            setOf(
                "a",
                "b",
                "c",
                "d",
                "e",
            ),
            useCase.invoke(base, appConfig),
        )
    }
}
