package site.siredvin.peripheralworks.computercraft.operations

import net.neoforged.neoforge.common.ModConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOperation

enum class UnconditionalFreeOperations(
    private val defaultCooldown: Int,
) : IPeripheralOperation<Any?>,
    IForgeConfigHandler {
    INSPECT_CHUNK(10_000),
    UPDATE_MAP(60_000),
    EXTRACT_MAP(10_000),
    ;

    private var cooldown: ModConfigSpec.IntValue? = null

    override fun addToConfig(builder: ModConfigSpec.Builder) {
        cooldown = builder.defineInRange(
            settingsName() + "Cooldown",
            defaultCooldown,
            0,
            Int.MAX_VALUE,
        )
    }

    override fun computerDescription(): Map<String, Any?> {
        val data: MutableMap<String, Any> = HashMap()
        data["name"] = settingsName()
        data["type"] = javaClass.name
        data["cooldown"] = cooldown!!.get()
        return data
    }

    override fun getCooldown(context: Any?): Int = cooldown!!.get()

    override fun getCost(context: Any?): Int = 0
}
