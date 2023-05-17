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
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CreateQuickSettingsTileUseCase
@Inject
constructor(
    private val context: Context,
) {
    suspend operator fun invoke(): Boolean =
        suspendCoroutine { continuation ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService<StatusBarManager>()!!.requestAddTileService(
                    ComponentName.createRelative(context, "ch.rmy.android.http_shortcuts.tiles.QuickTileService"),
                    context.getString(R.string.action_quick_settings_tile_trigger),
                    Icon.createWithResource(context, R.drawable.ic_quick_settings_tile),
                    context.mainExecutor,
                ) { result ->
                    continuation.resume(
                        result == TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED || result == TILE_ADD_REQUEST_RESULT_TILE_ADDED,
                    )
                }
            }
        }
}
