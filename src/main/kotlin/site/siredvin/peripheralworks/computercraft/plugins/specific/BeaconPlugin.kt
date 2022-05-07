package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.entity.BeaconBlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralworks.common.ExtractorProxy
import java.util.*
import java.util.function.Predicate
import kotlin.math.min

class BeaconPlugin(private val target: BeaconBlockEntity): IPeripheralPlugin {
    override val additionalType: String
        get() = "beacon"

    @LuaFunction(mainThread = true)
    fun getLevel(): Int {
        return target.levels
    }

    @LuaFunction(mainThread = true)
    fun getPossiblePowers(): List<String> {
        val powers: MutableList<String> = mutableListOf()
        for (i in 0..min(target.levels - 1, 2)) {
            BeaconBlockEntity.BEACON_EFFECTS[i].forEach { powers.add(it.descriptionId) }
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
    fun configure(computer: IComputerAccess, primaryPower: String, fromName: String, itemHint: Optional<String>, regenerationSecondary: Optional<Boolean>): MethodResult {
        var primaryEffect: MobEffect? = null
        for (i in 0..min(target.levels - 1, 2)) {
            primaryEffect = BeaconBlockEntity.BEACON_EFFECTS[i].find { it.descriptionId == primaryPower }
            if (primaryEffect != null)
                break
        }
        if (primaryEffect == null)
            return MethodResult.of(null, "Cannot find desired effect in list of available effects")

        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractItemStorage(location.target)
            ?: throw LuaException("Target '$fromName' is not an fluid inventory")

        val itemPredicate = if (itemHint.isPresent) {
            val item = Registry.ITEM.get(ResourceLocation(itemHint.get()))
            if (item == Items.AIR)
                throw LuaException("Cannot find item ${itemHint.get()}")
            Predicate<ItemVariant> { it.isOf(item) }
        } else {
            Predicate<ItemVariant> { it.toStack().`is`(ItemTags.BEACON_PAYMENT_ITEMS) }
        }

        Transaction.openOuter().use {
            val extractableResource = StorageUtil.findExtractableResource(fromStorage, itemPredicate, it)
                ?: return MethodResult.of(null, "Target storage cannot provide desired items")
            val amount = fromStorage.extract(extractableResource, 1, it)
            if (amount != 1L)
                return MethodResult.of(null, "Target storage cannot provide desired items")
            it.commit()
        }

        target.primaryPower = primaryEffect

        if (target.levels == 4)
            if (regenerationSecondary.orElse(false)) {
                target.secondaryPower = MobEffects.REGENERATION
            } else {
                target.secondaryPower = primaryEffect
            }

        target.level!!.blockEntityChanged(target.blockPos)

        return MethodResult.of(true)
    }
}