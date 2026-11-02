package ch.rmy.android.http_shortcuts.data.domains.widgets

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.enums.WidgetBackgroundType
import ch.rmy.android.http_shortcuts.data.models.VariableWidget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class VariableWidgetsRepositoryTest {

    private val widgetDao = mockk<VariableWidgetDao>()
    private val database = mockk<Database> {
        every { variableWidgetDao() } returns widgetDao
    }
    private val repository = VariableWidgetsRepository(database)

    @Test
    fun `clear shortcut preserves widgets and only updates matching actions once`() = runTest {
        val affectedWidgets = listOf(
            VariableWidget(1, "variable-1", 22, "First", "deleted", WidgetBackgroundType.Color(0xff123456.toInt())),
            VariableWidget(2, "variable-2", 30, "Second", "deleted"),
        )
        val otherWidgets = listOf(
            VariableWidget(3, "variable-3", 18, "Other", "other"),
            VariableWidget(4, "variable-4", 24, "Display only"),
        )
        val widgets = (affectedWidgets + otherWidgets).associateBy { it.widgetId }.toMutableMap()
        val writes = mutableListOf<VariableWidget>()
        coEvery { widgetDao.getWidgets() } answers { widgets.values.toList() }
        coEvery { widgetDao.insertOrReplace(capture(writes)) } answers {
            firstArg<VariableWidget>().let { widgets[it.widgetId] = it }
        }

        assertEquals(listOf(1, 2), repository.clearShortcutFromVariableWidgets("deleted"))
        assertEquals(affectedWidgets.map { it.copy(shortcutId = null) }, writes)
        assertEquals(affectedWidgets.map { it.copy(shortcutId = null) } + otherWidgets, widgets.values.toList())

        assertEquals(emptyList(), repository.clearShortcutFromVariableWidgets("deleted"))
        coVerify(exactly = 2) { widgetDao.insertOrReplace(any()) }
    }

    @Test
    fun `clear shortcut with no widgets is a no-op`() = runTest {
        coEvery { widgetDao.getWidgets() } returns emptyList()

        assertEquals(emptyList(), repository.clearShortcutFromVariableWidgets("deleted"))

        coVerify(exactly = 0) { widgetDao.insertOrReplace(any()) }
    }

    @Test
    fun `clear shortcut propagates DAO failures`() = runTest {
        val failure = IllegalStateException("Database unavailable")
        coEvery { widgetDao.getWidgets() } throws failure

        assertSame(failure, assertFailsWith<IllegalStateException> { repository.clearShortcutFromVariableWidgets("deleted") })

        coEvery { widgetDao.getWidgets() } returns listOf(VariableWidget(1, "variable", 22, "Title", "deleted"))
        coEvery { widgetDao.insertOrReplace(any()) } throws failure

        assertSame(failure, assertFailsWith<IllegalStateException> { repository.clearShortcutFromVariableWidgets("deleted") })
    }
}
