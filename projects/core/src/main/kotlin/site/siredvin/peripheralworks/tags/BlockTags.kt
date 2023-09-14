package site.siredvin.peripheralworks.tags

import dan200.computercraft.api.ComputerCraftAPI
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import site.siredvin.peripheralworks.PeripheralWorksCore

object BlockTags {
    val PERIPHERAL_PROXY_FORBIDDEN = TagKey.create(Registries.BLOCK, ResourceLocation(PeripheralWorksCore.MOD_ID, "peripheral_proxy_forbidden"))
    val REALITY_FORGER_FORBIDDEN = TagKey.create(Registries.BLOCK, ResourceLocation(PeripheralWorksCore.MOD_ID, "reality_forger_forbidden"))
    val CCT_PERIPHERAL_HUB_IGNORE = TagKey.create(Registries.BLOCK, ResourceLocation(ComputerCraftAPI.MOD_ID,"peripheral_hub_ignore"))
}
