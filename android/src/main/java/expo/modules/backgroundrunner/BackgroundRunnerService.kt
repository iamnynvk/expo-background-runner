package expo.modules.backgroundrunner

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class BackgroundRunnerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {

            // ---- GET OPTIONS SAFELY ----
            val options = intent?.getSerializableMap("options")
                ?: BackgroundStorage.lastOptions
                ?: hashMapOf()

            BackgroundStorage.save(options)

            // ---- ALWAYS CREATE CHANNEL FIRST ----
            BackgroundNotificationController.ensureChannel(this)

            // ---- ALWAYS BUILD + APPLY NOTIFICATION FIRST TIME ----
            val notification =
                BackgroundNotificationController.buildForegroundNotification(this, options)

            // ---- FOREGROUND SERVICE START ----
            startForeground(
                BackgroundNotificationController.NOTIFICATION_ID,
                notification
            )

            // ---- START HEADLESS JS TASK ----
            val headlessIntent =
                Intent(applicationContext, BackgroundRunnerTaskService::class.java).apply {
                    putExtra("options", HashMap(options))
                }

            applicationContext.startService(headlessIntent)

        } catch (e: Exception) {
            Log.e("BGService", "Error: ${e.message}")
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        sendBroadcast(Intent("expo.backgroundrunner.RESTART"))
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
    }
}
