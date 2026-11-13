
package tn.isetbizerte.tp4_alertes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : FlutterActivity() {

    private val CHANNEL = "alerte/notifier"
    private val WORK_CHANNEL = "sync/planifier"
    private val SERVICE_CHANNEL = "service/surveillance"

    private var permissionResult: MethodChannel.Result? = null
    private var actionEnAttente: String? = null
    private var flutterChannel: MethodChannel? = null

    override fun configureFlutterEngine(
        flutterEngine: FlutterEngine
    ) {
        super.configureFlutterEngine(flutterEngine)

        Alertes.creerCanal(this)

        // CHANNEL 1 : Notifications
        flutterChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        )

        flutterChannel?.setMethodCallHandler { call, result ->
            when (call.method) {

                "demanderPermission" -> {
                    demanderPermission(result)
                }

                "notifier" -> {
                    val titre =
                        call.argument<String>("titre") ?: "Alerte"

                    val message =
                        call.argument<String>("message") ?: "Alerte importante"

                    val success = Alertes.notifier(
                        this, titre, message
                    )

                    result.success(success)
                }

                "actionInitiale" -> {
                    result.success(actionEnAttente)
                    actionEnAttente = null
                }

                else -> result.notImplemented()
            }
        }

        // CHANNEL 2 : WorkManager
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            WORK_CHANNEL
        ).setMethodCallHandler { call, result ->

            val seuil = call.argument<Int>("seuil") ?: 20

            when (call.method) {

                "planifier" -> {
                    val data = workDataOf("seuil" to seuil)

                    val request =
                        PeriodicWorkRequestBuilder<VerificationWorker>(
                            15, TimeUnit.MINUTES
                        )
                            .setInputData(data)
                            .build()

                    WorkManager.getInstance(applicationContext)
                        .enqueueUniquePeriodicWork(
                            "verification_batterie",
                            ExistingPeriodicWorkPolicy.UPDATE,
                            request
                        )

                    result.success(true)
                }

                "maintenant" -> {
                    val data = workDataOf("seuil" to seuil)

                    val request =
                        OneTimeWorkRequestBuilder<VerificationWorker>()
                            .setInputData(data)
                            .build()

                    WorkManager.getInstance(applicationContext)
                        .enqueue(request)

                    result.success(true)
                }

                "annuler" -> {
                    WorkManager.getInstance(applicationContext)
                        .cancelUniqueWork("verification_batterie")

                    result.success(true)
                }

                else -> result.notImplemented()
            }
        }

        // CHANNEL 3 : Foreground Service
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            SERVICE_CHANNEL
        ).setMethodCallHandler { call, result ->

            when (call.method) {

                "demarrerService" -> {
                    try {
                        val serviceIntent = Intent(
                            this,
                            ServiceSurveillance::class.java
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }

                        result.success(true)

                    } catch (e: Exception) {
                        result.error(
                            "SERVICE_ERROR",
                            e.message,
                            null
                        )
                    }
                }

                "arreterService" -> {
                    val serviceIntent = Intent(
                        this,
                        ServiceSurveillance::class.java
                    )

                    result.success(stopService(serviceIntent))
                }

                else -> result.notImplemented()
            }
        }

        traiterIntent(intent)
    }

    private fun traiterIntent(intent: Intent?) {
        if (intent?.action == "VOIR_ALERTE") {
            actionEnAttente = "VOIR"
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.action == "VOIR_ALERTE") {
            flutterChannel?.invokeMethod("surAction", "VOIR")
        } else {
            traiterIntent(intent)
        }
    }

    private fun demanderPermission(result: MethodChannel.Result) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            result.success(true)
            return
        }

        if (
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            result.success(true)
            return
        }

        if (permissionResult != null) {
            result.error(
                "REQUEST_IN_PROGRESS",
                "Demande en cours",
                null
            )
            return
        }

        permissionResult = result

        requestPermissions(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            2001
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 2001) {
            val granted =
                grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED

            permissionResult?.success(granted)
            permissionResult = null
        }
    }
}
