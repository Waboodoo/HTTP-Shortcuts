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
class IncrementTypeTest {

    @RelaxedMockK
    lateinit var variablesRepository: GlobalVariableRepository

    @RelaxedMockK
    lateinit var dialogHandle: DialogHandle

    @InjectMockKs
    lateinit var incrementType: IncrementType

    @Test
    fun `increment uses the latest persisted value`() = runTest {
        val variable = DefaultModels.variable.copy(id = "variable", value = "1")
        coEvery { variablesRepository.updateVariableValue(variable.id, any()) } answers {
            secondArg<(String?) -> String>().invoke("41")
        }

        assertEquals("42", incrementType.resolve(variable, dialogHandle))
    }

    @Test
    fun `invalid value is treated as zero`() = runTest {
        val variable = DefaultModels.variable.copy(id = "variable", value = "invalid")
        coEvery { variablesRepository.updateVariableValue(variable.id, any()) } answers {
            secondArg<(String?) -> String>().invoke("invalid")
        }

        assertEquals("1", incrementType.resolve(variable, dialogHandle))
    }
}
