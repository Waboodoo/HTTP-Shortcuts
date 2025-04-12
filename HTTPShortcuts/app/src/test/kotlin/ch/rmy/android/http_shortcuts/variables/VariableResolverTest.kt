package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import ch.rmy.android.testutils.DefaultModels
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class VariableResolverTest {

    @RelaxedMockK
    private lateinit var variableTypeFactory: VariableTypeFactory

    private val resolutionOrder = mutableListOf<String>()

    @BeforeTest
    fun setUp() {
        resolutionOrder.clear()
        every { variableTypeFactory.getType(any()) } answers {
            mockk {
                coEvery { resolve(any(), any()) } answers {
                    val variable = firstArg<Variable>()
                    resolutionOrder.add(variable.id)
                    variable.value.orEmpty()
                }
            }
        }
    }

    @Test
    fun `test variable resolution of static variables`() = runTest {
        val variableManager = VariableManager(
            listOf(
                variable(id = "1234", key = "myVariable", value = "Hello World"),
            ),
        )
        VariableResolver(variableTypeFactory)
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = VariableResolver.extractVariableIdsExcludingScripting(
                    shortcutWithContent("{{1234}}"),
                    headers = emptyList(),
                    parameters = emptyList(),
                ),
                dialogHandle = mockk(),
            )

        assertEquals(
            "Hello World",
            variableManager.getVariableValueById("1234"),
        )
        assertEquals(
            "Hello World",
            variableManager.getVariableValueByKey("myVariable"),
        )
        assertEquals(
            "Hello World",
            variableManager.getVariableValueByKeyOrId("1234"),
        )
        assertEquals(
            "Hello World",
            variableManager.getVariableValueByKeyOrId("myVariable"),
        )
    }

    @Test
    fun `test variable resolution of static variables referencing other static variables`() = runTest {
        val variableManager = VariableManager(
            listOf(
                variable(id = "1234", key = "myVariable1", value = "Hello {{5678}}"),
                variable(id = "5678", key = "myVariable2", value = "World"),
            ),
        )
        VariableResolver(variableTypeFactory)
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = VariableResolver.extractVariableIdsExcludingScripting(
                    shortcutWithContent("{{1234}}"),
                    headers = emptyList(),
                    parameters = emptyList(),
                ),
                dialogHandle = mockk(),
            )

        assertEquals(
            "World",
            variableManager.getVariableValueById("5678"),
        )
        assertEquals(
            "World",
            variableManager.getVariableValueByKey("myVariable2"),
        )
        assertEquals(
            "World",
            variableManager.getVariableValueByKeyOrId("5678"),
        )
        assertEquals(
            "World",
            variableManager.getVariableValueByKeyOrId("myVariable2"),
        )

        assertEquals(
            "Hello World",
            variableManager.getVariableValueById("1234"),
        )
        assertEquals(
            "Hello World",
            variableManager.getVariableValueByKey("myVariable1"),
        )
        assertEquals(
            "Hello World",
            variableManager.getVariableValueByKeyOrId("1234"),
        )
        assertEquals(
            "Hello World",
            variableManager.getVariableValueByKeyOrId("myVariable1"),
        )
    }

    @Test
    fun `test variable resolution of static variable references in JS code`() {
        val shortcut = shortcutWithJSContent(
            content = """
            const foo = getVariable(/*[variable]*/"1234"/*[/variable]*/);
            getVariable("my_variable");
            """.trimIndent(),
        )
        val variableLookup = object : VariableLookup {
            override fun getVariableById(id: String): Variable? =
                when (id) {
                    "1234" -> mockk()
                    else -> null
                }

            override fun getVariableByKey(key: String): Variable? =
                when (key) {
                    "my_variable" -> mockk {
                        every { id } returns "5678"
                    }
                    else -> null
                }
        }
        val variableIds = VariableResolver.extractVariableIdsIncludingScripting(
            shortcut,
            headers = emptyList(),
            parameters = emptyList(),
            variableLookup = variableLookup,
        )

        assertEquals(
            setOf("1234", "5678"),
            variableIds,
        )
    }

    @Test
    fun `test variable resolution order`() = runTest {
        val variableManager = VariableManager(
            listOf(
                variable(id = "123", key = "myVariable1", value = "Hello {{789}}"),
                variable(id = "456", key = "myVariable2", value = "!!!"),
                variable(id = "789", key = "myVariable2", value = "World"),
            ),
        )
        VariableResolver(variableTypeFactory)
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = setOf("123", "456"),
                dialogHandle = mockk(),
            )

        assertEquals(
            listOf("123", "789", "456"),
            resolutionOrder,
        )
        assertEquals(
            mapOf(
                "123" to "Hello World",
                "456" to "!!!",
                "789" to "World",
            ),
            variableManager.getVariableValues().mapKeys { it.key.id },
        )
    }

    @Test
    fun `test multi-level recursion variable`() = runTest {
        val variableManager = VariableManager(
            listOf(
                variable(id = "123", key = "myVariable1", value = "Hello {{456}}"),
                variable(id = "456", key = "myVariable2", value = "World{{789}}"),
                variable(id = "789", key = "myVariable2", value = "!!!"),
            ),
        )
        VariableResolver(variableTypeFactory)
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = setOf("123"),
                dialogHandle = mockk(),
            )

        assertEquals(
            mapOf(
                "123" to "Hello World!!!",
                "456" to "World!!!",
                "789" to "!!!",
            ),
            variableManager.getVariableValues().mapKeys { it.key.id },
        )
    }

    @Test
    fun `test self-referential variable`() = runTest {
        val variableManager = VariableManager(
            listOf(
                variable(id = "123", key = "myVariable1", value = "Hello {{123}}"),
            ),
        )
        VariableResolver(variableTypeFactory)
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = setOf("123"),
                dialogHandle = mockk(),
            )

        assertEquals(
            mapOf(
                "123" to "Hello Hello Hello {{123}}",
            ),
            variableManager.getVariableValues().mapKeys { it.key.id },
        )
    }

    companion object {

        private fun variable(id: String, key: String, value: String): Variable =
            DefaultModels.variable.copy(
                id = id,
                key = key,
                value = value,
            )

        private fun shortcutWithContent(content: String): Shortcut =
            DefaultModels.shortcut.copy(
                method = HttpMethod.POST,
                bodyContent = content,
            )

        private fun shortcutWithJSContent(content: String): Shortcut =
            DefaultModels.shortcut.copy(
                method = HttpMethod.POST,
                codeOnSuccess = content,
            )
    }
}
