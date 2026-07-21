package site.siredvin.peripheralworks.tags

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import site.siredvin.peripheralworks.PeripheralWorksCore

@Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
object ItemTags {
    val PERIPHERAL_PROXY_FORBIDDEN = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "peripheral_proxy_forbidden"))
    val REALITY_FORGER_FORBIDDEN = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "reality_forger_forbidden"))
}
