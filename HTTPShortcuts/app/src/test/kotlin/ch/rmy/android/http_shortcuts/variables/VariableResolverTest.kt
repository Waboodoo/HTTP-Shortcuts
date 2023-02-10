package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.test.TestContext
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class VariableResolverTest {

    private val context = TestContext.create()

    @Test
    fun `test variable resolution of static variables`() = runTest {
        val variableManager = VariableManager(
            listOf(
                VariableModel(id = "1234", key = "myVariable", value = "Hello World")
            )
        )
        VariableResolver(context)
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = VariableResolver.extractVariableIds(
                    withContent("{{1234}}"),
                    variableManager,
                    includeScripting = false,
                )
            )

        assertThat(
            variableManager.getVariableValueById("1234"),
            equalTo("Hello World")
        )
        assertThat(
            variableManager.getVariableValueByKey("myVariable"),
            equalTo("Hello World")
        )
        assertThat(
            variableManager.getVariableValueByKeyOrId("1234"),
            equalTo("Hello World")
        )
        assertThat(
            variableManager.getVariableValueByKeyOrId("myVariable"),
            equalTo("Hello World")
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
        VariableResolver(context)
            .resolve(
                variableManager = variableManager,
                requiredVariableIds = VariableResolver.extractVariableIds(
                    withContent("{{1234}}"),
                    variableManager,
                    includeScripting = false,
                )
            )

        assertThat(
            variableManager.getVariableValueById("5678"),
            equalTo("World")
        )
        assertThat(
            variableManager.getVariableValueByKey("myVariable2"),
            equalTo("World")
        )
        assertThat(
            variableManager.getVariableValueByKeyOrId("5678"),
            equalTo("World")
        )
        assertThat(
            variableManager.getVariableValueByKeyOrId("myVariable2"),
            equalTo("World")
        )

        assertThat(
            variableManager.getVariableValueById("1234"),
            equalTo("Hello World")
        )
        assertThat(
            variableManager.getVariableValueByKey("myVariable1"),
            equalTo("Hello World")
        )
        assertThat(
            variableManager.getVariableValueByKeyOrId("1234"),
            equalTo("Hello World")
        )
        assertThat(
            variableManager.getVariableValueByKeyOrId("myVariable1"),
            equalTo("Hello World")
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
                    "1234" -> mock()
                    else -> null
                }

            override fun getVariableByKey(key: String): VariableModel? =
                when (key) {
                    "my_variable" -> mock {
                        on(mock.id).thenReturn("5678")
                    }
                    else -> null
                }
        }
        val variableIds = VariableResolver.extractVariableIds(shortcut, variableLookup)

        assertThat(
            variableIds,
            equalTo(setOf("1234", "5678"))
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
