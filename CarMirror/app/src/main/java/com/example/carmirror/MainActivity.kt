package com.example.carmirror

import android.Manifest
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var status: TextView

    private val projectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val intent = Intent(this, MirrorForegroundService::class.java)
                    .putExtra(MirrorForegroundService.EXTRA_CODE, result.resultCode)
                    .putExtra(MirrorForegroundService.EXTRA_DATA, data)
                ContextCompat.startForegroundService(this, intent)
                status.text = "Yansıtma hazır. Araç ekranında uygulamayı aç."
            } else {
                status.text = "Ekran yakalama izni verilmedi."
            }
        }

    private val notificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= 33) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(pad, pad, pad, pad)
        }

        status = TextView(this).apply {
            text = "1) Önce bu düğmeye bas\n2) Sonra araç ekranından \"Ekran Yansıt\" uygulamasını aç\n\nVideoyu yatay (landscape) oynat."
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val start = Button(this).apply {
            text = "Yansıtmayı Başlat"
            setOnClickListener {
                val mpm = getSystemService(MediaProjectionManager::class.java)
                projectionLauncher.launch(mpm.createScreenCaptureIntent())
            }
        }

        val stop = Button(this).apply {
            text = "Durdur"
            setOnClickListener {
                startService(
                    Intent(this@MainActivity, MirrorForegroundService::class.java)
                        .setAction(MirrorForegroundService.ACTION_STOP)
                )
                status.text = "Durduruldu."
            }
        }

        root.addView(status)
        root.addView(start)
        root.addView(stop)
        setContentView(root)
    }
}
