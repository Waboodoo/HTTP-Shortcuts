package ch.rmy.android.http_shortcuts.data.domains.sections

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.models.Section
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SectionRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getSections(): List<Section> = query {
        sectionDao().getSections()
    }

    suspend fun getSections(categoryId: SectionId): List<Section> = query {
        sectionDao().getSectionByCategoryId(categoryId)
    }

    fun observeSections(): Flow<List<Section>> = queryFlow {
        sectionDao().observeSections()
    }

    fun observeSections(categoryId: SectionId): Flow<List<Section>> = queryFlow {
        sectionDao().observeSectionsByCategoryId(categoryId)
    }

    suspend fun addSection(categoryId: CategoryId, name: String): Section = commitTransaction {
        val section = Section(
            id = newUUID(),
            name = name.trim(),
            categoryId = categoryId,
            sortingOrder = sectionDao().getSectionCountByCategoryId(categoryId),
        )
        sectionDao().insertOrUpdateSection(section)
        section
    }

    suspend fun moveSection(sectionId1: SectionId, sectionId2: SectionId) = commitTransaction {
        val sectionDao = sectionDao()
        val section1 = sectionDao.getSectionById(sectionId1).firstOrNull() ?: return@commitTransaction
        val section2 = sectionDao.getSectionById(sectionId2).firstOrNull() ?: return@commitTransaction
        assert(section1.categoryId == section2.categoryId)
        val categoryId = section1.categoryId
        if (section1.sortingOrder < section2.sortingOrder) {
            sectionDao.updateSortingOrder(
                categoryId = categoryId,
                from = section1.sortingOrder + 1,
                until = section2.sortingOrder,
                diff = -1,
            )
        } else {
            sectionDao.updateSortingOrder(
                categoryId = categoryId,
                from = section2.sortingOrder,
                until = section1.sortingOrder - 1,
                diff = 1,
            )
        }
        sectionDao.insertOrUpdateSection(section1.copy(sortingOrder = section2.sortingOrder))
    }

    suspend fun updateSection(sectionId: SectionId, name: String) = commitTransaction {
        val sectionDao = sectionDao()
        val section = sectionDao.getSectionById(sectionId).firstOrNull()
            ?: return@commitTransaction

        sectionDao.insertOrUpdateSection(
            section.copy(name = name.trim()),
        )
    }

    suspend fun removeSection(sectionId: SectionId) = commitTransaction {
        val sectionDao = sectionDao()
        val section = sectionDao.getSectionById(sectionId).firstOrNull()
            ?: return@commitTransaction
        sectionDao.deleteSection(sectionId)
        sectionDao.updateSortingOrder(
            section.categoryId,
            from = section.sortingOrder,
            until = Int.MAX_VALUE,
            diff = -1,
        )
    }
}
