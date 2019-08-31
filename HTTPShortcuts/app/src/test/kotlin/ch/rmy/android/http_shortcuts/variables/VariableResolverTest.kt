package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class VariableResolverTest {

    private val context = RuntimeEnvironment.application.applicationContext

    @Test
    fun testVariableResolutionOfConstants() {
        val variableManager = VariableResolver(context)
            .resolve(
                variables = listOf(
                    Variable(id = "1234", key = "myVariable", value = "Hello World")
                ),
                shortcut = withContent("{{1234}}")
            )
            .blockingGet()!!

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
    fun testVariableResolutionOfConstantsReferencingOtherConstant() {
        val variableManager = VariableResolver(context)
            .resolve(
                variables = listOf(
                    Variable(id = "1234", key = "myVariable1", value = "Hello {{5678}}"),
                    Variable(id = "5678", key = "myVariable2", value = "World")
                ),
                shortcut = withContent("{{1234}}")
            )
            .blockingGet()!!

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
    fun testVariableResolutionOfConstantsInJSCodeByReferencingVariableKey() {
        val variableManager = VariableResolver(context)
            .resolve(
                variables = listOf(
                    Variable(id = "1234", key = "myVariable", value = "Hello World")
                ),
                shortcut = withJSContent("getVariable(\"myVariable\")")
            )
            .blockingGet()!!

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
    fun testVariableResolutionOfConstantsInJSCodeByReferencingVariableId() {
        val variableManager = VariableResolver(context)
            .resolve(
                variables = listOf(
                    Variable(id = "1234", key = "myVariable", value = "Hello World")
                ),
                shortcut = withJSContent("getVariable(/*[variable]*/\"1234\"/*[/variable]*/)")
            )
            .blockingGet()!!

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

    companion object {

        private fun withContent(content: String) =
            Shortcut.createNew().apply {
                method = Shortcut.METHOD_POST
                requestBodyType = Shortcut.REQUEST_BODY_TYPE_CUSTOM_TEXT
                bodyContent = content
            }

        private fun withJSContent(content: String) =
            Shortcut.createNew().apply {
                codeOnSuccess = content
            }

    }

}