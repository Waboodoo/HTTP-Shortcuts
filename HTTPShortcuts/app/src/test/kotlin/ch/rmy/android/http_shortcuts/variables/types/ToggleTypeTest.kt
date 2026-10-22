package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
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
class ToggleTypeTest {

    @RelaxedMockK
    lateinit var variablesRepository: GlobalVariableRepository

    @RelaxedMockK
    lateinit var dialogHandle: DialogHandle

    @InjectMockKs
    lateinit var toggleType: ToggleType

    @Test
    fun `toggle uses the latest persisted index`() = runTest {
        val variable = DefaultModels.variable.copy(
            id = "variable",
            value = "0",
            data = """{"values":["First","Second","Third"]}""",
        )
        coEvery { variablesRepository.updateVariableValue(variable.id, any()) } answers {
            secondArg<(String?) -> String>().invoke("1")
        }

        assertEquals("Third", toggleType.resolve(variable, dialogHandle))
    }

    @Test
    fun `toggle wraps around`() = runTest {
        val variable = DefaultModels.variable.copy(
            id = "variable",
            data = """{"values":["First","Second","Third"]}""",
        )
        coEvery { variablesRepository.updateVariableValue(variable.id, any()) } answers {
            secondArg<(String?) -> String>().invoke("2")
        }

        assertEquals("First", toggleType.resolve(variable, dialogHandle))
    }
}
