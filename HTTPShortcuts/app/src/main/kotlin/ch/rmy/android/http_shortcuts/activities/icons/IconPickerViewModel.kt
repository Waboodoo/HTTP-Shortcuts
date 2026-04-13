package ch.rmy.android.http_shortcuts.activities.icons

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.models.IconShape
import ch.rmy.android.http_shortcuts.activities.icons.models.MaterialIcon
import ch.rmy.android.http_shortcuts.activities.icons.usecases.FetchAndStoreMaterialIconUseCase
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetIconListItemsUseCase
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.icons.CustomIconName
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.IconUtil.analyzeColors
import ch.rmy.android.http_shortcuts.utils.SearchUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import ch.rmy.iconfetcher.IconFetcher
import ch.rmy.iconfetcher.models.IconEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@HiltViewModel
class IconPickerViewModel
@Inject
constructor(
    application: Application,
    private val getIconListItems: GetIconListItemsUseCase,
    private val httpClientFactory: HttpClientFactory,
    private val deviceLocalPreferences: DeviceLocalPreferences,
    private val fetchAndStoreMaterialIcon: FetchAndStoreMaterialIconUseCase,
) : BaseViewModel<IconPickerViewModel.InitData, IconPickerViewState>(application) {

    private var cachedIcons: List<MaterialIcon>? = null
    private val iconsMutex = Mutex()

    private var selectedShape = IconShape.SQUARE

    override suspend fun initialize(data: InitData): IconPickerViewState {
        val icons = withContext(Dispatchers.IO) {
            getIconListItems()
        }
        viewModelScope.launch {
            if (data.isMaterialDesignIconPicker) {
                if (deviceLocalPreferences.isAwareOfMaterialIconsInfo) {
                    showMaterialIconSelectionDialog()
                } else {
                    showMaterialIconsInfoDialog()
                }
            } else if (icons.isEmpty()) {
                showCircleSelectionDialog()
            }
        }
        return IconPickerViewState(
            icons = icons,
        )
    }

    private suspend fun showMaterialIconsInfoDialog() {
        updateViewState {
            copy(dialogState = IconPickerDialogState.MaterialIconsInfo)
        }
    }

    private suspend fun showMaterialIconSelectionDialog() {
        updateViewState {
            copy(dialogState = IconPickerDialogState.SelectMaterialIcon)
        }
    }

    fun onIconClicked(icon: ShortcutIcon.CustomIcon) = runAction {
        selectIcon(icon)
    }

    private suspend fun selectIcon(icon: ShortcutIcon.CustomIcon) {
        closeScreen(result = NavigationDestination.IconPicker.Result(icon))
    }

    fun onAddIconButtonClicked() = runAction {
        showCircleSelectionDialog()
    }

    private suspend fun showCircleSelectionDialog() {
        updateViewState {
            copy(dialogState = IconPickerDialogState.SelectShape)
        }
    }

    private suspend fun showImagePicker() {
        emitEvent(IconPickerEvent.ShowImagePicker)
    }

    fun onShapeSelected(iconShape: IconShape) = runAction {
        selectedShape = iconShape
        updateViewState {
            copy(dialogState = null)
        }
        showImagePicker()
    }

    fun onMaterialIconsInfoConfirmed() = runAction {
        deviceLocalPreferences.isAwareOfMaterialIconsInfo = true
        showMaterialIconSelectionDialog()
    }

    fun onMaterialIconSelected(icon: MaterialIcon, color: Int) = runAction {
        updateViewState {
            copy(dialogState = IconPickerDialogState.Processing)
        }
        try {
            val icon = fetchAndStoreMaterialIcon(icon, color)
            updateViewState {
                copy(
                    icons = icons.plus(IconPickerListItem(icon, isUnused = true)),
                )
            }
            selectIcon(icon)
        } catch (_: IOException) {
            updateViewState {
                copy(dialogState = null)
            }
            showSnackbar(R.string.error_set_image, long = true)
            return@runAction
        }
    }

    fun onIconCreationFailed() = runAction {
        showSnackbar(R.string.error_set_image, long = true)
    }

    fun onImageSelected(image: Uri) = runAction {
        emitEvent(IconPickerEvent.ShowImageCropper(image, selectedShape))
    }

    fun onIconCreated(iconUri: Uri) = runAction {
        val colorAnalysis = withContext(Dispatchers.IO) {
            val bitmap = context.contentResolver.openInputStream(iconUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            try {
                bitmap?.analyzeColors()
            } finally {
                bitmap?.recycle()
            }
        }
        val iconName = CustomIconName.generate(
            isCircular = selectedShape == IconShape.CIRCLE,
            hasTransparency = colorAnalysis?.hasSignificantTransparency == true,
            singleColor = colorAnalysis?.singleColor,
        )
        val targetFile = File(context.filesDir, iconName.toString())
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(iconUri)!!.use { inputStream ->
                targetFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        val icon = ShortcutIcon.CustomIcon(iconName)

        updateViewState {
            copy(
                icons = icons.plus(IconPickerListItem(icon, isUnused = true)),
            )
        }
        selectIcon(icon)
    }

    fun onImagePickerFailed() = runAction {
        showSnackbar(R.string.error_not_supported)
    }

    fun onIconLongClicked(icon: ShortcutIcon.CustomIcon) = runAction {
        val isUnused = viewState.icons.find { it.icon == icon }?.isUnused ?: skipAction()
        updateViewState {
            copy(dialogState = IconPickerDialogState.DeleteIcon(icon, !isUnused))
        }
    }

    fun onDeleteButtonClicked() = runAction {
        updateViewState {
            copy(dialogState = IconPickerDialogState.BulkDelete)
        }
    }

    fun onDeletionConfirmed() = runAction {
        when (val dialogState = viewState.dialogState) {
            is IconPickerDialogState.BulkDelete -> onBulkDeletionConfirmed()
            is IconPickerDialogState.DeleteIcon -> onDeletionConfirmed(dialogState.icon)
            else -> Unit
        }
    }

    private suspend fun onDeletionConfirmed(icon: ShortcutIcon.CustomIcon) {
        viewModelScope.launch(Dispatchers.IO) {
            icon.getFile(context)?.delete()
        }
        updateViewState {
            copy(
                icons = icons.filter { it.icon != icon },
                dialogState = null,
            )
        }
    }

    private suspend fun ViewModelScope<IconPickerViewState>.onBulkDeletionConfirmed() {
        val icons = viewState.icons.filter { it.isUnused }
        viewModelScope.launch(Dispatchers.IO) {
            icons.forEach {
                it.icon.getFile(context)?.delete()
            }
        }
        updateViewState {
            copy(
                icons = this.icons.filterNot { it.isUnused },
                dialogState = null,
            )
        }
    }

    fun onDialogDismissalRequested() = runAction {
        val dialogState = viewState.dialogState
        updateViewState {
            copy(
                dialogState = null,
            )
        }
        if (dialogState == IconPickerDialogState.SelectMaterialIcon || dialogState == IconPickerDialogState.MaterialIconsInfo) {
            closeScreen()
        }
    }

    suspend fun getIcons(query: String): List<MaterialIcon> = withContext(Dispatchers.Default) {
        val queryTerms = query.trim()
            .takeUnlessEmpty()
            ?.let {
                SearchUtil.normalizeToKeywords(it, minLength = 1)
            }
        getIcons()
            .runIfNotNull(queryTerms) { queryTerms ->
                filter { icon ->
                    icon.matches(queryTerms)
                }
            }
    }

    private suspend fun getIcons(): List<MaterialIcon> {
        cachedIcons?.let {
            return it
        }
        iconsMutex.withLock {
            cachedIcons?.let {
                return it
            }
            cachedIcons = computeIconIndex()
            return cachedIcons!!
        }
    }

    private suspend fun computeIconIndex(): List<MaterialIcon> = coroutineScope {
        val iconFetcher = IconFetcher(
            client = httpClientFactory.getClient(context, userAgent = UserAgentProvider.getUserAgent(context)),
            baseUrl = ICONS_BASE_URL,
            cacheFile = File(context.cacheDir, MATERIAL_ICONS_INDEX_FILE),
        )
        iconFetcher.getIcons()
            .chunked(1000)
            .map { iconEntries: List<IconEntry> ->
                async {
                    iconEntries.map { iconEntry ->
                        MaterialIcon(
                            name = iconEntry.name,
                            url = ICONS_BASE_URL + iconEntry.url,
                            keywords = buildSet {
                                addAll(SearchUtil.normalizeToKeywords(iconEntry.name, minLength = 2))
                                iconEntry.aliases?.forEach { alias -> addAll(SearchUtil.normalizeToKeywords(alias, minLength = 3)) }
                                iconEntry.tags?.forEach { alias -> addAll(SearchUtil.normalizeToKeywords(alias, minLength = 3)) }
                            },
                        )
                    }
                }
            }
            .awaitAll()
            .flatten()
    }

    private fun MaterialIcon.matches(queryTerms: Set<String>): Boolean =
        queryTerms.any { queryTerm ->
            keywords.any { keyword -> queryTerm in keyword }
        }

    data class InitData(
        val isMaterialDesignIconPicker: Boolean,
    )

    companion object {
        const val ICONS_BASE_URL = "https://http-shortcuts.rmy.ch/material-icons/"
        private const val MATERIAL_ICONS_INDEX_FILE = "material-icons-index.json"
    }
}
