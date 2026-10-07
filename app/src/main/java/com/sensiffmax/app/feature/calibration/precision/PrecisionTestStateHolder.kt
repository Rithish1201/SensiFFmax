package com.sensiffmax.app.feature.calibration.precision

import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PrecisionTestStateHolder — In-memory state holder for the latest precision calibration result.
 *
 * Follows the same pattern as ReactionTestStateHolder for cross-screen data sharing.
 */
object PrecisionTestStateHolder {
    private val _latestResult = MutableStateFlow<PrecisionTestResult?>(null)
    val latestResult: StateFlow<PrecisionTestResult?> = _latestResult.asStateFlow()

    fun updateResult(result: PrecisionTestResult) {
        _latestResult.value = result
    }

    fun clear() {
        _latestResult.value = null
    }
}
