package com.blocker.volumelockapp

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var audioManager: AudioManager
    private lateinit var volumeTextView: TextView
    private var isVolumeLocked = false
    private var currentVolume = 0

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (!Settings.canDrawOverlays(this)) {
            Log.e("MainActivity", "Overlay permission not granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }

        if (!isAccessibilityServiceEnabled(this, VolumeLockAccessibilityService::class.java)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        try {
            audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            volumeTextView = findViewById(R.id.volumeTextView)
            val lockButton: Button = findViewById(R.id.lockButton)

            lockButton.setOnClickListener {
                isVolumeLocked = !isVolumeLocked
                volumeTextView.text = if (isVolumeLocked) {
                    startVolumeLockService()
                    "Volume Lock: ON"
                } else {
                    stopVolumeLockService()
                    "Volume Lock: OFF"
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing views or AudioManager", e)
        }
    }

    private fun startVolumeLockService() {
        val intent = Intent(this, VolumeLockService::class.java)
        startService(intent)
    }

    private fun stopVolumeLockService() {
        val intent = Intent(this, VolumeLockService::class.java)
        stopService(intent)
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(ComponentName(context, service).flattenToString(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Do nothing and consume the event
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Do nothing and consume the event
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}