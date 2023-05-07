package site.siredvin.peripheralworks

import net.fabricmc.api.ClientModInitializer

object FabricPeripheralWorksClient: ClientModInitializer {
    private val CLIENT_HOOKS: MutableList<Runnable> = mutableListOf()
    private var initialized: Boolean = false

    fun registerHook(it: Runnable) {
        if (!initialized)
            CLIENT_HOOKS.add(it)
        else
            it.run()
    }

    override fun onInitializeClient() {
        CLIENT_HOOKS.forEach { it.run() }
        initialized = true
    }
}