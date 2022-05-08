package site.siredvin.peripheralworks.api

import dan200.computercraft.shared.util.ItemStorage
import net.minecraft.world.level.Level

fun interface CCItemStorageExtractor {
    fun extract(level: Level, obj: Any?): ItemStorage?
}