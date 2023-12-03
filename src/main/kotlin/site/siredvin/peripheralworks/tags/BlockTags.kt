package site.siredvin.peripheralworks.tags

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import site.siredvin.peripheralworks.PeripheralWorks

object BlockTags {
    val DEFERRED_FLUID_STORAGE = TagKey.create(
        Registry.BLOCK_REGISTRY,
        ResourceLocation(PeripheralWorks.MOD_ID, "deferred_fluid_storage"),
    )
    val IGNORE = TagKey.create(
        Registry.BLOCK_REGISTRY,
        ResourceLocation(PeripheralWorks.MOD_ID, "ignore"),
    )
}
