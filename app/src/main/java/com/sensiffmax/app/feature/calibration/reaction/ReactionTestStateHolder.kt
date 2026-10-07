package com.sensiffmax.app.feature.calibration.reaction

import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ReactionTestStateHolder — In-memory state holder for the latest reaction calibration result.
 */
object ReactionTestStateHolder {
    private val _latestResult = MutableStateFlow<ReactionTestResult?>(null)
    val latestResult: StateFlow<ReactionTestResult?> = _latestResult.asStateFlow()

    fun updateResult(result: ReactionTestResult) {
        _latestResult.value = result
    }

    fun clear() {
        _latestResult.value = null
    }
}
