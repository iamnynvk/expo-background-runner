package expo.modules.backgroundrunner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

object BatteryOptimizationHelper {

    private const val TAG = "BatteryOptHelper"

    /** Check if battery optimization is ignored */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean = try {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.isIgnoringBatteryOptimizations(context.packageName)
    } catch (e: Exception) {
        Log.w(TAG, "check opt error: ${e.message}")
        false
    }

    /** Request Ignore Battery Optimization using system dialog */
    fun requestIgnoreBatteryOptimizations(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "requestIgnore error: ${e.message}")
            openBatterySettings(activity)
        }
    }

    /** Open system battery optimization settings */
    private fun openBatterySettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Log.e(TAG, "openBatterySettings error: ${e.message}")
        }
    }

    /** Open OEM Auto-start settings (best-effort) */
    fun openAutoStartSettings(context: Context) {

        val pkg = context.packageName

        val intents = listOf(
            // Xiaomi
            intent("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"),
            intent("com.miui.powerkeeper", "com.miui.powerkeeper.ui.PowerSettingsActivity"),

            // Huawei
            intent("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),

            // Oppo / Realme
            intent("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            intent("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),

            // Vivo
            intent("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),

            // Samsung + generic fallback
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$pkg")
            }
        )

        for (i in intents) {
            try {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
                return
            } catch (_: Exception) {
                // try next
            }
        }
    }

    /** Helper: create component intent safely */
    private fun intent(pkg: String, activity: String): Intent {
        return Intent().apply {
            component = android.content.ComponentName(pkg, activity)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
