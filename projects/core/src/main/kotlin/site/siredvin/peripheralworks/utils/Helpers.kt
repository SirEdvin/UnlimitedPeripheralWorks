package site.siredvin.peripheralworks.utils

import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore

@Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
fun modId(text: String): ResourceLocation = ResourceLocation(PeripheralWorksCore.MOD_ID, text)
