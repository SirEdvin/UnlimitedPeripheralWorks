package site.siredvin.peripheralworks.tags

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import site.siredvin.peripheralworks.PeripheralWorksCore

@Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
object EntityTags {
    val LINK_BLOCKLIST = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "link_blocklist"))
}
