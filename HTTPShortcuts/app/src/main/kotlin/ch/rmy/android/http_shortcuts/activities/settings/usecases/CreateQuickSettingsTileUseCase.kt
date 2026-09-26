package ch.rmy.android.http_shortcuts.activities.settings.usecases

import android.app.StatusBarManager
import android.app.StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED
import android.app.StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.getSystemService
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.tiles.QuickTileService
import ch.rmy.android.http_shortcuts.tiles.QuickTileUpdater
import ch.rmy.android.http_shortcuts.utils.IconUtil
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class CreateQuickSettingsTileUseCase
@Inject
constructor(
    private val context: Context,
    private val shortcutRepository: ShortcutRepository,
    private val quickTileUpdater: QuickTileUpdater,
) {
    suspend operator fun invoke(): Boolean {
        val shortcut = shortcutRepository.getQuickSettingsShortcuts()
            .singleOrNull()
        val label = shortcut?.name
            ?: context.getString(R.string.action_quick_settings_tile_trigger)
        val icon = shortcut?.icon
            ?.takeIf { it.isUsableAsSilhouette }
            ?.let {
                withContext(Dispatchers.IO) {
                    IconUtil.getIcon(context, it, adaptive = false)
                }
            }
            ?: Icon.createWithResource(context, R.drawable.ic_quick_settings_tile)
        val result = suspendCancellableCoroutine { continuation ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService<StatusBarManager>()!!.requestAddTileService(
                    ComponentName(context, QuickTileService::class.java),
                    label,
                    icon,
                    context.mainExecutor,
                ) { result ->
                    continuation.resume(result)
                }
            }
        }
        return when (result) {
            TILE_ADD_REQUEST_RESULT_TILE_ADDED -> {
                true
            }
            TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> {
                quickTileUpdater.update()
                true
            }
            else -> false
        }
    }
}
