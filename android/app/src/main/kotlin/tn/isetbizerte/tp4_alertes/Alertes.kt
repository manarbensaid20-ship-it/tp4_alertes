
package tn.isetbizerte.tp4_alertes

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object Alertes {

    const val CANAL_ID = "alertes_critiques"
    const val NOTIFICATION_ID = 1001

    fun creerCanal(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val canal = NotificationChannel(
                CANAL_ID,
                "Alertes critiques",
                NotificationManager.IMPORTANCE_HIGH
            )

            canal.description = "Canal des alertes importantes"
            canal.enableVibration(true)

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(canal)
        }
    }

    fun notifier(
        context: Context,
        titre: String,
        message: String
    ): Boolean {

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (!manager.areNotificationsEnabled()) {
            return false
        }

        // Action Voir
        val intentVoir = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = "VOIR_ALERTE"
        }

        val pendingVoir = PendingIntent.getActivity(
            context,
            1,
            intentVoir,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Action Ignorer
        val intentIgnorer = Intent(
            context,
            AlerteReceiver::class.java
        ).apply {
            action = "IGNORER_ALERTE"
        }

        val pendingIgnorer = PendingIntent.getBroadcast(
            context,
            2,
            intentIgnorer,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(
            context,
            CANAL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titre)
            .setContentText(message)
            .setStyle(
                Notification.BigTextStyle().bigText(message)
            )
            .setContentIntent(pendingVoir)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Voir",
                pendingVoir
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Ignorer",
                pendingIgnorer
            )
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_ALARM)
            .build()

        manager.notify(NOTIFICATION_ID, notification)

        return true
    }
}
