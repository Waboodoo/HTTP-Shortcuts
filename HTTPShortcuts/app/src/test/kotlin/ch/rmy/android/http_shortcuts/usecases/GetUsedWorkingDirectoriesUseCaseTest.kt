package ch.rmy.android.http_shortcuts.usecases

import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
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
            workingDirectory(id = "a", "dir 1"),
            workingDirectory(id = "b", "dir2"),
            workingDirectory(id = "c", "dir 3"),
            workingDirectory(id = "d", "dir 4"),
            workingDirectory(id = "e", "dir 5"),
            workingDirectory(id = "f", "dir 6"),
        )

        val base = Base()
        base.globalCode = """
            const foo = getDirectory('dir 1');
            const bar = getDirectory("dir2");
        """
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
            useCase.invoke(base),
        )
    }

    private fun workingDirectory(id: String, name: String): WorkingDirectory =
        WorkingDirectory().apply {
            this.id = id
            this.name = name
        }
}
