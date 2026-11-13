
package tn.isetbizerte.tp4_alertes

import android.app.Service
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import android.os.Build

class ServiceSurveillance : Service() {

    companion object {
        const val CHANNEL_ID = "surveillance_continue"
        const val NOTIFICATION_ID = 3001
    }

    override fun onCreate() {
        super.onCreate()
        creerCanal()
    }

    private fun creerCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Surveillance continue",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Surveillance active")
            .setContentText("Le service fonctionne en arrière-plan")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
