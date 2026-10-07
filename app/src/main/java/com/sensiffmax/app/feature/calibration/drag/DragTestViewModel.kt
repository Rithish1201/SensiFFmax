package com.sensiffmax.app.feature.calibration.drag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sensiffmax.app.feature.calibration.drag.engine.DragScoringEngine
import com.sensiffmax.app.feature.calibration.drag.model.DragSample
import com.sensiffmax.app.feature.calibration.drag.model.DragTestResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * DragTestViewModel — Drives real-time tracking, touch sampling, and results computation.
 */
class DragTestViewModel : ViewModel() {

    enum class TestPhase {
        READY,
        TRACKING,
        FINISHED
    }

    data class UiState(
        val phase: TestPhase = TestPhase.READY,
        val targetX: Float = 0f,
        val targetY: Float = 0f,
        val fingerX: Float? = null,
        val fingerY: Float? = null,
        val currentErrorPx: Float? = null,
        val isOnTarget: Boolean = false,
        val isTouching: Boolean = false,
        val elapsedMs: Long = 0L,
        val remainingSeconds: Float = 20.0f,
        val progress: Float = 0f,
        val liveAccuracy: Float = 0f,
        val boundsWidth: Float = 1080f,
        val boundsHeight: Float = 1600f,
        val targetRadiusPx: Float = DragScoringEngine.DEFAULT_RADIUS_PX,
        val result: DragTestResult? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null
    private val recordedSamples = mutableListOf<DragSample>()

    // Current pointer position updated from UI gestures
    @Volatile
    private var currentFingerX: Float? = null
    @Volatile
    private var currentFingerY: Float? = null
    @Volatile
    private var isFingerDown: Boolean = false

    fun setBounds(width: Float, height: Float, radiusPx: Float = DragScoringEngine.DEFAULT_RADIUS_PX) {
        if (width <= 0f || height <= 0f) return
        _uiState.update { current ->
            val initialPos = DragScoringEngine.calculateTrajectoryPoint(0f, width, height)
            current.copy(
                boundsWidth = width,
                boundsHeight = height,
                targetRadiusPx = radiusPx,
                targetX = initialPos.first,
                targetY = initialPos.second
            )
        }
    }

    fun startTest() {
        if (_uiState.value.phase == TestPhase.TRACKING) return

        trackingJob?.cancel()
        recordedSamples.clear()
        currentFingerX = null
        currentFingerY = null
        isFingerDown = false

        _uiState.update { current ->
            val initialPos = DragScoringEngine.calculateTrajectoryPoint(0f, current.boundsWidth, current.boundsHeight)
            current.copy(
                phase = TestPhase.TRACKING,
                targetX = initialPos.first,
                targetY = initialPos.second,
                fingerX = null,
                fingerY = null,
                currentErrorPx = null,
                isOnTarget = false,
                isTouching = false,
                elapsedMs = 0L,
                remainingSeconds = (DragScoringEngine.DEFAULT_DURATION_MS / 1000f),
                progress = 0f,
                liveAccuracy = 0f,
                result = null
            )
        }

        trackingJob = viewModelScope.launch {
            val totalDuration = DragScoringEngine.DEFAULT_DURATION_MS
            val interval = DragScoringEngine.SAMPLE_INTERVAL_MS
            var elapsed = 0L

            while (isActive && elapsed < totalDuration) {
                delay(interval)
                elapsed += interval

                val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                val targetPos = DragScoringEngine.calculateTrajectoryPoint(
                    progress,
                    _uiState.value.boundsWidth,
                    _uiState.value.boundsHeight
                )

                val fX = currentFingerX
                val fY = currentFingerY
                val touching = isFingerDown && fX != null && fY != null

                val errorPx: Float?
                val onTarget: Boolean

                if (touching && fX != null && fY != null) {
                    val dist = DragScoringEngine.calculateDistance(fX, fY, targetPos.first, targetPos.second)
                    errorPx = dist
                    onTarget = dist <= _uiState.value.targetRadiusPx
                } else {
                    errorPx = null
                    onTarget = false
                }

                val sample = DragSample(
                    timestampMs = elapsed,
                    targetX = targetPos.first,
                    targetY = targetPos.second,
                    fingerX = if (touching) fX else null,
                    fingerY = if (touching) fY else null,
                    errorPx = errorPx,
                    isOnTarget = onTarget,
                    isTouching = touching
                )
                recordedSamples.add(sample)

                // Calculate live accuracy among recorded touch samples
                val touchedSamples = recordedSamples.filter { it.isTouching }
                val liveAcc = if (touchedSamples.isNotEmpty()) {
                    (touchedSamples.count { it.isOnTarget }.toFloat() / touchedSamples.size.toFloat()) * 100f
                } else 0f

                val remainingSec = ((totalDuration - elapsed).coerceAtLeast(0L) / 1000f)

                _uiState.update { current ->
                    current.copy(
                        targetX = targetPos.first,
                        targetY = targetPos.second,
                        fingerX = if (touching) fX else null,
                        fingerY = if (touching) fY else null,
                        currentErrorPx = errorPx,
                        isOnTarget = onTarget,
                        isTouching = touching,
                        elapsedMs = elapsed,
                        remainingSeconds = remainingSec,
                        progress = progress,
                        liveAccuracy = liveAcc
                    )
                }
            }

            // Test finished — compute official score
            completeTest()
        }
    }

    fun onTouchDown(x: Float, y: Float) {
        currentFingerX = x
        currentFingerY = y
        isFingerDown = true
    }

    fun onTouchMove(x: Float, y: Float) {
        currentFingerX = x
        currentFingerY = y
        isFingerDown = true
    }

    fun onTouchUp() {
        currentFingerX = null
        currentFingerY = null
        isFingerDown = false
    }

    fun abortAndRestart() {
        trackingJob?.cancel()
        recordedSamples.clear()
        currentFingerX = null
        currentFingerY = null
        isFingerDown = false

        _uiState.update { current ->
            val initialPos = DragScoringEngine.calculateTrajectoryPoint(0f, current.boundsWidth, current.boundsHeight)
            current.copy(
                phase = TestPhase.READY,
                targetX = initialPos.first,
                targetY = initialPos.second,
                fingerX = null,
                fingerY = null,
                currentErrorPx = null,
                isOnTarget = false,
                isTouching = false,
                elapsedMs = 0L,
                remainingSeconds = (DragScoringEngine.DEFAULT_DURATION_MS / 1000f),
                progress = 0f,
                liveAccuracy = 0f,
                result = null
            )
        }
    }

    private fun completeTest() {
        val result = DragScoringEngine.calculateResult(
            samples = recordedSamples.toList(),
            totalDurationMs = DragScoringEngine.DEFAULT_DURATION_MS,
            targetRadiusPx = _uiState.value.targetRadiusPx
        )
        DragTestStateHolder.updateResult(result)
        com.sensiffmax.app.feature.calibration.CalibrationSessionStateHolder.updateDrag(result)

        _uiState.update { current ->
            current.copy(
                phase = TestPhase.FINISHED,
                result = result
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
    }
}
