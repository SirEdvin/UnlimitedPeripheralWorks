package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.ability.PeripheralOwnerAbility
import site.siredvin.peripheralium.computercraft.peripheral.ability.ScanningAbility
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.owner.PocketPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.UniversalScannerBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations

class UniversalScannerPeripheral(owner: IPeripheralOwner) : OwnedPeripheral<IPeripheralOwner>(TYPE, owner) {

    companion object {
        const val TYPE = "universal_scanner"
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, TYPE)

        fun of(turtle: ITurtleAccess, side: TurtleSide): UniversalScannerPeripheral {
            val owner = TurtlePeripheralOwner(turtle, side)
            owner.attachOperations(config = PeripheralWorksConfig)
            owner.attachFuel()
            return UniversalScannerPeripheral(owner)
        }

        fun of(pocket: IPocketAccess): UniversalScannerPeripheral {
            val owner = PocketPeripheralOwner(pocket)
            owner.attachOperations(config = PeripheralWorksConfig)
            owner.attachFuel()
            return UniversalScannerPeripheral(owner)
        }

        fun of(blockEntity: UniversalScannerBlockEntity): UniversalScannerPeripheral {
            val owner = BlockEntityPeripheralOwner(blockEntity)
            owner.attachOperations(config = PeripheralWorksConfig)
            return UniversalScannerPeripheral(owner)
        }
    }

    init {
        val maxRadius = if (owner.getAbility(PeripheralOwnerAbility.FUEL) != null) {
            SphereOperations.UNIVERSAL_SCAN.maxCostRadius
        } else {
            SphereOperations.UNIVERSAL_SCAN.maxFreeRadius
        }
        owner.attachAbility(
            PeripheralOwnerAbility.SCANNING,
            ScanningAbility(owner, maxRadius).attachBlockScan(
                SphereOperations.UNIVERSAL_SCAN,
            ).attachLivingEntityScan(
                SphereOperations.UNIVERSAL_SCAN,
                { true },
            ).attachItemScan(
                SphereOperations.UNIVERSAL_SCAN,
            ).attachPlayerScan(
                SphereOperations.UNIVERSAL_SCAN,
            ),
        )
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableUniversalScanner
}
