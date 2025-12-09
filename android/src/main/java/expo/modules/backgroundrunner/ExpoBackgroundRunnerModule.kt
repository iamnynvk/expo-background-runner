package expo.modules.backgroundrunner

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.util.Calendar

class ExpoBackgroundRunnerModule : Module() {

  private var isServiceRunning = false

  override fun definition() = ModuleDefinition {
    Name("ExpoBackgroundRunner")
    Events("onExecute")

    OnCreate {
      BackgroundEventEmitter.setModule(this@ExpoBackgroundRunnerModule)
    }

    // START SERVICE
    AsyncFunction("startNative") { options: Map<String, Any> ->
      val context = appContext.reactContext
        ?: throw IllegalStateException("ReactContext not available")

      val activity = appContext.currentActivity

      // Notification permission check
      if (!NotificationPermissionHelper.hasPermission(context)) {
        NotificationPermissionHelper.requestPermission(activity)
        NotificationPermissionHelper.showPermissionToast(context)
        return@AsyncFunction false
      }

      BackgroundStorage.save(options)

      // Ensure notification channel exists
      BackgroundNotificationController.ensureChannel(context)

      val intent = Intent(context, BackgroundRunnerService::class.java)
        .putExtra("options", HashMap(options))

      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          ContextCompat.startForegroundService(context, intent)
        } else {
          context.startService(intent)
        }
        isServiceRunning = true
        true
      } catch (e: Exception) {
        Log.e("BGRunner", "Failed to start service: ${e.message}")
        false
      }
    }

    // STOP SERVICE
    AsyncFunction("stop") {
     val ctx = appContext.reactContext ?: return@AsyncFunction false
     BackgroundStorage.clear()
     ctx.stopService(Intent(ctx, BackgroundRunnerService::class.java))
     ctx.stopService(Intent(ctx, BackgroundRunnerTaskService::class.java))
     cancelScheduledAlarm(ctx)
     isServiceRunning = false
     true
    }

    // SCHEDULE DAILY SERVICE
    AsyncFunction("scheduleDaily") { hour: Int, minute: Int, options: Map<String, Any> ->
      val ctx = appContext.reactContext
        ?: throw IllegalStateException("ReactContext not available")

      val activity = appContext.currentActivity

      if (!NotificationPermissionHelper.hasPermission(ctx)) {
        NotificationPermissionHelper.requestPermission(activity)
        NotificationPermissionHelper.showPermissionToast(ctx)
        return@AsyncFunction false
      }

      if (!NotificationPermissionHelper.hasAlarmPermission(ctx)) {
        NotificationPermissionHelper.requestAlarmPermission(ctx)
        Log.e("BGRunner", "Exact alarm permission not granted")
        return@AsyncFunction false
      }

      BackgroundStorage.shouldStop = false
      BackgroundNotificationController.ensureChannel(ctx)
      BackgroundStorage.save(options)

      val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

      val intent = Intent(ctx, BackgroundAlarmReceiver::class.java)
        .putExtra("options", HashMap(options))
        .putExtra("hour", hour)
        .putExtra("minute", minute)

      val pi = PendingIntent.getBroadcast(
        ctx, 9999, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
      )

      val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
          add(Calendar.DAY_OF_MONTH, 1)
        }
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
      }

      true
    }

    // UPDATE NOTIFICATION
    AsyncFunction("updateNotification") { options: Map<String, Any> ->
      BackgroundNotificationController.updateNotification(
        appContext.reactContext!!,
        options
      )
    }

    // BATTERY OPTIMIZATION
    Function("isBatteryOptIgnored") {
      BatteryOptimizationHelper.isIgnoringBatteryOptimizations(
        appContext.reactContext!!
      )
    }

    AsyncFunction("requestIgnoreBatteryOptimizations") {
      val activity = appContext.currentActivity
      if (activity != null) {
        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(activity)
        true
      } else {
        BatteryOptimizationHelper.openAutoStartSettings(appContext.reactContext!!)
        false
      }
    }

    Function("openAutoStartSettings") {
      BatteryOptimizationHelper.openAutoStartSettings(appContext.reactContext!!)
    }

    Function("isRunning") {
      isServiceRunning
    }
  }

  private fun cancelScheduledAlarm(context: Context) {
    val intent = Intent(context, BackgroundAlarmReceiver::class.java)

    val pi = PendingIntent.getBroadcast(
      context,
      9999, // SAME requestCode used in scheduleDaily
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pi)

    Log.i("BGRunner", "Scheduled alarm cancelled")
  }
}
