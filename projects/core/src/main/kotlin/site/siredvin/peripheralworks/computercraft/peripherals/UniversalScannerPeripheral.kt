package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.UniversalScannerBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.boon.PeripheralOwnerBoonKey
import site.siredvin.tweakium.modules.peripheral.boon.ScanningBoon
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner

class UniversalScannerPeripheral(owner: IPeripheralOwner) : OwnedPeripheral<IPeripheralOwner>(TYPE, owner) {

    companion object {
        const val TYPE = "universal_scanner"

        @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, TYPE)

        fun of(turtle: ITurtleAccess, side: TurtleSide): UniversalScannerPeripheral {
            val owner = TurtlePeripheralOwner(turtle, side)
            owner.attachOperations(cooldownThreshold = PeripheralWorksConfig.cooldownTresholdLevel)
            owner.attachFuel()
            return UniversalScannerPeripheral(owner)
        }

        fun of(pocket: IPocketAccess): UniversalScannerPeripheral {
            val owner = PocketPeripheralOwner(pocket)
            owner.attachOperations(cooldownThreshold = PeripheralWorksConfig.cooldownTresholdLevel)
            owner.attachFuel()
            return UniversalScannerPeripheral(owner)
        }

        fun of(blockEntity: UniversalScannerBlockEntity): UniversalScannerPeripheral {
            val owner = BlockEntityPeripheralOwner(blockEntity)
            owner.attachOperations(cooldownThreshold = PeripheralWorksConfig.cooldownTresholdLevel)
            return UniversalScannerPeripheral(owner)
        }
    }

    init {
        val operation = if (owner.getBoon(PeripheralOwnerBoonKey.FUEL) != null) {
            SphereOperations.PORTABLE_UNIVERSAL_SCAN
        } else {
            // Because only block entity doesn't have fuel ability here
            SphereOperations.STATIONARY_UNIVERSAL_SCAN
        }
        val maxRadius = operation.maxCostRadius
        owner.attachBoon(
            PeripheralOwnerBoonKey.SCANNING,
            ScanningBoon(owner, maxRadius).attachBlockScan(
                operation,
            ).attachLivingEntityScan(
                operation,
                { true },
            ).attachItemScan(
                operation,
            ).attachPlayerScan(
                operation,
            ),
        )
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableUniversalScanner
}
