package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class VariableResolverTest {

    private val resolutionOrder = mutableListOf<String>()

    @BeforeTest
    fun setUp() {
        resolutionOrder.clear()
        mockkObject(VariableTypeFactory)
        every { VariableTypeFactory.getType(any()) } answers {
            mockk {
                coEvery { resolve(any(), any()) } answers {
                    val variable = secondArg<VariableModel>()
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
                VariableModel(id = "1234", key = "myVariable", value = "Hello World")
            )
        )
        VariableResolver(mockk())
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = VariableResolver.extractVariableIds(
                    withContent("{{1234}}"),
                    variableManager,
                    includeScripting = false,
                )
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
                VariableModel(id = "1234", key = "myVariable1", value = "Hello {{5678}}"),
                VariableModel(id = "5678", key = "myVariable2", value = "World")
            )
        )
        VariableResolver(mockk(relaxed = true))
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = VariableResolver.extractVariableIds(
                    withContent("{{1234}}"),
                    variableManager,
                    includeScripting = false,
                )
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
        val shortcut = withJSContent(
            content = """
            const foo = getVariable(/*[variable]*/"1234"/*[/variable]*/);
            getVariable("my_variable");
            """.trimIndent()
        )
        val variableLookup = object : VariableLookup {
            override fun getVariableById(id: String): VariableModel? =
                when (id) {
                    "1234" -> mockk()
                    else -> null
                }

            override fun getVariableByKey(key: String): VariableModel? =
                when (key) {
                    "my_variable" -> mockk {
                        every { id } returns "5678"
                    }
                    else -> null
                }
        }
        val variableIds = VariableResolver.extractVariableIds(shortcut, variableLookup)

        assertEquals(
            setOf("1234", "5678"),
            variableIds,
        )
    }

    @Test
    fun `test variable resolution order`() = runTest {
        val variableManager = VariableManager(
            listOf(
                VariableModel(id = "123", key = "myVariable1", value = "Hello {{789}}"),
                VariableModel(id = "456", key = "myVariable2", value = "!!!"),
                VariableModel(id = "789", key = "myVariable2", value = "World"),
            )
        )
        VariableResolver(mockk(relaxed = true))
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = setOf("123", "456"),
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
                VariableModel(id = "123", key = "myVariable1", value = "Hello {{456}}"),
                VariableModel(id = "456", key = "myVariable2", value = "World{{789}}"),
                VariableModel(id = "789", key = "myVariable2", value = "!!!"),
            )
        )
        VariableResolver(mockk(relaxed = true))
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = setOf("123"),
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
                VariableModel(id = "123", key = "myVariable1", value = "Hello {{123}}"),
            )
        )
        VariableResolver(mockk(relaxed = true))
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = setOf("123"),
            )

        assertEquals(
            mapOf(
                "123" to "Hello Hello Hello {{123}}",
            ),
            variableManager.getVariableValues().mapKeys { it.key.id },
        )
    }

    companion object {

        private fun withContent(content: String) =
            ShortcutModel().apply {
                method = ShortcutModel.METHOD_POST
                bodyType = RequestBodyType.CUSTOM_TEXT
                bodyContent = content
            }

        private fun withJSContent(content: String) =
            ShortcutModel().apply {
                codeOnSuccess = content
            }
    }
}
