package expo.modules.backgroundrunner

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class BackgroundAlarmReceiver : BroadcastReceiver() {

  @SuppressLint("UnsafeIntentLaunch")
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null || intent == null) return

    try {
      val options = intent.getSerializableMap("options")

      // Ensure notification channel before service starts
      BackgroundNotificationController.ensureChannel(context)

      // Start foreground service
      val serviceIntent = Intent(context, BackgroundRunnerService::class.java).apply {
        if (options != null) putExtra("options", options)
        putExtra("fromAlarm", true)
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
      } else {
        context.startService(serviceIntent)
      }

      // Reschedule next day
      scheduleNextDay(context, intent)

    } catch (e: Exception) {
      Log.e("BGAlarmReceiver", "Failed: ${e.message}")
    }
  }

  private fun scheduleNextDay(context: Context, originalIntent: Intent) {
    val hour = originalIntent.getIntExtra("hour", -1)
    val minute = originalIntent.getIntExtra("minute", -1)
    if (hour < 0 || minute < 0) return

    val cal = Calendar.getInstance().apply {
      add(Calendar.DATE, 1)
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
    }

    val alarmIntent = PendingIntent.getBroadcast(
      context,
      9999,
      originalIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, alarmIntent)
    } else {
      alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, alarmIntent)
    }

    Log.d("BGAlarmReceiver", "Next alarm set for: $hour:$minute tomorrow")
  }
}

@Suppress("DEPRECATION")
fun Intent.getSerializableMap(key: String): HashMap<String, Any>? {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    this.getSerializableExtra(key, HashMap::class.java) as? HashMap<String, Any>
  } else {
    this.getSerializableExtra(key) as? HashMap<String, Any>
  }
}
