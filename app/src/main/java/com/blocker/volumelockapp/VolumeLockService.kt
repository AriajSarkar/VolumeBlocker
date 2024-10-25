package com.blocker.volumelockapp

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager

class VolumeLockService : Service() {
    private lateinit var audioManager: AudioManager
    private var currentVolume = 0
    private lateinit var overlayView: View

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        setupOverlay()
    }

    private fun setupOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Log.e("VolumeLockService", "Overlay permission not granted")
            stopSelf()
            return
        }

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = View(this).apply {
            setBackgroundColor(0) // Make the view transparent
        }
        val params = WindowManager.LayoutParams(
            1, // Width of 1 pixel
            1, // Height of 1 pixel
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayView, params)
        overlayView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.action == KeyEvent.ACTION_DOWN) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                return@setOnKeyListener true
            }
            false
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.removeViewImmediate(overlayView)
    }
}