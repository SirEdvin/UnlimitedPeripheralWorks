package site.siredvin.peripheralworks.computercraft.operations

import com.google.common.math.IntMath
import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.peripheral.IPeripheralOperation
import site.siredvin.peripheralium.computercraft.operations.SphereOperationContext
import kotlin.math.floor

enum class SphereOperations(
    private val defaultCooldown: Int,
    private val defaultMaxFreeRadius: Int,
    private val defaultMaxCostRadius: Int,
    private val defaultExtraBlockCost: Double,
) : IPeripheralOperation<SphereOperationContext> {
    PORTABLE_UNIVERSAL_SCAN(1000, 8, 16, 0.17),
    STATIONARY_UNIVERSAL_SCAN(500, 24, 24, 0.0),
    ;

    private var cooldown: ForgeConfigSpec.IntValue? = null
    private var maxFreeRadiusConfig: ForgeConfigSpec.IntValue? = null
    private var maxCostRadiusConfig: ForgeConfigSpec.IntValue? = null
    private var extraBlockCost: ForgeConfigSpec.DoubleValue? = null
    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        cooldown = builder.defineInRange(
            settingsName() + "Cooldown",
            defaultCooldown,
            0,
            Int.MAX_VALUE,
        )
        maxFreeRadiusConfig = builder.defineInRange(
            settingsName() + "MaxFreeRadius",
            defaultMaxFreeRadius,
            1,
            Int.MAX_VALUE,
        )
        maxCostRadiusConfig = builder.defineInRange(
            settingsName() + "MaxCostRadius",
            defaultMaxCostRadius,
            1,
            Int.MAX_VALUE,
        )
        extraBlockCost = builder.defineInRange(
            settingsName() + "ExtraBlockCost",
            defaultExtraBlockCost,
            0.1,
            Double.MAX_VALUE,
        )
    }

    override fun getCooldown(context: SphereOperationContext): Int = cooldown!!.get()

    override fun getCost(context: SphereOperationContext): Int {
        if (context.radius <= maxFreeRadiusConfig!!.get()) return 0
        val freeBlockCount = IntMath.pow(2 * maxFreeRadiusConfig!!.get() + 1, 3)
        val allBlockCount = IntMath.pow(2 * context.radius + 1, 3)
        return floor((allBlockCount - freeBlockCount) * extraBlockCost!!.get()).toInt()
    }

    val maxFreeRadius: Int
        get() = maxFreeRadiusConfig!!.get()
    val maxCostRadius: Int
        get() = maxCostRadiusConfig!!.get()

    override fun computerDescription(): Map<String, Any> {
        val data: MutableMap<String, Any> = HashMap()
        data["name"] = settingsName()
        data["type"] = javaClass.name
        data["cooldown"] = cooldown!!.get()
        data["maxFreeRadius"] = maxFreeRadiusConfig!!.get()
        data["maxCostRadius"] = maxCostRadiusConfig!!.get()
        data["extraBlockCost"] = extraBlockCost!!.get()
        return data
    }

    fun free(): SphereOperationContext = SphereOperationContext(maxFreeRadius)

    fun cost(): SphereOperationContext = SphereOperationContext(maxCostRadius)
}
