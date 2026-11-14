package com.example.carmirror

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.view.Surface

/**
 * Telefon ekranı (MediaProjection) ile araç ekranı yüzeyini (Surface) birbirine bağlar.
 * Android 14+ aynı projection ile tek VirtualDisplay'e izin verdiği için,
 * yüzey değişince yeni display açmak yerine mevcut display'in yüzeyi değiştirilir.
 */
object MirrorController {
    private val main = Handler(Looper.getMainLooper())

    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var surface: Surface? = null
    private var width = 0
    private var height = 0
    private var dpi = 0

    @Volatile
    var onChange: (() -> Unit)? = null

    val isProjecting: Boolean
        @Synchronized get() = projection != null

    @Synchronized
    fun isCurrent(p: MediaProjection): Boolean = projection === p

    @Synchronized
    fun setProjection(p: MediaProjection?) {
        val old = projection
        display?.release()
        display = null
        projection = p
        if (old != null && old !== p) old.stop()
        createDisplayIfReady()
        notifyChange()
    }

    @Synchronized
    fun attach(s: Surface, w: Int, h: Int, d: Int) {
        surface = s
        width = w
        height = h
        dpi = d
        val vd = display
        if (vd != null) {
            vd.surface = s
            vd.resize(w, h, d)
        } else {
            createDisplayIfReady()
        }
    }

    @Synchronized
    fun detach() {
        surface = null
        display?.surface = null
    }

    private fun createDisplayIfReady() {
        val p = projection ?: return
        val s = surface ?: return
        if (display != null || width <= 0 || height <= 0) return
        display = p.createVirtualDisplay(
            "CarMirror",
            width,
            height,
            dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            s,
            null,
            null
        )
    }

    private fun notifyChange() {
        main.post { onChange?.invoke() }
    }
}
