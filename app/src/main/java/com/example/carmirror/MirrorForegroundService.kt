package com.example.carmirror

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class MirrorForegroundService : Service() {

    companion object {
        const val ACTION_STOP = "com.example.carmirror.STOP"
        const val EXTRA_CODE = "code"
        const val EXTRA_DATA = "data"
        private const val CHANNEL_ID = "mirror"
        private const val NOTIF_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            MirrorController.setProjection(null)
            stopSelf()
            return START_NOT_STICKY
        }

        createChannel()
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MirrorForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Ekran yansıtılıyor")
            .setContentText("Araç ekranına yansıtma açık")
            .setOngoing(true)
            .addAction(0, "Durdur", stopIntent)
            .build()

        // Android 14+: projection alınmadan ÖNCE foreground servis (mediaProjection tipi) başlamalı.
        val type = if (Build.VERSION.SDK_INT >= 29) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        } else 0
        ServiceCompat.startForeground(this, NOTIF_ID, notification, type)

        val code = intent?.getIntExtra(EXTRA_CODE, 0) ?: 0
        val data: Intent? = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableExtra(EXTRA_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_DATA)
        }
        if (data == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val mpm = getSystemService(MediaProjectionManager::class.java)
        val projection = mpm.getMediaProjection(code, data)
        projection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                if (MirrorController.isCurrent(projection)) {
                    MirrorController.setProjection(null)
                    stopSelf()
                }
            }
        }, Handler(Looper.getMainLooper()))
        MirrorController.setProjection(projection)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        MirrorController.setProjection(null)
        super.onDestroy()
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Ekran yansıtma", NotificationManager.IMPORTANCE_LOW)
        )
    }
}
