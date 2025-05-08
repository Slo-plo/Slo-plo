package com.example.slo_plo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class LocationTrackingService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("LocationService", "서비스 시작")
        startForegroundServiceNotification()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "location_tracking_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "위치 추적",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("플로깅 중...")
            .setContentText("플로깅을 진행하고 있습니다.")
            .setSmallIcon(R.drawable.ic_noti_sloplo)
            .build()

        Log.d("LocationService", "알림 생성 및 Foreground 시작")

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }
}
