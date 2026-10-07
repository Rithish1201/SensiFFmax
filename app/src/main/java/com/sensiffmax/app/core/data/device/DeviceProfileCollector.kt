package com.sensiffmax.app.core.data.device

import android.content.Context
import android.os.Build
import android.view.WindowManager
import com.sensiffmax.app.core.data.model.DeviceProfile

/**
 * DeviceProfileCollector — Gathers device hardware info without permissions.
 *
 * Uses standard Display and WindowMetrics APIs available without any runtime permissions.
 * Refresh rate is only available on API 23+ (our minSdk is 26, so always available).
 */
object DeviceProfileCollector {

    fun collect(context: Context): DeviceProfile {
        var widthPx = 0
        var heightPx = 0
        var refreshRate = 60f
        val densityDpi = context.resources.displayMetrics.densityDpi

        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (windowManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val bounds = windowManager.currentWindowMetrics.bounds
                    widthPx = bounds.width()
                    heightPx = bounds.height()
                } else {
                    @Suppress("DEPRECATION")
                    val display = windowManager.defaultDisplay
                    val size = android.graphics.Point()
                    @Suppress("DEPRECATION")
                    display.getRealSize(size)
                    widthPx = size.x
                    heightPx = size.y
                }
            }
        } catch (_: Exception) {
            val dm = context.resources.displayMetrics
            widthPx = dm.widthPixels
            heightPx = dm.heightPixels
        }

        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager
            val defaultDisplay = displayManager?.getDisplay(android.view.Display.DEFAULT_DISPLAY)
            if (defaultDisplay != null) {
                refreshRate = defaultDisplay.refreshRate
            }
        } catch (_: Exception) {
            refreshRate = 60f
        }

        return DeviceProfile(
            screenWidthPx = widthPx,
            screenHeightPx = heightPx,
            screenDensityDpi = densityDpi,
            refreshRate = if (refreshRate > 0) refreshRate else 60f
        )
    }
}
