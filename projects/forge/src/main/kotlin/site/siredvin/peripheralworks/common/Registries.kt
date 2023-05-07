package site.siredvin.peripheralworks.common

import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraftforge.registries.DeferredRegister
import site.siredvin.peripheralworks.PeripheralWorksCore


object Registries {
    val TURTLE_SERIALIZER = DeferredRegister.create(
        TurtleUpgradeSerialiser.REGISTRY_ID,
        PeripheralWorksCore.MOD_ID
    )
    val POCKET_SERIALIZER = DeferredRegister.create(
        PocketUpgradeSerialiser.REGISTRY_ID,
        PeripheralWorksCore.MOD_ID
    )
}