package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.api.datatypes.AreaInteractionMode
import site.siredvin.peripheralium.api.peripheral.IPeripheralOperation
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.computercraft.operations.SphereOperationContext
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.ability.PeripheralOwnerAbility
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.owner.PocketPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralium.extra.plugins.AbstractScanningPlugin
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.UniversalScannerBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import java.util.function.BiConsumer
import java.util.function.Predicate

class UniversalScannerPeripheral(owner: IPeripheralOwner): OwnedPeripheral<IPeripheralOwner>(TYPE, owner) {

    companion object {
        const val TYPE = "universal_scanner"
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, TYPE)

        fun of(turtle: ITurtleAccess, side: TurtleSide): UniversalScannerPeripheral {
            val owner = TurtlePeripheralOwner(turtle, side)
            owner.attachOperations()
            owner.attachFuel()
            return UniversalScannerPeripheral(owner)
        }

        fun of(pocket: IPocketAccess): UniversalScannerPeripheral {
            val owner = PocketPeripheralOwner(pocket)
            owner.attachOperations()
            owner.attachFuel()
            return UniversalScannerPeripheral(owner)
        }

        fun of(blockEntity: UniversalScannerBlockEntity): UniversalScannerPeripheral {
            val owner = BlockEntityPeripheralOwner(blockEntity)
            owner.attachOperations()
            return UniversalScannerPeripheral(owner)
        }

    }

    internal class UniversalScanningPlugin(owner: IPeripheralOwner) : AbstractScanningPlugin(owner) {
        override val allowedMods: Set<AreaInteractionMode> = setOf(AreaInteractionMode.BLOCK, AreaInteractionMode.ENTITY, AreaInteractionMode.ITEM)
        override val operations: Array<IPeripheralOperation<*>> = arrayOf(SphereOperations.UNIVERSAL_SCAN)
        override val blockStateEnriches: List<BiConsumer<BlockState, MutableMap<String, Any>>> = emptyList()
        override val entityEnriches: List<BiConsumer<Entity, MutableMap<String, Any>>> = emptyList()
        override val itemEnriches: List<BiConsumer<ItemEntity, MutableMap<String, Any>>> = emptyList()
        override val scanBlocksOperation: IPeripheralOperation<SphereOperationContext> = SphereOperations.UNIVERSAL_SCAN
        override val scanEntitiesOperation: IPeripheralOperation<SphereOperationContext> = SphereOperations.UNIVERSAL_SCAN
        override val scanItemsOperation: IPeripheralOperation<SphereOperationContext> = SphereOperations.UNIVERSAL_SCAN
        override val suitableEntity: Predicate<Entity> = Predicate<Entity> { it is LivingEntity }.and { it !is Player }

        override val scanRadius: Int
            get() =
                if (owner.getAbility(PeripheralOwnerAbility.FUEL) != null) {
                    SphereOperations.UNIVERSAL_SCAN.maxCostRadius
                } else {
                    SphereOperations.UNIVERSAL_SCAN.maxFreeRadius
                }
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableUniversalScanner

    init {
        addPlugin(UniversalScanningPlugin(peripheralOwner))
    }
}