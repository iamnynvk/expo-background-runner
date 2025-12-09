package expo.modules.backgroundrunner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BackgroundAutostartReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null || intent == null) return

    val action = intent.action
    Log.d("AutostartReceiver", "Received action: $action")

    // Only allow valid restart triggers
    if (
      action != Intent.ACTION_BOOT_COMPLETED &&
      action != Intent.ACTION_MY_PACKAGE_REPLACED &&
      action != "expo.backgroundrunner.RESTART"
    ) {
      Log.d("AutostartReceiver", "Ignored action")
      return
    }

    val options = BackgroundStorage.lastOptions
    if (options == null) {
      Log.d("AutostartReceiver", "No stored options — nothing to restart")
      return
    }

    try {
      val serviceIntent = Intent(context, BackgroundRunnerService::class.java).apply {
        putExtra("options", HashMap(options))
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
      } else {
        context.startService(serviceIntent)
      }

      Log.d("AutostartReceiver", "Service restarted with saved options")
    } catch (e: Exception) {
      Log.e("AutostartReceiver", "Failed to restart: ${e.message}")
    }
  }
}
