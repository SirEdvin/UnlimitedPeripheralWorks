package site.siredvin.peripheralworks.utils

import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore

fun modId(text: String): ResourceLocation {
    return ResourceLocation(PeripheralWorksCore.MOD_ID, text)
}
