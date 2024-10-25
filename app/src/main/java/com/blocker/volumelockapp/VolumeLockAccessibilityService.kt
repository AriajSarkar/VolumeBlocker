package com.blocker.volumelockapp

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.media.AudioManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class VolumeLockAccessibilityService : AccessibilityService() {
    private lateinit var audioManager: AudioManager
    private var currentVolume = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No implementation needed for this example
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.action == KeyEvent.ACTION_DOWN) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onInterrupt() {
        // No implementation needed for this example
    }
}