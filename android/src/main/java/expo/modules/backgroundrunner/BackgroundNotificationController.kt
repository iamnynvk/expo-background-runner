package expo.modules.backgroundrunner

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

object BackgroundNotificationController {

    private const val CHANNEL_ID = "background_runner_channel"
    const val NOTIFICATION_ID = 10001

    private var builder: NotificationCompat.Builder? = null

    // -------------------------------------------------------------------
    // 1) MUST BE CALLED BEFORE ANY NOTIFICATION
    // -------------------------------------------------------------------
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Runner",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service notifications"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            manager.createNotificationChannel(channel)
        }
    }

    // -------------------------------------------------------------------
    // 2) FOREGROUND START (FIRST NOTIFICATION)
    // -------------------------------------------------------------------
    fun buildForegroundNotification(
        context: Context,
        options: Map<String, Any>
    ): Notification {

        ensureChannel(context)

        val title = options["taskTitle"]?.toString() ?: "Background Task"
        val desc = options["taskDesc"]?.toString() ?: "Running..."
        val colorStr = options["color"] as? String
        val iconMap = options["taskIcon"] as? Map<*, *>
        val linkUri = options["linkingURI"] as? String

        val iconRes = resolveIcon(context, iconMap)
        val tapIntent = createTapIntent(context, linkUri)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(desc)
            .setSmallIcon(iconRes)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (colorStr != null) {
            try { builder!!.color = Color.parseColor(colorStr) } catch (_: Exception) {}
        }

        return builder!!.build()
    }

    // -------------------------------------------------------------------
    // 3) UPDATE NOTIFICATION (ALREADY RUNNING)
    // -------------------------------------------------------------------
    fun updateNotification(context: Context, options: Map<String, Any>) {
        if (builder == null) {
            // Service not started properly → build fallback
            val temp = buildForegroundNotification(context, options)
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, temp)
            return
        }

        val title = options["taskTitle"] as? String
        val desc = options["taskDesc"] as? String
        val colorStr = options["color"] as? String
        val iconMap = options["taskIcon"] as? Map<*, *>

        if (title != null) builder!!.setContentTitle(title)
        if (desc != null) builder!!.setContentText(desc)

        if (colorStr != null) {
            try { builder!!.color = Color.parseColor(colorStr) } catch (_: Exception) {}
        }

        if (iconMap != null) {
            builder!!.setSmallIcon(resolveIcon(context, iconMap))
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(NOTIFICATION_ID, builder!!.build())
    }

    // -------------------------------------------------------------------
    // 4) STOP FOREGROUND
    // -------------------------------------------------------------------
    fun stopForegroundNotification(service: Service) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                service.stopForeground(true)
            }
            val manager =
                service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}

        builder = null
    }

    // -------------------------------------------------------------------
    // UTIL: RESOLVE ICON
    // -------------------------------------------------------------------
    private fun resolveIcon(context: Context, iconMap: Map<*, *>?): Int {
        val defaultIcon = context.applicationInfo.icon.takeIf { it != 0 }
            ?: android.R.drawable.ic_popup_sync

        if (iconMap == null) return defaultIcon

        val name = iconMap["name"] as? String ?: return defaultIcon
        val type = iconMap["type"] as? String ?: "mipmap"

        val resId = context.resources.getIdentifier(name, type, context.packageName)
        return if (resId != 0) resId else defaultIcon
    }

    // -------------------------------------------------------------------
    // UTIL: TAP INTENT
    // -------------------------------------------------------------------
    private fun createTapIntent(context: Context, link: String?): Intent {
        return if (!link.isNullOrEmpty()) {
            Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(link)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                ?: Intent(context, Class.forName("${context.packageName}.MainActivity")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
        }
    }
}
