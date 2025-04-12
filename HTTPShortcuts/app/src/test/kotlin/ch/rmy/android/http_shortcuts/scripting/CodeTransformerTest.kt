package ch.rmy.android.http_shortcuts.scripting

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.testutils.DefaultModels
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CodeTransformerTest {

    @RelaxedMockK
    lateinit var shortcutRepository: ShortcutRepository

    @RelaxedMockK
    lateinit var variableRepository: VariableRepository

    @InjectMockKs
    lateinit var codeTransformer: CodeTransformer

    @BeforeTest
    fun setUp() {
        coEvery { shortcutRepository.getShortcuts() } returns listOf(
            mockShortcut(id = ID1, name = "My Shortcut"),
            mockShortcut(id = ID2, name = "My \"Shortcut\""),
            mockShortcut(id = ID3, name = "My 'Shortcut'"),
            mockShortcut(id = ID4, name = "My \\\"Shortcut\\\""),
        )
        coEvery { variableRepository.getVariables() } returns listOf(
            mockVariable(id = ID1, key = "my_variable"),
        )
    }

    @Test
    fun `variable id is transformed for editing`() = runTest {
        val result = codeTransformer.transformForEditing(
            """
            const x = getVariable(/*[variable]*/"$ID1"/*[/variable]*/);
            setVariable(/*[variable]*/"$ID1"/*[/variable]*/, "foo");
            """.trimIndent(),
        )
        assertEquals(
            """
            const x = getVariable("my_variable");
            setVariable("my_variable", "foo");
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `variable id that is not found is not transformed for editing`() = runTest {
        val result = codeTransformer.transformForEditing(
            """
            const x = getVariable(/*[variable]*/"$ID2"/*[/variable]*/);
            setVariable(/*[variable]*/"$ID3"/*[/variable]*/, "foo");
            """.trimIndent(),
        )
        assertEquals(
            """
            const x = getVariable("$ID2");
            setVariable("$ID3", "foo");
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `variable key is transformed for storing`() = runTest {
        val result = codeTransformer.transformForStoring(
            """
            const x = getVariable("my_variable");
            setVariable("my_variable", "foo");
            const y = getVariable('my_variable');
            setVariable('my_variable', 'foo');
            """.trimIndent(),
        )
        assertEquals(
            """
            const x = getVariable(/*[variable]*/"$ID1"/*[/variable]*/);
            setVariable(/*[variable]*/"$ID1"/*[/variable]*/, "foo");
            const y = getVariable(/*[variable]*/"$ID1"/*[/variable]*/);
            setVariable(/*[variable]*/"$ID1"/*[/variable]*/, 'foo');
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `variable key that is not found is not transformed for storing`() = runTest {
        val result = codeTransformer.transformForStoring(
            """
            const x = getVariable("my_variable2");
            setVariable("my_Variable", "foo");
            setVariable("my_variable" + "", "foo");
            """.trimIndent(),
        )
        assertEquals(
            """
            const x = getVariable("my_variable2");
            setVariable("my_Variable", "foo");
            setVariable("my_variable" + "", "foo");
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `shortcut ids transformed for editing`() = runTest {
        val result = codeTransformer.transformForEditing(
            """
                triggerShortcut(/*[shortcut]*/"$ID1"/*[/shortcut]*/);
                renameShortcut(/*[shortcut]*/"$ID2"/*[/shortcut]*/, "Test");
                changeIcon(/*[shortcut]*/"$ID3"/*[/shortcut]*/, "new_icon");
                changeDescription(/*[shortcut]*/"$ID4"/*[/shortcut]*/, "...");
            """.trimIndent(),
        )
        assertEquals(
            """
                triggerShortcut("My Shortcut");
                renameShortcut("My \"Shortcut\"", "Test");
                changeIcon("My 'Shortcut'", "new_icon");
                changeDescription("My \\\"Shortcut\\\"", "...");
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `shortcut ids that are not found are not transformed for editing`() = runTest {
        val result = codeTransformer.transformForEditing(
            """
                triggerShortcut(/*[shortcut]*/"$ID5"/*[/shortcut]*/);
                renameShortcut(/*[shortcut]*/"$ID5"/*[/shortcut]*/, "Test");
                changeIcon(/*[shortcut]*/"$ID5"/*[/shortcut]*/, "new_icon");
                changeDescription(/*[shortcut]*/"$ID5"/*[/shortcut]*/, "...");
            """.trimIndent(),
        )
        assertEquals(
            """
                triggerShortcut("$ID5");
                renameShortcut("$ID5", "Test");
                changeIcon("$ID5", "new_icon");
                changeDescription("$ID5", "...");
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `shortcut names are transformed for storing`() = runTest {
        val result = codeTransformer.transformForStoring(
            """
                triggerShortcut("My Shortcut");
                renameShortcut("My \"Shortcut\"", "Test");
                changeIcon("My 'Shortcut'", "new_icon");
                changeDescription("My \\\"Shortcut\\\"", "...");
                triggerShortcut('My Shortcut');
                renameShortcut('My "Shortcut"', 'Test');
                changeIcon('My \'Shortcut\'', 'new_icon');
                changeDescription('My \\"Shortcut\\"', '...');
            """.trimIndent(),
        )
        assertEquals(
            """
                triggerShortcut(/*[shortcut]*/"$ID1"/*[/shortcut]*/);
                renameShortcut(/*[shortcut]*/"$ID2"/*[/shortcut]*/, "Test");
                changeIcon(/*[shortcut]*/"$ID3"/*[/shortcut]*/, "new_icon");
                changeDescription(/*[shortcut]*/"$ID4"/*[/shortcut]*/, "...");
                triggerShortcut(/*[shortcut]*/"$ID1"/*[/shortcut]*/);
                renameShortcut(/*[shortcut]*/"$ID2"/*[/shortcut]*/, 'Test');
                changeIcon(/*[shortcut]*/"$ID3"/*[/shortcut]*/, 'new_icon');
                changeDescription(/*[shortcut]*/"$ID4"/*[/shortcut]*/, '...');
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `shortcut names that are not found are not transformed for storing`() = runTest {
        val result = codeTransformer.transformForStoring(
            """
                triggerShortcut("My shortcut");
                triggerShortcut("My Shortcut" + "");
                renameShortcut("My  \"Shortcut\"", "Test");
                changeIcon('My \'shortcut\'', 'new_icon');
                changeDescription('My \\"Shortcut\\"2', '...');
            """.trimIndent(),
        )
        assertEquals(
            """
                triggerShortcut("My shortcut");
                triggerShortcut("My Shortcut" + "");
                renameShortcut("My  \"Shortcut\"", "Test");
                changeIcon('My \'shortcut\'', 'new_icon');
                changeDescription('My \\"Shortcut\\"2', '...');
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `marker comments are removed for execution`() {
        val result = codeTransformer.transformForExecuting(
            """
           const x = getVariable(/*[variable]*/"$ID1"/*[/variable]*/);
           /* setVariable(/*[variable]*/"$ID1"/*[/variable]*/, "foo"); */
           /* my comment */
           renameShortcut(/*[shortcut]*/"$ID2"/*[/shortcut]*/, "new name");
            """.trimIndent(),
        )
        assertEquals(
            """
            const x = getVariable("$ID1");
            /* setVariable("$ID1", "foo"); */
            /* my comment */
            renameShortcut("$ID2", "new name");
            """.trimIndent(),
            result,
        )
    }

    companion object {

        private const val ID1 = "d1a245d5-8aac-4e44-b9b5-c80f7da06076"
        private const val ID2 = "4a1adabe-23fc-4491-8828-dcb36c55d38b"
        private const val ID3 = "24bbce50-8152-4b5e-b47e-7190f08281c3"
        private const val ID4 = "6925fddb-1e20-4d76-8f0f-e71ca992acb3"
        private const val ID5 = "98c8ddfa-dd18-41ec-997d-87a058e78da7"

        private fun mockShortcut(id: ShortcutId, name: String): Shortcut =
            DefaultModels.shortcut.copy(
                id = id,
                name = name,
            )

        private fun mockVariable(id: VariableId, key: VariableKey): Variable =
            DefaultModels.variable.copy(
                id = id,
                key = key,
            )
    }
}
