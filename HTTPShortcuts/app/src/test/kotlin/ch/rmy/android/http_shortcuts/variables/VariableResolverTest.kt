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

    @BeforeTest
    fun setUp() {
        mockkObject(VariableTypeFactory)
        every { VariableTypeFactory.getType(any()) } answers {
            mockk {
                coEvery { resolve(any(), any()) } answers {
                    secondArg<VariableModel>().value.orEmpty()
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
