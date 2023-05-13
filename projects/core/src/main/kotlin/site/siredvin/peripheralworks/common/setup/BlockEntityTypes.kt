package site.siredvin.peripheralworks.common.setup

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.blockentity.UniversalScannerBlockEntity
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.function.Supplier

object BlockEntityTypes {
    val UNIVERSAL_SCANNER: Supplier<BlockEntityType<UniversalScannerBlockEntity>> = PeripheralWorksPlatform.registerBlockEntity(
        ResourceLocation(PeripheralWorksCore.MOD_ID, "universal_scanner")
    ) {
        PeripheralWorksPlatform.createBlockEntityType(
            { pos, state -> UniversalScannerBlockEntity(pos, state) },
            Blocks.UNIVERSAL_SCANNER.get()
        )
    }

    fun doSomething() {}
}