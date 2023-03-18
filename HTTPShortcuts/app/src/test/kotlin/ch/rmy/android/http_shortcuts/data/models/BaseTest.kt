package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.test.createBase
import ch.rmy.android.http_shortcuts.test.createCategory
import ch.rmy.android.http_shortcuts.test.createHeader
import ch.rmy.android.http_shortcuts.test.createShortcut
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class BaseTest {

    @Test
    fun `validate succeeds if there are no shortcuts`() {
        val base = createBase(
            categories = listOf(
                createCategory(id = ID1),
            ),
        )

        base.validate()
    }

    @Test
    fun `validate succeeds if there are multiple empty categories`() {
        val base = createBase(
            categories = listOf(
                createCategory(id = ID1),
                createCategory(id = ID2),
            ),
        )

        base.validate()
    }

    @Test
    fun `validate succeeds if there are no duplicate shortcut ids`() {
        val base = createBase(
            categories = listOf(
                createCategory(
                    shortcuts = listOf(
                        createShortcut(id = ID1),
                    )
                ),
                createCategory(
                    shortcuts = listOf(
                        createShortcut(id = ID2),
                    )
                ),
            ),
        )

        base.validate()
    }

    @Test
    fun `validate fails if there are duplicate category ids`() {
        val base = createBase(
            categories = listOf(
                createCategory(id = ID1),
                createCategory(id = ID1),
            ),
        )

        assertThrows<IllegalArgumentException> {
            base.validate()
        }
    }

    @Test
    fun `validate fails if there are duplicate shortcut ids`() {
        val base = createBase(
            categories = listOf(
                createCategory(
                    shortcuts = listOf(
                        createShortcut(id = ID1),
                    )
                ),
                createCategory(
                    shortcuts = listOf(
                        createShortcut(id = ID1),
                    )
                ),
            ),
        )

        assertThrows<IllegalArgumentException> {
            base.validate()
        }
    }

    @Test
    fun `validate fails if there are duplicate header ids`() {
        val base = createBase(
            categories = listOf(
                createCategory(
                    shortcuts = listOf(
                        createShortcut(
                            headers = listOf(
                                createHeader(id = ID1),
                            )
                        ),
                    )
                ),
                createCategory(
                    shortcuts = listOf(
                        createShortcut(
                            headers = listOf(
                                createHeader(id = ID1),
                            )
                        ),
                    )
                ),
            ),
        )

        assertThrows<IllegalArgumentException> {
            base.validate()
        }
    }

    companion object {
        private const val ID1 = "9990448e-0e9d-4266-a06d-6a60309b12e0"
        private const val ID2 = "a054855f-8fb2-46d4-9ee0-96df8091eedc"
    }
}
