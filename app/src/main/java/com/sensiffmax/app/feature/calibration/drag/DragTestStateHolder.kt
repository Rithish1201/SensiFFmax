package com.sensiffmax.app.feature.calibration.drag

import com.sensiffmax.app.feature.calibration.drag.model.DragTestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * DragTestStateHolder — In-memory state holder for the latest drag tracking calibration result.
 *
 * Maintains parity with ReactionTestStateHolder and PrecisionTestStateHolder for pipeline integration.
 */
object DragTestStateHolder {
    private val _latestResult = MutableStateFlow<DragTestResult?>(null)
    val latestResult: StateFlow<DragTestResult?> = _latestResult.asStateFlow()

    fun updateResult(result: DragTestResult) {
        _latestResult.value = result
    }

    fun clear() {
        _latestResult.value = null
    }
}
