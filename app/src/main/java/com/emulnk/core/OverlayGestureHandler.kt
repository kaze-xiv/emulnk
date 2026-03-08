package com.emulnk.core

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.emulnk.model.ScreenTarget
import com.emulnk.model.WidgetConfig
import kotlin.math.abs

/**
 * Unified gesture handler for widget drag, pinch-resize, tap-select, and double-tap-toggle.
 * Works for both primary (WindowManager) and secondary (FrameLayout) widgets via lambdas.
 */
class OverlayGestureHandler(
    context: Context,
    private val session: BuilderSession,
    private val widgetId: String,
    private val screen: ScreenTarget,
    private val config: WidgetConfig,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val density: Float,
    private val getPosition: () -> Pair<Int, Int>,
    private val getSize: () -> Pair<Int, Int>,
    private val onPositionChanged: (x: Int, y: Int) -> Unit,
    private val onSizeChanged: (w: Int, h: Int) -> Unit,
    private val onSelected: () -> Unit,
    private val onToggled: () -> Unit
) {
    private val minWidthPx = (config.minWidth * density).toInt()
    private val minHeightPx = (config.minHeight * density).toInt()
    private val snapPx = (OverlayConstants.SNAP_THRESHOLD_DP * density).toInt()

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    // For resize undo tracking
    private var resizeStartW = 0
    private var resizeStartH = 0
    private var isResizing = false

    // Double-tap detection: two taps within DOUBLE_TAP_TIMEOUT_MS toggles enable/disable.
    // Reset on drag start to prevent tap→drag→release→tap from triggering a false double-tap.
    private var lastTapTime = 0L

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (!config.resizable) return false
            val (w, h) = getSize()
            resizeStartW = w
            resizeStartH = h
            isResizing = true
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (!config.resizable) return false
            val (w, h) = getSize()
            val newW = (w * detector.scaleFactor).toInt().coerceIn(minWidthPx, maxOf(minWidthPx, screenWidth))
            val newH = (h * detector.scaleFactor).toInt().coerceIn(minHeightPx, maxOf(minHeightPx, screenHeight))
            onSizeChanged(newW, newH)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (isResizing) {
                isResizing = false
                val (newW, newH) = getSize()
                session.commitResize(
                    widgetId, screen,
                    (resizeStartW / density).toInt(), (resizeStartH / density).toInt(),
                    (newW / density).toInt(), (newH / density).toInt()
                )
            }
        }
    })

    @SuppressLint("ClickableViewAccessibility")
    fun attachTo(view: View) {
        view.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            if (scaleDetector.isInProgress) return@setOnTouchListener true

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val (x, y) = getPosition()
                    initialX = x
                    initialY = y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount > 1) return@setOnTouchListener true
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (abs(dx) > OverlayConstants.DRAG_THRESHOLD_PX || abs(dy) > OverlayConstants.DRAG_THRESHOLD_PX)) {
                        isDragging = true
                        // Reset so a tap after drag-release can't register as double-tap
                        lastTapTime = 0L
                    }
                    if (isDragging) {
                        val (w, h) = getSize()
                        val rightEdge = maxOf(0, screenWidth - w)
                        val bottomEdge = maxOf(0, screenHeight - h)
                        var newX = (initialX + dx.toInt()).coerceIn(0, rightEdge)
                        var newY = (initialY + dy.toInt()).coerceIn(0, bottomEdge)
                        // Snap to edges
                        if (newX in 0..snapPx) newX = 0
                        if (newY in 0..snapPx) newY = 0
                        if (rightEdge > 0 && newX in (rightEdge - snapPx) until rightEdge) newX = rightEdge
                        if (bottomEdge > 0 && newY in (bottomEdge - snapPx) until bottomEdge) newY = bottomEdge
                        onPositionChanged(newX, newY)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val wasSelected = session.state.value.selectedWidgetId == widgetId &&
                            session.state.value.selectedScreen == screen

                    onSelected()

                    if (!isDragging && wasSelected) {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < OverlayConstants.DOUBLE_TAP_TIMEOUT_MS) {
                            onToggled()
                            lastTapTime = 0L
                        } else {
                            lastTapTime = now
                        }
                    } else if (isDragging) {
                        val (newX, newY) = getPosition()
                        session.commitMove(
                            widgetId, screen,
                            (initialX / density).toInt(), (initialY / density).toInt(),
                            (newX / density).toInt(), (newY / density).toInt()
                        )
                    }
                    true
                }
                else -> false
            }
        }
    }
}
