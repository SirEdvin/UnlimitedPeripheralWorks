package site.siredvin.peripheralworks.utils

import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore

fun modId(text: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, text)
