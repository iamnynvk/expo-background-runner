package expo.modules.backgroundrunner

import android.util.Log
import expo.modules.kotlin.modules.Module

object BackgroundEventEmitter {

    @Volatile
    private var moduleRef: Module? = null

    private val pendingEvents = mutableListOf<Map<String, Any?>>()

    /**
     * Attach JS module reference (called when module loads).
     * Any pending events (queued when JS wasn't ready) are flushed immediately.
     */
    @Synchronized
    fun setModule(module: Module?) {
        moduleRef = module

        Log.d("BGEmitter", "Module attached: $module | pending=${pendingEvents.size}")

        if (module == null || pendingEvents.isEmpty()) return

        // Flush all pending events
        val events = pendingEvents.toList()
        pendingEvents.clear()

        events.forEach { event ->
            try {
                module.sendEvent("onExecute", event)
                Log.d("BGEmitter", "Flushed pending event: $event")
            } catch (e: Exception) {
                Log.e("BGEmitter", "Failed sending pending event: ${e.message}")
            }
        }
    }

    /**
     * Fire event to JS. If JS module not available yet, event will be queued.
     */
    fun fireExecuteEvent(data: Map<String, Any?>) {
        val module = moduleRef

        if (module != null) {
            try {
                module.sendEvent("onExecute", data)
                Log.d("BGEmitter", "Sent event to JS: $data")
            } catch (e: Exception) {
                Log.e("BGEmitter", "Send event failed: ${e.message}")
            }
            return
        }

        // JS not ready → queue the event
        synchronized(this) {
            pendingEvents.add(data)
        }

        Log.d("BGEmitter", "JS not ready → queued event: $data")
    }

    /**
     * Clears all pending queued events. Useful for tests.
     */
    @Synchronized
    fun clearPending() {
        pendingEvents.clear()
    }
}
