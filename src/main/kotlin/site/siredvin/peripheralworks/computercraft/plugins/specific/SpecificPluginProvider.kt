package site.siredvin.peripheralworks.computercraft.plugins.specific

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BeaconBlockEntity
import net.minecraft.world.level.block.entity.LecternBlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider

class SpecificPluginProvider: PeripheralPluginProvider {
    override val pluginType: String
        get() = "minecraft"

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        val entity = level.getBlockEntity(pos) ?: return null
        return when (entity::class.java) {
            LecternBlockEntity::class.java -> LecternPlugin(entity as LecternBlockEntity)
            BeaconBlockEntity::class.java -> BeaconPlugin(entity as BeaconBlockEntity)
            else -> null
        }
    }
}