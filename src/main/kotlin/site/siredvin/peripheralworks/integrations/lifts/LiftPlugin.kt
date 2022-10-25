package site.siredvin.peripheralworks.integrations.lifts

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import io.github.lucaargolo.lifts.common.blockentity.lift.ElectricLiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.util.assertBetween
import site.siredvin.peripheralworks.api.PeripheralPluginProvider

class LiftPlugin(private val entity: LiftBlockEntity): IPeripheralPlugin {

    companion object {
        const val PLUGIN_TYPE = "lift"
    }

    class Provider: PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableLift)
                return null
            val entity = level.getBlockEntity(pos)
            if (entity !is LiftBlockEntity)
                return null
            return LiftPlugin(entity)
        }
    }

    private fun reverseNumber(index: Int): Int {
        return entity.liftShaft!!.size - index
    }

    private fun getLiftNameRaw(index: Int): Component {
        val floor = reverseNumber(index)
        val suffix = when (floor) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }

        return TextComponent(floor.toString()).append(TranslatableComponent("screen.lifts.number.$suffix"))
            .append(" ").append(TranslatableComponent("screen.lifts.common.floor") )
    }

    private fun getLiftName(index: Int, lift: LiftBlockEntity): Component {
        if (lift.liftName != null)
            return TextComponent(lift.liftName!!)
        return getLiftNameRaw(index)
    }

    private fun toLuaBase(index: Int, lift: LiftBlockEntity): MutableMap<String, Any?> {
        return hashMapOf(
            "name" to getLiftName(index, lift).string,
            "ready" to lift.ready,
            "number" to reverseNumber(index)
        )
    }

    private fun toLuaElectric(index: Int, lift: ElectricLiftBlockEntity): MutableMap<String, Any?> {
        val base = toLuaBase(index, lift)
        base["energy"] = hashMapOf(
            "amount" to lift.energyStorage.amount,
            "capacity" to lift.energyStorage.capacity
        )
        return base
    }

    private fun toLuaStirling(index: Int, lift: StirlingLiftBlockEntity): MutableMap<String, Any?> {
        val base = toLuaBase(index, lift)
        lift.propertyDelegate
        base["generator"] = hashMapOf(
            "burningTicks" to lift.propertyDelegate.get(1),
            "storedTicks" to lift.propertyDelegate.get(2)
        )
        return base
    }

    private fun toLua(index: Int, lift: LiftBlockEntity): MutableMap<String, Any?> {
        if (lift is StirlingLiftBlockEntity)
            return toLuaStirling(index, lift)
        if (lift is ElectricLiftBlockEntity)
            return toLuaElectric(index, lift)
        return toLuaBase(index, lift)
    }

    @LuaFunction(mainThread = true)
    fun floors(): List<Map<String, Any?>> {
        if (entity.liftShaft == null) {
            return listOf(toLua(0, entity))
        }
        return entity.liftShaft!!.lifts.mapIndexed { index, it -> toLua(index, it) }
    }

    @LuaFunction(mainThread = true)
    fun getFloorName(index: Int): String {
        assertBetween(index, 1, Int.MAX_VALUE, "Floor should be positive integer")
        val realIndex = reverseNumber(index)
        if (entity.liftShaft == null)
            return getLiftNameRaw(realIndex).string
        val maxFloor = entity.liftShaft!!.lifts.size
        assertBetween(index, 1, maxFloor, "There is only $maxFloor present")
        entity.liftShaft!!.lifts.forEachIndexed { internal_index, liftBlockEntity ->
            if (internal_index == realIndex)
                return getLiftName(realIndex, liftBlockEntity).string
        }
        return getLiftNameRaw(realIndex).string
    }

    @LuaFunction(mainThread = true)
    fun getCurrentFloor(): MethodResult {
        if (entity.liftShaft == null)
            return MethodResult.of(null, "Lift shaft are not exists")

        entity.liftShaft!!.lifts.forEachIndexed { index, liftBlockEntity ->
            if (liftBlockEntity.isPlatformHere)
                return MethodResult.of(reverseNumber(index))
        }
        return MethodResult.of(null, "Seems platform on the way")
    }

    @LuaFunction(mainThread = true)
    fun changeFloor(floor: Int): MethodResult {
        val index = reverseNumber(floor)
        val targetLift = entity.liftShaft!!.lifts.elementAtOrNull(index)
            ?: return MethodResult.of(null, "Floor should be from 1 to ${entity.liftShaft!!.size}")
        val result = entity.liftShaft!!.sendPlatformTo(entity.level!!, targetLift, false)
        if (!result.isAccepted())
            return MethodResult.of(null, result.name.lowercase())
        return MethodResult.of(true)
    }
}