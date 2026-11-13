
package tn.isetbizerte.tp4_alertes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager

class AlerteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "IGNORER_ALERTE") {

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.cancel(Alertes.NOTIFICATION_ID)
        }
    }
}
