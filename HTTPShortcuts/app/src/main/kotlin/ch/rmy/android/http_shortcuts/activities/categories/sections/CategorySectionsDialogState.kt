package ch.rmy.android.http_shortcuts.activities.categories.sections

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId

@Stable
sealed class CategorySectionsDialogState {
    @Stable
    data object AddSection : CategorySectionsDialogState()

    @Stable
    data class EditSection(
        val id: SectionId,
        val name: String,
    ) : CategorySectionsDialogState()
}
