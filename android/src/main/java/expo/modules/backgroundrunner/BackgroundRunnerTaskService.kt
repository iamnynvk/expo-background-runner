package expo.modules.backgroundrunner

import android.content.Intent
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class BackgroundRunnerTaskService : HeadlessJsTaskService() {

    companion object {
        private const val TAG = "BGTaskService"
        private const val TASK_NAME = "BackgroundRunnerTask"
        private const val TIMEOUT = 60000
    }

    /** Safe param extractor */
    private fun extractParams(intent: Intent?): Map<String, Any> {
        val options = intent?.getSerializableMap("options") ?: emptyMap<String, Any>()
        return options["parameters"] as? Map<String, Any> ?: emptyMap()
    }

    /** Foreground/background event-based handler */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return try {
            val params = extractParams(intent)
            Log.d(TAG, "onStartCommand params: $params")

            BackgroundEventEmitter.fireExecuteEvent(
                mapOf("parameters" to params)
            )

            super.onStartCommand(intent, flags, startId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to emit event: ${e.message}")
            super.onStartCommand(intent, flags, startId)
        }
    }

    /** Kill-mode → Headless JS handler */
    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig {
        val params = extractParams(intent)
        Log.d(TAG, "getTaskConfig params: $params")

        val jsParams = Arguments.makeNativeMap(
            mapOf("parameters" to params)
        )

        return HeadlessJsTaskConfig(
            TASK_NAME,
            jsParams,
            TIMEOUT.toLong(),
            true // allow in foreground too
        )
    }
}
