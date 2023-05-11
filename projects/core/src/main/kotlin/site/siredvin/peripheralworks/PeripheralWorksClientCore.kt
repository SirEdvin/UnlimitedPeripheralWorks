package site.siredvin.peripheralworks

object PeripheralWorksClientCore {
    private val CLIENT_HOOKS: MutableList<Runnable> = mutableListOf()
    private var initialized: Boolean = false

    fun registerHook(it: Runnable) {
        if (!initialized)
            CLIENT_HOOKS.add(it)
        else
            it.run()
    }

    fun onInit() {
        CLIENT_HOOKS.forEach { it.run() }
        initialized = true
    }
}