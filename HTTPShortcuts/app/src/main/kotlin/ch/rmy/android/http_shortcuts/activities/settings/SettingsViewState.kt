package ch.rmy.android.http_shortcuts.activities.settings

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.AvailableBrowserPackageNamesLookup

@Stable
data class MediaControlCategoryOption(
    val id: CategoryId,
    val name: String,
    val icon: ShortcutIcon?,
)

@Stable
data class SettingsViewState(
    val dialogState: SettingsDialogState? = null,
    val privacySectionVisible: Boolean,
    val quickSettingsTileButtonVisible: Boolean,
    val selectedLanguage: String?,
    val selectedDarkModeOption: String,
    val selectedClickActionOption: ShortcutClickBehavior,
    val crashReportingAllowed: Boolean,
    val colorTheme: String,
    val showHiddenShortcuts: Boolean,
    val mediaControlsEnabled: Boolean,
    val mediaControlCategories: List<MediaControlCategoryOption>,
    val mediaControlsIncludeAllCategories: Boolean,
    val mediaControlSelectedCategoryIds: Set<CategoryId>,
    val rememberActiveCategory: Boolean,
    val rememberActiveCategoryEnabled: Boolean,
    val hasLock: Boolean,
    val isInSyncReplaceMode: Boolean,
    val translationProgress: Map<String, Int>,
    val browserPackageNameOptions: List<AvailableBrowserPackageNamesLookup.InstalledBrowser> = emptyList(),
    val defaultBrowser: TargetBrowser.Browser,
)
