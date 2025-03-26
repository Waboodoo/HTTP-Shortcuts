package ch.rmy.android.http_shortcuts.activities.categories.sections

import android.app.Application
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.activities.categories.sections.models.CategorySectionListItem
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionRepository
import ch.rmy.android.http_shortcuts.data.models.Section
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategorySectionsViewModel
@Inject
constructor(
    application: Application,
    private val sectionRepository: SectionRepository,
) : BaseViewModel<CategorySectionsViewModel.InitData, CategorySectionsViewState>(application) {

    private val categoryId: CategoryId
        get() = initData.categoryId

    private var sections: List<Section> = emptyList()

    private suspend fun updateSections(sections: List<Section>) {
        this.sections = sections
        updateViewState {
            copy(
                sectionItems = sections.toSectionItem(),
            )
        }
    }

    override suspend fun initialize(data: InitData): CategorySectionsViewState {
        sections = sectionRepository.getSections(categoryId)
        return CategorySectionsViewState(
            sectionItems = sections.toSectionItem(),
        )
    }

    private fun List<Section>.toSectionItem() =
        map { section ->
            CategorySectionListItem(
                id = section.id,
                name = section.name,
            )
        }

    fun onSectionMoved(sectionId1: SectionId, sectionId2: SectionId) = runAction {
        updateSections(sections.swapped(sectionId1, sectionId2) { id })
        withProgressTracking {
            sectionRepository.moveSection(sectionId1, sectionId2)
        }
    }

    fun onAddSectionButtonClicked() = runAction {
        updateDialogState(CategorySectionsDialogState.AddSection)
    }

    fun onDialogConfirmed(name: String) = runAction {
        when (val dialogState = viewState.dialogState) {
            is CategorySectionsDialogState.AddSection -> onAddSectionDialogConfirmed(name)
            is CategorySectionsDialogState.EditSection -> onEditSectionDialogConfirmed(dialogState.id, name)
            else -> Unit
        }
    }

    private suspend fun ViewModelScope<*>.onAddSectionDialogConfirmed(name: String) {
        updateDialogState(null)
        withProgressTracking {
            val newSection = sectionRepository.addSection(categoryId, name)
            updateSections(sections.plus(newSection))
        }
    }

    private suspend fun ViewModelScope<*>.onEditSectionDialogConfirmed(sectionId: SectionId, name: String) {
        updateDialogState(null)
        updateSections(
            sections
                .map { section ->
                    if (section.id == sectionId) {
                        section.copy(name = name)
                    } else {
                        section
                    }
                },
        )
        withProgressTracking {
            sectionRepository.updateSection(sectionId, name)
        }
    }

    fun onRemoveSectionButtonClicked() = runAction {
        val sectionId = (viewState.dialogState as? CategorySectionsDialogState.EditSection)?.id ?: skipAction()
        updateDialogState(null)
        updateSections(
            sections.filter { section ->
                section.id != sectionId
            },
        )
        withProgressTracking {
            sectionRepository.removeSection(sectionId)
        }
    }

    fun onSectionClicked(id: SectionId) = runAction {
        sections
            .firstOrNull { section ->
                section.id == id
            }
            ?.let { section ->
                updateDialogState(
                    CategorySectionsDialogState.EditSection(
                        id = section.id,
                        name = section.name,
                    ),
                )
            }
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
    }

    fun onDismissDialog() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: CategorySectionsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(val categoryId: CategoryId)
}
