package expo.modules.backgroundrunner

/**
 * Safe global in-memory storage for passing options between service,
 * broadcast receivers, and headless JS tasks.
 */
object BackgroundStorage {

    // Volatile → thread-safe read/write across AlarmReceiver, Service, AutoStartReceiver etc.
    @Volatile
    private var _lastOptions: HashMap<String, Any>? = null

    /**
     * Public getter
     */
    val lastOptions: HashMap<String, Any>?
        get() = _lastOptions

    /**
     * Save options (called from startNative or when service restarts)
     */
    fun save(options: Map<String, Any>) {
        _lastOptions = HashMap(options)
    }

    /**
     * Clear stored options when service stops.
     */
    fun clear() {
        _lastOptions = null
    }

    /**
     * Returns true if any stored background config exists.
     */
    fun hasOptions(): Boolean = _lastOptions != null
}
