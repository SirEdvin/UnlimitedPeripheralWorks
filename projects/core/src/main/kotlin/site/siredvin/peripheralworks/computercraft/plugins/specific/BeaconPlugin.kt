package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.tags.ItemTags
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BeaconBlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.api.storage.ExtractorProxy
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import java.util.*
import java.util.function.Predicate
import kotlin.math.min

class BeaconPlugin(private val target: BeaconBlockEntity): IPeripheralPlugin {

    @LuaFunction(mainThread = true)
    fun getLevel(): Int {
        return target.levels
    }

    @LuaFunction(mainThread = true)
    fun getPossiblePowers(): List<String> {
        val powers: MutableList<String> = mutableListOf()
        for (i in 0..min(target.levels - 1, 2)) {
            BeaconBlockEntity.BEACON_EFFECTS[i].forEach { powers.add(
                LuaRepresentation.fromLegacyToNewID(it.descriptionId)
            ) }
        }
        return powers
    }

    @LuaFunction(mainThread = true)
    fun getPowers(): List<Map<String, Any>> {
        val effectTime: Int = (9 + target.levels * 2) * 20
        if (target.primaryPower == null)
            return emptyList()
        val effects: MutableList<Map<String, Any>> = mutableListOf()
        effects.add(
            LuaRepresentation.forMobEffectInstance(MobEffectInstance(
                target.primaryPower!!, effectTime,
                if (target.secondaryPower == target.primaryPower) 1 else 0,
                true, true
            ))
        )
        if (target.secondaryPower != null && target.secondaryPower != target.primaryPower)
            effects.add(LuaRepresentation.forMobEffectInstance(
                MobEffectInstance(
                target.secondaryPower!!, effectTime, 0, true, true
            )
            ))
        return effects
    }

    @LuaFunction(mainThread = true)
    fun configure(computer: IComputerAccess, arguments: IArguments): MethodResult {
        val primaryPower = arguments.getString(0)
        val fromName = arguments.getString(1)
        val itemQuery = arguments.get(2)
        val regenerationSecondary = arguments.optBoolean(3, false)
        var primaryEffect: MobEffect? = null
        for (i in 0..min(target.levels - 1, 2)) {
            primaryEffect = BeaconBlockEntity.BEACON_EFFECTS[i].find { LuaRepresentation.fromLegacyToNewID(it.descriptionId) == primaryPower }
            if (primaryEffect != null)
                break
        }
        if (primaryEffect == null)
            return MethodResult.of(null, "Cannot find desired effect in list of available effects")

        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractStorage(target.level!!, location.target)
            ?: throw LuaException("Target '$fromName' is not an item inventory")

        var predicate = Predicate<ItemStack> { it.`is`(ItemTags.BEACON_PAYMENT_ITEMS) }

        if (itemQuery != null)
            predicate = predicate.and(PeripheralPluginUtils.itemQueryToPredicate(itemQuery))

        val extractedStack = fromStorage.takeItems(predicate, 1)
        if (extractedStack.isEmpty)
            return MethodResult.of(null, "Target storage cannot provide desired items")

        target.primaryPower = primaryEffect

        if (target.levels == 4)
            if (regenerationSecondary) {
                target.secondaryPower = MobEffects.REGENERATION
            } else {
                target.secondaryPower = primaryEffect
            }

        target.level!!.blockEntityChanged(target.blockPos)

        return MethodResult.of(true)
    }
}