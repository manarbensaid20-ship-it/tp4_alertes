
package tn.isetbizerte.tp4_alertes

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class VerificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val seuil = inputData.getInt("seuil", 20)

            val batteryIntent = applicationContext.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            val niveau = batteryIntent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

                if (level >= 0 && scale > 0) {
                    (level * 100) / scale
                } else {
                    -1
                }
            } ?: -1

            if (niveau >= 0 && niveau < seuil) {
                Alertes.creerCanal(applicationContext)
                Alertes.notifier(
                    applicationContext,
                    "Batterie faible",
                    "Niveau de batterie : $niveau%"
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
