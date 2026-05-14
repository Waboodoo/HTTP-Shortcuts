package ch.rmy.android.http_shortcuts.http

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class ProgressTracker {
    private val progressFlow = MutableStateFlow<UploadProgress?>(null)

    fun observeProgress(): Flow<UploadProgress> =
        progressFlow.filterNotNull()

    fun onProgress(progress: UploadProgress) {
        progressFlow.value = progress
    }
}
