package site.siredvin.peripheralworks.tags

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import site.siredvin.peripheralworks.PeripheralWorksCore

object BlockTags {
    val PERIPHERAL_PROXY_FORBIDDEN = TagKey.create(Registries.BLOCK, ResourceLocation(PeripheralWorksCore.MOD_ID, "peripheral_proxy_forbidden"))
}